package com.todaktodot.TDTD.domain.feedback.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.admin.prompt.repository.AiPromptRepository;
import com.todaktodot.TDTD.admin.prompt.repository.entity.AiPromptEntity;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.CoupleDailyCardRepository;
import com.todaktodot.TDTD.domain.feedback.dto.ai.AiGeneratedFeedbackDTO;
import com.todaktodot.TDTD.domain.feedback.dto.reqeust.GenerateFeedbackRequestDTO;
import com.todaktodot.TDTD.domain.feedback.dto.response.FeedbackResponseDTO;
import com.todaktodot.TDTD.domain.feedback.repository.AiFeedbackConfigRepository;
import com.todaktodot.TDTD.domain.feedback.repository.DailyCardFeedbackRepository;
import com.todaktodot.TDTD.domain.feedback.repository.entity.AiFeedbackConfigEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.projection.FeedbackDataProjection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    private final ChatClient.Builder chatClientBuilder;
    private final DailyCardFeedbackRepository dailyCardFeedbackRepository;
    private final FeedbackMappingService feedbackMappingService;
    private final FeedbackPersistenceService feedbackPersistenceService;
    private final CoupleRepository coupleRepository;
    private final CoupleDailyCardRepository coupleDailyCardRepository;
    private final AiFeedbackConfigRepository feedbackConfigRepository;
    private final AiPromptRepository aiPromptRepository;
    private final ObjectMapper objectMapper;

    private static final String QUESTION_TYPE_SUBJECTIVE = "SUBJECTIVE";
    private static final String ANSWER_REQUIRED = "Y";

    private record FeedbackGenerationResult(
            String finalPrompt,
            String rawResponse,
            AiGeneratedFeedbackDTO parsedResponse,
            double actualTemperature
    ) {}

    private record FeedbackContext(
            Long coupleCardId,
            Long cardId,
            Long userId1,
            Long userId2,
            String cardTitle,
            String mode,
            String subject,
            List<QuestionData> questions,
            boolean hasSubjectiveAnswer,
            String rawCombination,
            String combinationHash
    ) {}

    private record QuestionData(
            Integer questionNo,
            String questionType,
            String questionCnts,
            boolean answerRequired,
            List<OptionData> options,
            String user1Answer,
            String user2Answer
    ) {
        boolean isSubjective() {
            return QUESTION_TYPE_SUBJECTIVE.equals(questionType);
        }
    }

    private record OptionData(
            Integer optionNo,
            String optionCnts,
            boolean user1Selected,
            boolean user2Selected
    ) {}

    @Override
    public FeedbackResponseDTO generateFeedback(Long userId, GenerateFeedbackRequestDTO requestDTO) {
        FeedbackContext context = loadFeedbackContext(userId, requestDTO);

        FeedbackResponseDTO existingResponse = claimGenerationOrReturnExisting(context.coupleCardId(), userId);
        if (existingResponse != null) {
            return existingResponse;
        }

        if (!context.hasSubjectiveAnswer()) {
            DailyCardFeedbackEntity cachedFeedback = dailyCardFeedbackRepository
                    .findByCardIdAndChoiceCombinationHashAndHasSubjectiveAndDelYn(
                            context.cardId(), context.combinationHash(), "N", "N")
                    .orElse(null);

            if (cachedFeedback != null) {
                return feedbackMappingService.markCompletedIfGenerating(
                        context.coupleCardId(), cachedFeedback.getFeedbackId(), userId);
            }
        }

        try {
            AiFeedbackConfigEntity config = feedbackConfigRepository
                    .findTopByDelYnOrderByConfigIdDesc("N")
                    .orElseThrow(() -> new IllegalStateException("피드백 생성 설정이 없습니다. Admin에서 설정해주세요."));

            if (config.getPromptId() == null) {
                throw new IllegalStateException("적용 중인 피드백 프롬프트가 없습니다. Admin에서 프롬프트를 설정해주세요.");
            }

            AiPromptEntity promptEntity = aiPromptRepository.findById(config.getPromptId())
                    .orElseThrow(() -> new IllegalStateException("설정된 프롬프트(ID: " + config.getPromptId() + ")를 찾을 수 없습니다."));

            String aiModel = config.getAiModel();
            double temperature = config.getTemperature().doubleValue();
            FeedbackGenerationResult feedbackResult = callAiForFeedback(context, promptEntity.getPromptContent(), aiModel, temperature);

            DailyCardFeedbackEntity feedback = saveFeedbackResultOrLoadExisting(userId, context, feedbackResult, aiModel, config.getPromptId());
            return feedbackMappingService.markCompletedIfGenerating(context.coupleCardId(), feedback.getFeedbackId(), userId);
        } catch (RuntimeException e) {
            feedbackMappingService.markFailedIfGenerating(context.coupleCardId(), e, userId);
            throw e;
        }
    }

    private FeedbackResponseDTO claimGenerationOrReturnExisting(Long coupleCardId, Long userId) {
        try {
            return feedbackMappingService.createGeneratingOrReturnExisting(coupleCardId, userId);
        } catch (DataIntegrityViolationException e) {
            log.warn("피드백 생성 매핑 중복 선점 감지, 기존 매핑 조회: coupleCardId={}", coupleCardId);
            return feedbackMappingService.retryClaimNotStartedOrReturnExisting(coupleCardId, userId);
        }
    }

    private DailyCardFeedbackEntity saveFeedbackResultOrLoadExisting(Long userId, FeedbackContext context,
                                                                     FeedbackGenerationResult feedbackResult,
                                                                     String aiModel, Long promptId) {
        try {
            return feedbackPersistenceService.saveFeedbackResult(new FeedbackPersistenceService.SaveFeedbackCommand(
                    userId,
                    context.cardId(),
                    context.combinationHash(),
                    context.rawCombination(),
                    context.hasSubjectiveAnswer() ? "Y" : "N",
                    feedbackResult.parsedResponse(),
                    aiModel,
                    promptId,
                    feedbackResult.finalPrompt(),
                    feedbackResult.rawResponse(),
                    feedbackResult.actualTemperature()
            ));
        } catch (DataIntegrityViolationException e) {
            log.warn("피드백 중복 저장 시도 감지, 기존 레코드 조회: cardId={}, hash={}",
                    context.cardId(), context.combinationHash());
            return feedbackPersistenceService.loadExistingFeedback(
                    context.cardId(),
                    context.combinationHash(),
                    context.hasSubjectiveAnswer() ? "Y" : "N");
        }
    }

    @Override
    public FeedbackResponseDTO getFeedback(Long userId, Long coupleCardId) {
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 연결이 되어있지 않습니다."));

        if (couple.getUserId2() == null) {
            throw new IllegalStateException("커플이 아닙니다.");
        }

        coupleDailyCardRepository.findByCoupleIdAndCoupleCardIdAndDelYn(
                        couple.getCoupleId(), coupleCardId, "N")
                .orElseThrow(() -> new IllegalStateException("접근 권한이 없거나 카드가 존재하지 않습니다."));

        return feedbackMappingService.getFeedbackResponseForGet(coupleCardId);
    }

    private FeedbackContext loadFeedbackContext(Long userId, GenerateFeedbackRequestDTO requestDTO) {
        Long cardId = requestDTO.getCardId();
        Long coupleCardId = requestDTO.getCoupleCardId();

        List<FeedbackDataProjection> rows = dailyCardFeedbackRepository.findFeedbackDataByCardAndUser(
                cardId, coupleCardId, requestDTO.getIssuedDate(), userId);

        if (rows.isEmpty()) {
            throw new IllegalStateException("접근 권한이 없거나 데이터가 존재하지 않습니다.");
        }

        FeedbackDataProjection first = rows.getFirst();
        Long userId1 = first.getUserId1();
        Long userId2 = first.getUserId2();

        // [TDTDBE-55] SOLO 커플(userId2가 NULL)은 피드백 생성 불가
        if (userId2 == null) {
            throw new IllegalStateException("커플 연결 후 이용 가능한 기능입니다.");
        }

        Map<Integer, List<FeedbackDataProjection>> byQuestion = rows.stream()
                .collect(Collectors.groupingBy(
                        FeedbackDataProjection::getQuestionNo,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<QuestionData> questions = new ArrayList<>();
        boolean hasSubjectiveAnswer = false;

        for (Map.Entry<Integer, List<FeedbackDataProjection>> entry : byQuestion.entrySet()) {
            List<FeedbackDataProjection> questionRows = entry.getValue();
            FeedbackDataProjection questionFirst = questionRows.getFirst();

            String questionType = questionFirst.getQuestionType();
            boolean isSubjective = QUESTION_TYPE_SUBJECTIVE.equals(questionType);
            boolean isAnswerRequired = ANSWER_REQUIRED.equals(questionFirst.getAnswerReqYn());

            if (isSubjective) {
                String user1Answer = questionFirst.getUser1Answer();
                String user2Answer = questionFirst.getUser2Answer();

                if (hasSubjectiveAnswer(user1Answer, user2Answer)) {
                    hasSubjectiveAnswer = true;
                }

                if (isAnswerRequired && (user1Answer == null || user2Answer == null)) {
                    throw new IllegalStateException("커플 두 명의 답변이 완료되지 않았습니다.");
                }

                questions.add(new QuestionData(
                        questionFirst.getQuestionNo(),
                        questionType,
                        questionFirst.getQuestionCnts(),
                        isAnswerRequired,
                        List.of(),
                        user1Answer,
                        user2Answer
                ));
            } else {
                List<OptionData> options = new ArrayList<>();
                String user1SelectedOption = null;
                String user2SelectedOption = null;

                for (FeedbackDataProjection row : questionRows) {
                    boolean user1Selected = "Y".equals(row.getUser1Answer());
                    boolean user2Selected = "Y".equals(row.getUser2Answer());

                    if (user1Selected) {
                        user1SelectedOption = String.valueOf(row.getOptionNo());
                    }
                    if (user2Selected) {
                        user2SelectedOption = String.valueOf(row.getOptionNo());
                    }

                    options.add(new OptionData(
                            row.getOptionNo(),
                            row.getOptionCnts(),
                            user1Selected,
                            user2Selected
                    ));
                }

                if (isAnswerRequired && (user1SelectedOption == null || user2SelectedOption == null)) {
                    throw new IllegalStateException("커플 두 명의 답변이 완료되지 않았습니다.");
                }

                questions.add(new QuestionData(
                        questionFirst.getQuestionNo(),
                        questionType,
                        questionFirst.getQuestionCnts(),
                        isAnswerRequired,
                        options,
                        user1SelectedOption,
                        user2SelectedOption
                ));
            }
        }

        String rawCombination = buildChoiceCombinationRaw(questions, userId1, userId2);
        String combinationHash = buildHash(rawCombination);

        return new FeedbackContext(
                coupleCardId,
                cardId,
                userId1,
                userId2,
                first.getCardTitle(),
                first.getMode(),
                first.getSubject(),
                questions,
                hasSubjectiveAnswer,
                rawCombination,
                combinationHash
        );
    }

    private String buildChoiceCombinationRaw(List<QuestionData> questions, Long userId1, Long userId2) {
        StringBuilder builder = new StringBuilder();

        for (QuestionData question : questions) {
            builder.append("Q").append(question.questionNo())
                    .append("|")
                    .append(question.questionCnts())
                    .append("|");

            for (OptionData option : question.options()) {
                builder.append(option.optionNo())
                        .append(":")
                        .append(option.optionCnts())
                        .append("|");
            }

            String user1Answer = question.user1Answer() != null ? question.user1Answer() : "";
            String user2Answer = question.user2Answer() != null ? question.user2Answer() : "";

            builder.append("U").append("=").append(user1Answer).append("|")
                    .append("U").append("=").append(user2Answer)
                    .append("\n");
        }

        return builder.toString();
    }

    private String buildHash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private FeedbackGenerationResult callAiForFeedback(FeedbackContext context, String promptContent, String aiModel, double temperature) {
        String dynamicContext = buildDynamicContext(context);
        String prompt = promptContent + "\n\n" + dynamicContext;

        ChatClient chatClient = chatClientBuilder.build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(aiModel)
                .temperature(temperature)
                .build();

        String response;
        try {
            response = chatClient.prompt()
                    .options(options)
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new IllegalStateException("AI 피드백 생성 호출에 실패했습니다.", e);
        }

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI 피드백 응답이 비어있습니다.");
        }

        try {
            String jsonContent = extractJsonFromResponse(response);
            AiGeneratedFeedbackDTO parsed = objectMapper.readValue(jsonContent, AiGeneratedFeedbackDTO.class);
            return new FeedbackGenerationResult(prompt, response, parsed, temperature);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("AI 피드백 응답을 파싱할 수 없습니다.", e);
        }
    }

    private String buildDynamicContext(FeedbackContext context) {
        StringBuilder builder = new StringBuilder();
        builder.append("[카드 정보]\n");
        builder.append("- 제목: ").append(context.cardTitle()).append("\n");
        builder.append("- 모드: ").append(context.mode()).append("\n");
        builder.append("- 주제: ").append(context.subject()).append("\n\n");

        for (QuestionData question : context.questions()) {
            builder.append("[질문 ").append(question.questionNo()).append("] ")
                    .append(question.questionCnts()).append("\n");

            if (!question.options().isEmpty()) {
                builder.append("선택지:\n");
                for (OptionData option : question.options()) {
                    builder.append("- ").append(option.optionNo()).append(": ")
                            .append(option.optionCnts()).append("\n");
                }
            }

            builder.append("유저1 답변: ").append(resolveAnswerText(question, question.user1Answer())).append("\n");
            builder.append("유저2 답변: ").append(resolveAnswerText(question, question.user2Answer())).append("\n\n");
        }

        return builder.toString();
    }

    private String resolveAnswerText(QuestionData question, String answer) {
        if (answer == null || answer.isBlank()) {
            return "-";
        }

        if (question.isSubjective()) {
            return answer;
        }

        for (OptionData option : question.options()) {
            if (answer.equals(String.valueOf(option.optionNo()))) {
                return option.optionNo() + ". " + option.optionCnts();
            }
        }

        return answer;
    }

    private String extractJsonFromResponse(String response) {
        int jsonStart = response.indexOf("```json");
        if (jsonStart != -1) {
            int start = jsonStart + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }

        int codeStart = response.indexOf("```");
        if (codeStart != -1) {
            int start = codeStart + 3;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }

        return response.trim();
    }

    private boolean hasSubjectiveAnswer(String user1Answer, String user2Answer) {
        return isNonEmpty(user1Answer) || isNonEmpty(user2Answer);
    }

    private boolean isNonEmpty(String value) {
        return value != null && !value.isBlank();
    }

}
