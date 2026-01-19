package com.todaktodot.TDTD.domain.feedback.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.domain.feedback.dto.ai.AiGeneratedFeedbackDTO;
import com.todaktodot.TDTD.domain.feedback.dto.reqeust.GenerateFeedbackRequestDTO;
import com.todaktodot.TDTD.domain.feedback.dto.response.GenerateFeedbackResponseDTO;
import com.todaktodot.TDTD.domain.feedback.repository.AiCardFeedbackInfoRepository;
import com.todaktodot.TDTD.domain.feedback.repository.CoupleDailyCardFeedbackRepository;
import com.todaktodot.TDTD.domain.feedback.repository.DailyCardFeedbackRepository;
import com.todaktodot.TDTD.domain.feedback.repository.entity.AiCardFeedbackInfoEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.CoupleDailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.FeedbackStatus;
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
    private final AiCardFeedbackInfoRepository aiCardFeedbackInfoRepository;
    private final CoupleDailyCardFeedbackRepository coupleDailyCardFeedbackRepository;
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
    public GenerateFeedbackResponseDTO generateFeedback(Long userId, GenerateFeedbackRequestDTO requestDTO) {
        FeedbackContext context = loadFeedbackContext(userId, requestDTO);

        if (!context.hasSubjectiveAnswer()) {
            DailyCardFeedbackEntity cachedFeedback = dailyCardFeedbackRepository
                    .findByCardIdAndChoiceCombinationHashAndHasSubjectiveAndDelYn(
                            context.cardId(), context.combinationHash(), "N", "N")
                    .orElse(null);

            if (cachedFeedback != null) {
                saveOrUpdateCoupleFeedbackMapping(context.coupleCardId(), cachedFeedback.getFeedbackId(), userId);
                return GenerateFeedbackResponseDTO.from(cachedFeedback);
            }
        }

        String aiModel = "gpt-4o-mini";
        double temperature = 0.7;
        FeedbackGenerationResult feedbackResult = callAiForFeedback(context, aiModel, temperature);

        return saveFeedbackResult(userId, context, feedbackResult, aiModel);
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

    private void saveOrUpdateCoupleFeedbackMapping(Long coupleCardId, Long feedbackId, Long userId) {
        coupleDailyCardFeedbackRepository.findByCoupleCardIdAndDelYn(coupleCardId, "N")
                .ifPresentOrElse(
                        mapping -> mapping.updateFeedback(feedbackId, userId),
                        () -> coupleDailyCardFeedbackRepository.save(
                                CoupleDailyCardFeedbackEntity.builder()
                                        .coupleCardId(coupleCardId)
                                        .feedbackId(feedbackId)
                                        .regrId(userId)
                                        .updrId(userId)
                                        .build()));
    }

    private GenerateFeedbackResponseDTO saveFeedbackResult(Long userId, FeedbackContext context,
                                                           FeedbackGenerationResult feedbackResult, String aiModel) {
        AiGeneratedFeedbackDTO aiFeedback = feedbackResult.parsedResponse();

        String matchPointsText = toText(aiFeedback.getMatchPoints());
        String differencesText = toText(aiFeedback.getDifferences());
        String hasSubjectiveFlag = context.hasSubjectiveAnswer() ? "Y" : "N";

        DailyCardFeedbackEntity feedback;
        try {
            feedback = dailyCardFeedbackRepository.save(
                    DailyCardFeedbackEntity.builder()
                            .cardId(context.cardId())
                            .choiceCombinationHash(context.combinationHash())
                            .choiceCombinationRaw(context.rawCombination())
                            .hasSubjective(hasSubjectiveFlag)
                            .summary(aiFeedback.getSummary())
                            .matchPoints(matchPointsText)
                            .differences(differencesText)
                            .conversationStarter(aiFeedback.getConversationStarter())
                            .regrId(userId)
                            .updrId(userId)
                            .build());
        } catch (DataIntegrityViolationException e) {
            log.warn("피드백 중복 저장 시도 감지, 기존 레코드 조회: cardId={}, hash={}",
                    context.cardId(), context.combinationHash());
            feedback = dailyCardFeedbackRepository
                    .findByCardIdAndChoiceCombinationHashAndHasSubjectiveAndDelYn(
                            context.cardId(), context.combinationHash(), hasSubjectiveFlag, "N")
                    .orElseThrow(() -> new IllegalStateException("피드백 저장에 실패했습니다.", e));

            saveOrUpdateCoupleFeedbackMapping(context.coupleCardId(), feedback.getFeedbackId(), userId);
            return GenerateFeedbackResponseDTO.from(feedback);
        }

        aiCardFeedbackInfoRepository.save(
                AiCardFeedbackInfoEntity.builder()
                        .feedbackId(feedback.getFeedbackId())
                        .aiModel(aiModel)
                        .temperature(String.valueOf(feedbackResult.actualTemperature()))
                        .finalPrompt(feedbackResult.finalPrompt())
                        .aiResponseRaw(feedbackResult.rawResponse())
                        .status(FeedbackStatus.SUCCESS.name())
                        .regrId(userId)
                        .updrId(userId)
                        .build());

        coupleDailyCardFeedbackRepository.save(
                CoupleDailyCardFeedbackEntity.builder()
                        .coupleCardId(context.coupleCardId())
                        .feedbackId(feedback.getFeedbackId())
                        .regrId(userId)
                        .updrId(userId)
                        .build());

        return GenerateFeedbackResponseDTO.from(feedback);
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

    private FeedbackGenerationResult callAiForFeedback(FeedbackContext context, String aiModel, double temperature) {
        String prompt = buildFeedbackPrompt(context);

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

    private String buildFeedbackPrompt(FeedbackContext context) {
        StringBuilder builder = new StringBuilder();
        builder.append("너는 커플의 관계를 돕는 상담가다. 다음 데일리카드 답변을 바탕으로 피드백을 작성해라.\n");
        builder.append("응답은 반드시 JSON 형식으로만 출력한다.\n");
        builder.append("{\n");
        builder.append("  \"summary\": \"...\",\n");
        builder.append("  \"match_points\": [\"...\"],\n");
        builder.append("  \"differences\": [\"...\"],\n");
        builder.append("  \"conversation_starter\": \"...\"\n");
        builder.append("}\n\n");

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

    private String toText(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream()
                .filter(this::isNonEmpty)
                .collect(Collectors.joining("\n"));
    }
}
