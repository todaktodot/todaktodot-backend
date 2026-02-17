package com.todaktodot.TDTD.domain.dailycard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.domain.dailycard.dto.ai.AiGeneratedCardDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.AssignCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.GenerateDailyCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.SelectCardTypeRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.SubmitAnswerRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignBatchResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignMyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.GenerateDailyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.SelectCardTypeResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.HistoryCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.HistoryDetailResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.SubmitAnswerResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.WeeklyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.projection.HistoryCardProjection;
import com.todaktodot.TDTD.domain.dailycard.repository.projection.HistoryDetailProjection;
import com.todaktodot.TDTD.domain.dailycard.repository.projection.WeeklyCardProjection;
import com.todaktodot.TDTD.domain.feedback.repository.CoupleDailyCardFeedbackRepository;
import com.todaktodot.TDTD.domain.feedback.repository.DailyCardFeedbackRepository;
import com.todaktodot.TDTD.domain.feedback.repository.entity.CoupleDailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.AiCardGenerationInfoRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.CoupleDailyCardRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardUserAnswerRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardOptionRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardQuestionRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.*;

import java.math.BigDecimal;
import com.todaktodot.TDTD.admin.prompt.repository.AiPromptRepository;
import com.todaktodot.TDTD.admin.prompt.repository.SituationCategoryRepository;
import com.todaktodot.TDTD.admin.prompt.repository.entity.AiPromptEntity;
import com.todaktodot.TDTD.admin.prompt.repository.entity.SituationCategoryEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyCardServiceImpl implements DailyCardService {

    private final ChatClient.Builder chatClientBuilder;
    private final DailyCardRepository dailyCardRepository;
    private final DailyCardQuestionRepository dailyCardQuestionRepository;
    private final DailyCardOptionRepository dailyCardOptionRepository;
    private final DailyCardUserAnswerRepository dailyCardUserAnswerRepository;
    private final CoupleDailyCardRepository coupleDailyCardRepository;
    private final CoupleRepository coupleRepository;
    private final AiPromptRepository aiPromptRepository;
    private final SituationCategoryRepository situationCategoryRepository;
    private final AiCardGenerationInfoRepository aiCardGenerationInfoRepository;
    private final CoupleDailyCardFeedbackRepository coupleDailyCardFeedbackRepository;
    private final DailyCardFeedbackRepository dailyCardFeedbackRepository;
    private final ObjectMapper objectMapper;

    private static final Long SYSTEM_USER = 0L;

    /**
     * AI 생성 결과를 담는 record
     * 프롬프트, 응답 원문, 파싱된 응답, 실제 사용된 온도 등을 포함
     */
    private record AiGenerationResult(
            String finalPrompt,
            String rawResponse,
            AiGeneratedCardDTO parsedResponse,
            String situationCategory,
            double actualTemperature
    ) {}

    /**
     * 커플 단위 배정 결과를 담는 record
     */
    private record CoupleAssignResult(int assignedCount, int skippedDateCount) {}

    @Override
    @Transactional
    public GenerateDailyCardResponseDTO generateDailyCard(GenerateDailyCardRequestDTO requestDTO) {
        CardMode mode = requestDTO.getMode();
        CardSubject subject = requestDTO.getSubject();
        CardType type = requestDTO.getType();
        Long promptId = requestDTO.getPromptId();
        String situationCategory = requestDTO.getSituationCategory();
        String aiModel = requestDTO.getAiModel();
        Double temperature = requestDTO.getTemperature();

        // 프롬프트 ID 필수 검증
        if (promptId == null) {
            throw new IllegalArgumentException("프롬프트를 선택해주세요.");
        }

        // AI 모델 기본값 설정
        if (aiModel == null || aiModel.isBlank()) {
            aiModel = "gpt-4o-mini";
        }

        // temperature 기본값 및 범위 검증
        if (temperature == null) {
            temperature = 0.8;
        } else if (temperature < 0.0 || temperature > 2.0) {
            throw new IllegalArgumentException("온도는 0.0 ~ 2.0 범위여야 합니다.");
        }

        log.info("========================================");
        log.info("데일리카드 AI 생성 시작");
        log.info("모드: {}, 주제: {}, 유형: {}, 프롬프트ID: {}, AI모델: {}, 온도: {}", mode, subject, type, promptId, aiModel, temperature);
        log.info("========================================");

        // 1. AI 호출하여 콘텐츠 생성 (프롬프트, 응답 원문 포함)
        AiGenerationResult aiResult = callAiForCardGeneration(mode, subject, type, promptId, situationCategory, aiModel, temperature);
        AiGeneratedCardDTO aiResponse = aiResult.parsedResponse();

        // 2. DailyCard 엔티티 생성 및 저장
        DailyCardEntity dailyCard = DailyCardEntity.builder()
                .mode(mode)
                .subject(subject)
                .type(type)
                .cardTitle(aiResponse.getCardTitle())
                .situation(aiResult.situationCategory())
                .regrId(SYSTEM_USER)
                .updrId(SYSTEM_USER)
                .build();

        DailyCardEntity savedCard = dailyCardRepository.save(dailyCard);
        Long cardId = savedCard.getCardId();

        log.info("데일리카드 저장 완료: cardId={}", cardId);

        // 3. AI 생성 정보 저장
        AiCardGenerationInfoEntity generationInfo = AiCardGenerationInfoEntity.builder()
                .cardId(cardId)
                .promptId(promptId)
                .aiModel(aiModel)
                .temperature(BigDecimal.valueOf(aiResult.actualTemperature()))
                .mode(mode)
                .subject(subject)
                .type(type)
                .situationCategory(aiResult.situationCategory())
                .finalPrompt(aiResult.finalPrompt())
                .aiResponse(aiResult.rawResponse())
                .regrId(SYSTEM_USER)
                .build();
        aiCardGenerationInfoRepository.save(generationInfo);

        log.info("AI 생성 정보 저장 완료: infoId={}", generationInfo.getInfoId());

        // 4. Question 및 Option 저장 (수동 save만 사용, Cascade 충돌 방지)
        for (AiGeneratedCardDTO.AiQuestionDTO aiQuestion : aiResponse.getQuestions()) {
            DailyCardQuestionEntity question = DailyCardQuestionEntity.builder()
                    .cardId(cardId)
                    .questionNo(aiQuestion.getQuestionNo())
                    .questionType(QuestionType.valueOf(aiQuestion.getQuestionType()))
                    .answerReqYn(aiQuestion.getAnswerRequired() ? "Y" : "N")
                    .questionCnts(aiQuestion.getContent())
                    .regrId(SYSTEM_USER)
                    .updrId(SYSTEM_USER)
                    .build();

            DailyCardQuestionEntity savedQuestion = dailyCardQuestionRepository.save(question);

            // 객관식인 경우 옵션 저장
            if (aiQuestion.getOptions() != null && !aiQuestion.getOptions().isEmpty()) {
                for (AiGeneratedCardDTO.AiOptionDTO aiOption : aiQuestion.getOptions()) {
                    DailyCardOptionEntity option = DailyCardOptionEntity.builder()
                            .cardId(cardId)
                            .questionNo(aiQuestion.getQuestionNo())
                            .optionNo(aiOption.getOptionNo())
                            .optionCnts(aiOption.getContent())
                            .regrId(SYSTEM_USER)
                            .updrId(SYSTEM_USER)
                            .build();

                    dailyCardOptionRepository.save(option);
                }
            }
        }

        log.info("========================================");
        log.info("데일리카드 AI 생성 완료: cardId={}", cardId);
        log.info("========================================");

        // AI 응답 데이터를 기반으로 응답 DTO 생성
        return GenerateDailyCardResponseDTO.of(savedCard, aiResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GenerateDailyCardResponseDTO getDailyCard(Long cardId) {
        DailyCardEntity dailyCard = dailyCardRepository.findByIdWithQuestionsAndOptions(cardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다: " + cardId));

        return GenerateDailyCardResponseDTO.from(dailyCard);
    }

    @Override
    @Transactional
    public SubmitAnswerResponseDTO submitAnswer(Long userId, SubmitAnswerRequestDTO requestDTO) {
        Long coupleCardId = requestDTO.getCoupleCardId();
        Long cardId = requestDTO.getCardId();

        // 카드 존재 여부 확인
        if (!dailyCardRepository.existsById(cardId)) {
            throw new IllegalArgumentException("존재하지 않는 카드입니다: " + cardId);
        }

        // 사용자의 커플 정보 조회
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 연결이 되어있지 않습니다."));

        // 해당 커플에게 할당된 카드인지 확인
        CoupleDailyCardEntity coupleCard = coupleDailyCardRepository.findById(coupleCardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커플 카드입니다: " + coupleCardId));

        if (!coupleCard.getCoupleId().equals(couple.getCoupleId())) {
            throw new IllegalStateException("해당 카드에 대한 접근 권한이 없습니다.");
        }

        // 이미 답변했는지 확인
        if (dailyCardUserAnswerRepository.existsByCoupleCardIdAndUserIdAndDelYn(coupleCardId, userId, "N")) {
            throw new IllegalStateException("이미 답변 작성이 완료된 카드입니다.");
        }

        log.info("데일리카드 답변 제출 시작: coupleCardId={}, cardId={}, userId={}", coupleCardId, cardId, userId);

        List<SubmitAnswerResponseDTO.SavedAnswer> savedAnswers = new ArrayList<>();

        for (SubmitAnswerRequestDTO.AnswerItem answerItem : requestDTO.getAnswers()) {
            Integer questionNo = answerItem.getQuestionNo();

            DailyCardUserAnswerEntity answer = DailyCardUserAnswerEntity.builder()
                    .coupleCardId(coupleCardId)
                    .cardId(cardId)
                    .questionNo(questionNo)
                    .userId(userId)
                    .answerContent(answerItem.getAnswerContent())
                    .regrId(userId)
                    .updrId(userId)
                    .build();

            DailyCardUserAnswerEntity savedAnswer = dailyCardUserAnswerRepository.save(answer);

            savedAnswers.add(SubmitAnswerResponseDTO.SavedAnswer.builder()
                    .answerId(savedAnswer.getAnswerId())
                    .questionNo(questionNo)
                    .answerContent(answerItem.getAnswerContent())
                    .build());
        }

        log.info("데일리카드 답변 저장 완료: coupleCardId={}, userId={}, 저장된 답변 수={}", coupleCardId, userId, savedAnswers.size());

        return SubmitAnswerResponseDTO.of(coupleCardId, cardId, userId, savedAnswers);
    }

    @Override
    @Transactional
    public AssignCardResponseDTO assignCardToCouple(Long userId, AssignCardRequestDTO requestDTO) {
        Long coupleId = requestDTO.getCoupleId();
        Long cardId = requestDTO.getCardId();

        // 카드 존재 여부 확인
        if (!dailyCardRepository.existsById(cardId)) {
            throw new IllegalArgumentException("존재하지 않는 카드입니다: " + cardId);
        }

        // 해당 커플에게 같은 날짜에 이미 카드가 2개 할당되어 있는지 확인
        if (coupleDailyCardRepository.countByCoupleIdAndIssuedDateAndDelYn(
                coupleId, requestDTO.getIssuedDate(), "N") >= 2) {
            throw new IllegalStateException("해당 날짜에 이미 카드가 모두 할당되어 있습니다.");
        }

        log.info("커플에게 데일리카드 할당 시작: coupleId={}, cardId={}, issuedDate={}",
                coupleId, cardId, requestDTO.getIssuedDate());

        CoupleDailyCardEntity coupleCard = CoupleDailyCardEntity.builder()
                .coupleId(coupleId)
                .cardId(cardId)
                .issuedDate(requestDTO.getIssuedDate())
                .regrId(userId)
                .updrId(userId)
                .build();

        CoupleDailyCardEntity savedCard = coupleDailyCardRepository.save(coupleCard);

        log.info("커플에게 데일리카드 할당 완료: coupleCardId={}", savedCard.getCoupleCardId());

        return AssignCardResponseDTO.from(savedCard);
    }

    @Override
    @Transactional
    public AssignBatchResponseDTO assignDailyCardsForCouples(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("배정 시작일과 종료일은 필수입니다.");
        }

        long rangeDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (rangeDays < 0 || rangeDays > 6) {
            throw new IllegalArgumentException("배정 기간은 시작일 기준 1~7일(같은 날 포함)이어야 합니다.");
        }

        int days = (int) rangeDays + 1;

        List<CoupleEntity> couples = coupleRepository.findByDelYn("N");
        log.info("데일리카드 배정 시작: startDate={}, endDate={}, days={}, coupleCount={}",
                startDate, endDate, days, couples.size());
        int assignedCount = 0;
        int skippedDateCount = 0;

        for (CoupleEntity couple : couples) {
            CoupleAssignResult result = assignCardsForCouple(
                    couple.getCoupleId(), startDate, endDate, SYSTEM_USER);
            assignedCount += result.assignedCount();
            skippedDateCount += result.skippedDateCount();
        }

        log.info("데일리카드 배정 완료: startDate={}, endDate={}, days={}, assignedCount={}, skippedDateCount={}",
                startDate, endDate, days, assignedCount, skippedDateCount);

        return AssignBatchResponseDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .days(days)
                .coupleCount(couples.size())
                .assignedCount(assignedCount)
                .skippedDateCount(skippedDateCount)
                .build();
    }

    @Override
    @Transactional
    public AssignMyCardResponseDTO assignMyDailyCards(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("배정 시작일과 종료일은 필수입니다.");
        }

        long rangeDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (rangeDays < 0 || rangeDays > 6) {
            throw new IllegalArgumentException("배정 기간은 시작일 기준 1~7일(같은 날 포함)이어야 합니다.");
        }

        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 연결이 되어있지 않습니다."));

        CoupleAssignResult result = assignCardsForCouple(
                couple.getCoupleId(), startDate, endDate, userId);

        return AssignMyCardResponseDTO.builder()
                .coupleId(couple.getCoupleId())
                .startDate(startDate)
                .endDate(endDate)
                .assignedCount(result.assignedCount())
                .skippedDateCount(result.skippedDateCount())
                .build();
    }

    /**
     * 커플 단위 데일리카드 배정 (배치/실시간 공용)
     * 이미 배정된 날짜는 스킵
     */
    private CoupleAssignResult assignCardsForCouple(Long coupleId, LocalDate startDate,
                                                     LocalDate endDate, Long registratorId) {
        int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        Set<Long> answeredCardIds = new HashSet<>(
                dailyCardUserAnswerRepository.findAnsweredCardIdsByCoupleId(coupleId)
        );
        int assignedCount = 0;
        int skippedDateCount = 0;

        log.info("커플 배정 시작: coupleId={}, answeredCardCount={}", coupleId, answeredCardIds.size());

        var lastAssignment = coupleDailyCardRepository
                .findTopByCoupleIdAndDelYnOrderByIssuedDateDesc(coupleId, "N");

        CardMode nextMode = CardMode.DESSERT;
        CardSubject lastSubject = null;

        if (lastAssignment.isPresent()) {
            CoupleDailyCardEntity lastCard = lastAssignment.get();
            LocalDate lastIssuedDate = lastCard.getIssuedDate();
            if (lastCard.getDailyCard() != null) {
                CardMode lastMode = lastCard.getDailyCard().getMode();
                nextMode = advanceMode(lastMode, 1);

                if (lastIssuedDate.equals(startDate) || lastIssuedDate.equals(startDate.minusDays(1))) {
                    lastSubject = lastCard.getDailyCard().getSubject();
                }
            }
        }

        for (int dayIndex = 0; dayIndex < days; dayIndex++) {
            LocalDate targetDate = startDate.plusDays(dayIndex);
            if (coupleDailyCardRepository.countByCoupleIdAndIssuedDateAndDelYn(coupleId, targetDate, "N") > 0) {
                List<CoupleDailyCardEntity> existingAssignments = coupleDailyCardRepository
                        .findAllByCoupleIdAndIssuedDateAndDelYnOrderByCoupleCardIdAsc(coupleId, targetDate, "N");
                if (!existingAssignments.isEmpty() && existingAssignments.get(0).getDailyCard() != null) {
                    // 스킵된 날의 모드를 기준으로 다음 모드 계산 (버그 수정)
                    CardMode skippedMode = existingAssignments.get(0).getDailyCard().getMode();
                    nextMode = advanceMode(skippedMode, 1);
                    lastSubject = existingAssignments.get(0).getDailyCard().getSubject();
                }
                skippedDateCount++;
                log.info("커플 배정 스킵: coupleId={}, targetDate={}, reason=alreadyAssigned",
                        coupleId, targetDate);
                continue;
            }

            CardMode modeForDate = nextMode;
            nextMode = advanceMode(nextMode, 1);  // 다음 날을 위해 모드 전진
            CardSubject subjectForDate = pickSubject(lastSubject);

            DailyCardEntity roleplayCard = pickCard(modeForDate, subjectForDate, CardType.ROLEPLAY, answeredCardIds);
            DailyCardEntity balanceCard = pickCard(modeForDate, subjectForDate, CardType.BALANCE, answeredCardIds);

            coupleDailyCardRepository.save(CoupleDailyCardEntity.builder()
                    .coupleId(coupleId)
                    .cardId(roleplayCard.getCardId())
                    .issuedDate(targetDate)
                    .regrId(registratorId)
                    .updrId(registratorId)
                    .build());

            coupleDailyCardRepository.save(CoupleDailyCardEntity.builder()
                    .coupleId(coupleId)
                    .cardId(balanceCard.getCardId())
                    .issuedDate(targetDate)
                    .regrId(registratorId)
                    .updrId(registratorId)
                    .build());

            assignedCount += 2;
            lastSubject = subjectForDate;

            log.info("커플 배정 완료: coupleId={}, targetDate={}, mode={}, subject={}, roleplayCardId={}, balanceCardId={}",
                    coupleId, targetDate, modeForDate, subjectForDate,
                    roleplayCard.getCardId(), balanceCard.getCardId());
        }

        log.info("커플 배정 요약: coupleId={}, assignedCount={}, skippedCount={}",
                coupleId, assignedCount, skippedDateCount);

        return new CoupleAssignResult(assignedCount, skippedDateCount);
    }

    private AiGenerationResult callAiForCardGeneration(CardMode mode, CardSubject subject, CardType type,
                                                        Long promptId, String situationCategory, String aiModel, Double temperature) {
        String category = (situationCategory != null && !situationCategory.isBlank())
                ? situationCategory
                : getRandomSituationCategory(subject);
        int randomSeed = (int) (Math.random() * 1000);

        String prompt = buildPrompt(mode, subject, type, category, randomSeed, promptId);

        // 추론 모델(o1, o3, gpt-5 시리즈)은 temperature 미지원 - 1로 고정
        boolean isReasoningModel = isReasoningModel(aiModel);
        double actualTemperature = temperature;
        if (isReasoningModel) {
            actualTemperature = 1.0;
            log.info("추론 모델 감지: temperature를 1.0으로 고정");
        }

        log.info("AI 프롬프트 생성 완료, API 호출 시작... (모델: {}, 온도: {})", aiModel, actualTemperature);

        ChatClient chatClient = chatClientBuilder.build();

        // 추론 모델은 temperature를 설정하지 않음 (기본값 1 사용)
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder().model(aiModel);
        if (!isReasoningModel) {
            optionsBuilder.temperature(actualTemperature);
        }

        String response = chatClient.prompt()
                .options(optionsBuilder.build())
                .user(prompt)
                .call()
                .content();

        log.info("AI 응답 수신 완료");
        log.debug("AI 응답 내용: {}", response);

        // JSON 파싱
        try {
            // AI 응답에서 JSON 부분만 추출 (마크다운 코드블록 제거)
            String jsonContent = extractJsonFromResponse(response);
            AiGeneratedCardDTO parsedResponse = objectMapper.readValue(jsonContent, AiGeneratedCardDTO.class);

            return new AiGenerationResult(prompt, response, parsedResponse, category, actualTemperature);
        } catch (JsonProcessingException e) {
            log.error("AI 응답 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("AI 응답을 파싱할 수 없습니다", e);
        }
    }

    private String extractJsonFromResponse(String response) {
        // 마크다운 코드블록에서 JSON 추출
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        return response.trim();
    }

    /**
     * 프롬프트 미리보기
     * AI 생성 전 최종 프롬프트를 확인할 수 있도록 제공
     */
    @Override
    public String previewPrompt(CardMode mode, CardSubject subject, CardType type, String situationCategory) {
        return previewPrompt(mode, subject, type, situationCategory, null);
    }

    /**
     * 프롬프트 미리보기 (promptId 포함)
     */
    public String previewPrompt(CardMode mode, CardSubject subject, CardType type,
                                String situationCategory, Long promptId) {
        String category = (situationCategory != null && !situationCategory.isBlank())
                ? situationCategory
                : getRandomSituationCategory(subject);
        int randomSeed = (int) (Math.random() * 1000);

        return buildPrompt(mode, subject, type, category, randomSeed, promptId);
    }

    /**
     * 프롬프트 미리보기 - 영역별 분리
     * 프론트에서 각 영역을 다른 색상으로 표시할 수 있도록 분리해서 반환
     */
    public Map<String, String> previewPromptSeparated(CardMode mode, CardSubject subject, CardType type,
                                                      String situationCategory, Long promptId) {
        String category = (situationCategory != null && !situationCategory.isBlank())
                ? situationCategory
                : getRandomSituationCategory(subject);
        int randomSeed = (int) (Math.random() * 1000);

        String prefix = buildSystemPrefix(mode, subject, type, category, randomSeed);
        String admin = getAdminPromptFromDB(promptId);
        String suffix = buildSystemSuffix(type.getOptionCount());

        return java.util.Map.of(
                "prefix", prefix,
                "admin", admin,
                "suffix", suffix
        );
    }

    /**
     * 3단 분리 구조로 프롬프트 생성
     * 1. System Prefix: 역할 정의 + 동적 조건 (하드코딩)
     * 2. Admin Prompt: 스타일 가이드 (DB)
     * 3. System Suffix: 출력 형식 + 주의사항 (하드코딩)
     */
    private String buildPrompt(CardMode mode, CardSubject subject, CardType type,
                               String situationCategory, int randomSeed, Long promptId) {
        String prefix = buildSystemPrefix(mode, subject, type, situationCategory, randomSeed);
        String adminPrompt = getAdminPromptFromDB(promptId);
        String suffix = buildSystemSuffix(type.getOptionCount());

        return prefix + "\n\n" + adminPrompt + "\n\n" + suffix;
    }

    /**
     * System Prefix: 역할 정의 + 동적 조건 (하드코딩 - 서버에서 주입)
     */
    private String buildSystemPrefix(CardMode mode, CardSubject subject, CardType type,
                                     String situationCategory, int randomSeed) {
        int optionCount = type.getOptionCount();
        String typeDescription = type == CardType.ROLEPLAY
                ? "특정 상황을 제시하고, 그 상황에서 어떻게 행동할지 묻는 상황극"
                : "A vs B 중 하나를 선택하는 밸런스게임";

        return String.format("""
            너는 커플의 깊이 있는 대화를 돕는 '데일리 질문 큐레이터'이자 '관계 심리 분석가'이다. \
            가벼운 취향부터 묵직한 가치관까지 3단계(디저트, 커피, 위스키)의 난이도로 질문을 설계하고, \
            사용자의 답변에 대해 담백하면서도 통찰력 있는 피드백을 제공해.

            [랜덤 시드: %d - 이 숫자를 참고하여 매번 완전히 새로운 상황을 만들어주세요]

            [조건]
            - 질문 모드: %s (%s)
            - 질문 주제: %s (%s)
            - 질문 유형: %s (%s)
            - 객관식 선택지 개수: %d개
            - 상황 카테고리: %s
            """,
                randomSeed,
                mode.getDisplayName(), mode.getDescription(),
                subject.getDisplayName(), subject.getDescription(),
                type.getDisplayName(), typeDescription,
                optionCount,
                situationCategory
        );
    }

    /**
     * Admin Prompt: DB에서 가져오는 프롬프트 (어드민이 관리)
     * promptId는 필수, 해당 프롬프트가 없거나 비활성화된 경우 예외 발생
     */
    private String getAdminPromptFromDB(Long promptId) {
        if (promptId == null) {
            throw new IllegalArgumentException("프롬프트 ID는 필수입니다.");
        }

        return aiPromptRepository.findById(promptId)
                .filter(p -> "Y".equals(p.getUseYn()) && "N".equals(p.getDelYn()))
                .map(AiPromptEntity::getPromptContent)
                .orElseThrow(() -> new IllegalArgumentException("유효한 프롬프트가 없습니다. ID: " + promptId));
    }

    /**
     * System Suffix: 출력 형식 + 주의사항 (하드코딩)
     */
    private String buildSystemSuffix(int optionCount) {
        return String.format("""
            [출력 형식 - 반드시 아래 JSON 형식으로만 응답]
            ```json
            {
              "cardTitle": "메인 상황 설명 또는 질문",
              "questions": [
                {
                  "questionNo": 1,
                  "questionType": "MULTIPLE_CHOICE",
                  "answerRequired": true,
                  "content": "객관식 질문 내용",
                  "options": [
                    {"optionNo": 1, "content": "선택지 1"},
                    {"optionNo": 2, "content": "선택지 2"}
                  ]
                },
                {
                  "questionNo": 2,
                  "questionType": "SUBJECTIVE",
                  "answerRequired": false,
                  "content": "그렇게 생각한 이유는 무엇인가요?",
                  "options": []
                }
              ]
            }
            ```

            [주의사항]
            - 첫 번째 질문은 반드시 객관식(MULTIPLE_CHOICE)이며 필수 응답
            - 두 번째 질문은 주관식(SUBJECTIVE)이며 선택 응답
            - 객관식 선택지는 정확히 %d개
            - 한국어로 작성
            - 담백하고 현실적인 톤으로 작성
            - JSON 외에 다른 텍스트를 포함하지 말 것
            """, optionCount);
    }

    /**
     * DB에서 상황 카테고리 가져오기, 없으면 기본값 사용
     */
    private String getRandomSituationCategory(CardSubject subject) {
        List<SituationCategoryEntity> categories = situationCategoryRepository.findActiveBySubject(subject);

        if (!categories.isEmpty()) {
            int randomIndex = (int) (Math.random() * categories.size());
            return categories.get(randomIndex).getCategoryName();
        }

        // DB에 카테고리가 없으면 기본값 사용
        return getDefaultCategory(subject);
    }

    private String getDefaultCategory(CardSubject subject) {
        String[] categories = switch (subject) {
            case LOVE -> new String[]{
                "기념일/이벤트", "질투/불안", "연락 빈도", "친구/가족 소개", "미래 계획",
                "싸움 후 화해", "서프라이즈", "고민 상담", "스킨십", "취미 공유"
            };
            case ECONOMY -> new String[]{
                "외식비", "공과금/생활비", "선물 예산", "저축/투자", "경조사비",
                "구독 서비스", "취미 지출", "여행 경비", "이사/인테리어", "용돈/개인지출"
            };
            case LIFESTYLE -> new String[]{
                "집안일 분담", "반려동물", "식사/요리", "수면 습관", "주말 계획",
                "운동/건강", "청소/정리", "TV/넷플릭스", "친구 만남", "귀가 시간"
            };
        };
        return categories[(int) (Math.random() * categories.length)];
    }

    /**
     * 추론 모델 여부 확인
     * 추론 모델(o1, o3, gpt-5 시리즈)은 temperature 등 샘플링 파라미터를 지원하지 않음
     */
    private boolean isReasoningModel(String modelId) {
        if (modelId == null) return false;
        String lowerModel = modelId.toLowerCase();
        return lowerModel.startsWith("o1") ||
               lowerModel.startsWith("o3") ||
               lowerModel.startsWith("o4") ||
               lowerModel.startsWith("gpt-5");
    }

    private DailyCardEntity pickCard(CardMode mode, CardSubject subject, CardType type, Set<Long> answeredCardIds) {
        List<DailyCardEntity> candidates = dailyCardRepository
                .findByModeAndSubjectAndTypeAndUseYnAndDelYn(mode, subject, type, "Y", "N");

        List<DailyCardEntity> available = new ArrayList<>();
        for (DailyCardEntity card : candidates) {
            if (!answeredCardIds.contains(card.getCardId())) {
                available.add(card);
            }
        }

        if (available.isEmpty()) {
            throw new IllegalStateException("사용 가능한 데일리카드가 없습니다. mode=" + mode + ", subject=" + subject + ", type=" + type);
        }

        int index = ThreadLocalRandom.current().nextInt(available.size());
        return available.get(index);
    }

    private CardSubject pickSubject(CardSubject lastSubject) {
        List<CardSubject> subjects = new ArrayList<>(Arrays.asList(CardSubject.values()));
        if (lastSubject != null) {
            subjects.remove(lastSubject);
        }
        int index = ThreadLocalRandom.current().nextInt(subjects.size());
        return subjects.get(index);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyCardResponseDTO getWeeklyCards(Long userId, LocalDate startDate, LocalDate endDate) {
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 연결이 되어있지 않습니다."));

        List<WeeklyCardProjection> rows = coupleDailyCardRepository
                .findWeeklyCardsWithDetails(couple.getCoupleId(), startDate, endDate);

        // flat rows → 중첩 DTO 변환 (coupleCardId → questionNo → options)
        LinkedHashMap<Long, List<WeeklyCardProjection>> cardGroups = new LinkedHashMap<>();
        for (WeeklyCardProjection row : rows) {
            cardGroups.computeIfAbsent(row.getCoupleCardId(), k -> new ArrayList<>()).add(row);
        }

        List<WeeklyCardResponseDTO.DailyCardItem> dailyCards = new ArrayList<>();
        for (var entry : cardGroups.entrySet()) {
            List<WeeklyCardProjection> cardRows = entry.getValue();
            WeeklyCardProjection first = cardRows.get(0);

            LinkedHashMap<Integer, List<WeeklyCardProjection>> questionGroups = new LinkedHashMap<>();
            for (WeeklyCardProjection row : cardRows) {
                questionGroups.computeIfAbsent(row.getQuestionNo(), k -> new ArrayList<>()).add(row);
            }

            List<WeeklyCardResponseDTO.QuestionItem> questions = new ArrayList<>();
            for (var qEntry : questionGroups.entrySet()) {
                List<WeeklyCardProjection> qRows = qEntry.getValue();
                WeeklyCardProjection qFirst = qRows.get(0);

                List<WeeklyCardResponseDTO.OptionItem> options = new ArrayList<>();
                for (WeeklyCardProjection qRow : qRows) {
                    if (qRow.getOptionNo() != null) {
                        options.add(WeeklyCardResponseDTO.OptionItem.builder()
                                .optionNo(qRow.getOptionNo())
                                .optionCnts(qRow.getOptionCnts())
                                .build());
                    }
                }

                questions.add(WeeklyCardResponseDTO.QuestionItem.builder()
                        .questionNo(qFirst.getQuestionNo())
                        .questionType(qFirst.getQuestionType())
                        .questionCnts(qFirst.getQuestionCnts())
                        .answerReqYn(qFirst.getAnswerReqYn())
                        .options(options)
                        .build());
            }

            dailyCards.add(WeeklyCardResponseDTO.DailyCardItem.builder()
                    .coupleCardId(first.getCoupleCardId())
                    .cardId(first.getCardId())
                    .issuedDate(first.getIssuedDate())
                    .cardTitle(first.getCardTitle())
                    .mode(first.getMode())
                    .subject(first.getSubject())
                    .type(first.getType())
                    .situation(first.getSituation())
                    .questions(questions)
                    .build());
        }

        return WeeklyCardResponseDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dailyCards(dailyCards)
                .build();
    }

    @Override
    @Transactional
    public SelectCardTypeResponseDTO selectCardType(Long userId, SelectCardTypeRequestDTO request) {
        // 1. userId → coupleId 조회
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 연결이 되어있지 않습니다."));

        // 2. 선택할 카드 조회 + dailyCard EAGER 로드
        CoupleDailyCardEntity selectedCard = coupleDailyCardRepository
                .findByIdWithDailyCard(request.getCoupleCardId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 커플 카드입니다: " + request.getCoupleCardId()));

        // 3. 접근 권한 검증
        if (!selectedCard.getCoupleId().equals(couple.getCoupleId())) {
            throw new IllegalStateException("해당 카드에 대한 접근 권한이 없습니다.");
        }

        // 4. 이미 유형 선택이 완료된 경우 차단
        if ("Y".equals(selectedCard.getSelectedYn())) {
            throw new IllegalStateException("이미 유형이 선택된 카드입니다: " + request.getCoupleCardId());
        }

        // 5. 선택 카드: markSelected 처리
        CardType selectedType = selectedCard.getDailyCard().getType();
        selectedCard.markSelected(userId, selectedType);

        // 6. 같은 날짜의 나머지 카드 soft delete 처리
        List<CoupleDailyCardEntity> siblingCards = coupleDailyCardRepository
                .findAllByCoupleIdAndIssuedDateAndDelYn(
                        couple.getCoupleId(), selectedCard.getIssuedDate(), "N");

        siblingCards.stream()
                .filter(card -> !card.getCoupleCardId().equals(request.getCoupleCardId()))
                .forEach(card -> card.softDelete(userId));

        return SelectCardTypeResponseDTO.from(selectedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryCardResponseDTO getHistoryCards(Long userId, LocalDate startDate, LocalDate endDate) {
        // 1. userId → CoupleEntity (coupleId, userId1, userId2 획득)
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 연결이 되어있지 않습니다."));

        // 2. 네이티브 쿼리 한 방 조회 (카드 마스터 + user1/user2 답변 여부)
        List<HistoryCardProjection> rows = coupleDailyCardRepository.findHistoryCards(
                couple.getCoupleId(), couple.getUserId1(), couple.getUserId2(),
                startDate, endDate);

        // 3. issuedDate별 그룹핑 (LinkedHashMap으로 순서 보장)
        LinkedHashMap<LocalDate, List<HistoryCardProjection>> dateGroups = new LinkedHashMap<>();
        for (HistoryCardProjection row : rows) {
            dateGroups.computeIfAbsent(row.getIssuedDate(), k -> new ArrayList<>()).add(row);
        }

        // 4. 각 일자별로 선택/미선택 분기하여 DTO 조립
        List<HistoryCardResponseDTO.HistoryCardItem> historyCards = new ArrayList<>();
        for (var entry : dateGroups.entrySet()) {
            List<HistoryCardProjection> dayCards = entry.getValue();

            // selectedYn='Y'인 카드 찾기
            HistoryCardProjection selectedCard = dayCards.stream()
                    .filter(c -> "Y".equals(c.getSelectedYn()))
                    .findFirst()
                    .orElse(null);

            if (selectedCard != null) {
                // 선택 완료: 전체 정보 노출
                historyCards.add(HistoryCardResponseDTO.HistoryCardItem.builder()
                        .issuedDate(selectedCard.getIssuedDate())
                        .mode(selectedCard.getMode())
                        .subject(selectedCard.getSubject())
                        .selected(true)
                        .coupleCardId(selectedCard.getCoupleCardId())
                        .cardId(selectedCard.getCardId())
                        .type(selectedCard.getType())
                        .situation(selectedCard.getSituation())
                        .user1Answered(selectedCard.getUser1Answered() != null && selectedCard.getUser1Answered() == 1L)
                        .user2Answered(selectedCard.getUser2Answered() != null && selectedCard.getUser2Answered() == 1L)
                        .build());
            } else {
                // 미선택: 모드/주제만 노출
                HistoryCardProjection first = dayCards.get(0);
                historyCards.add(HistoryCardResponseDTO.HistoryCardItem.builder()
                        .issuedDate(first.getIssuedDate())
                        .mode(first.getMode())
                        .subject(first.getSubject())
                        .selected(false)
                        .build());
            }
        }

        return HistoryCardResponseDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .historyCards(historyCards)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryDetailResponseDTO getHistoryDetailCards(Long userId, LocalDate startDate, LocalDate endDate) {
        // 1. 커플 조회 (coupleId, userId1, userId2)
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 연결이 되어있지 않습니다."));

        // 2. 메인 네이티브 쿼리 (카드 + 질문 + 선택지 + 답변)
        List<HistoryDetailProjection> rows = coupleDailyCardRepository.findHistoryDetailCards(
                couple.getCoupleId(), couple.getUserId1(), couple.getUserId2(),
                startDate, endDate);

        // 3. issuedDate별 그룹핑 (LinkedHashMap으로 순서 보장)
        LinkedHashMap<LocalDate, List<HistoryDetailProjection>> dateGroups = new LinkedHashMap<>();
        for (HistoryDetailProjection row : rows) {
            dateGroups.computeIfAbsent(row.getIssuedDate(), k -> new ArrayList<>()).add(row);
        }

        // 4. 선택 완료 카드의 coupleCardId 수집 (피드백 배치 조회용)
        List<Long> selectedCoupleCardIds = new ArrayList<>();
        for (var entry : dateGroups.entrySet()) {
            entry.getValue().stream()
                    .filter(c -> "Y".equals(c.getSelectedYn()))
                    .map(HistoryDetailProjection::getCoupleCardId)
                    .findFirst()
                    .ifPresent(selectedCoupleCardIds::add);
        }

        // 5. 피드백 배치 조회
        Map<Long, HistoryDetailResponseDTO.FeedbackItem> feedbackMap = new LinkedHashMap<>();
        if (!selectedCoupleCardIds.isEmpty()) {
            List<CoupleDailyCardFeedbackEntity> feedbackMappings =
                    coupleDailyCardFeedbackRepository.findAllByCoupleCardIdInAndDelYn(selectedCoupleCardIds, "N");

            if (!feedbackMappings.isEmpty()) {
                List<Long> feedbackIds = feedbackMappings.stream()
                        .map(CoupleDailyCardFeedbackEntity::getFeedbackId)
                        .distinct()
                        .toList();
                Map<Long, DailyCardFeedbackEntity> feedbackEntities = new LinkedHashMap<>();
                dailyCardFeedbackRepository.findAllById(feedbackIds)
                        .forEach(fb -> feedbackEntities.put(fb.getFeedbackId(), fb));

                for (CoupleDailyCardFeedbackEntity mapping : feedbackMappings) {
                    DailyCardFeedbackEntity fb = feedbackEntities.get(mapping.getFeedbackId());
                    if (fb != null && "N".equals(fb.getDelYn())) {
                        feedbackMap.put(mapping.getCoupleCardId(),
                                HistoryDetailResponseDTO.FeedbackItem.builder()
                                        .feedbackId(fb.getFeedbackId())
                                        .summary(fb.getSummary())
                                        .matchPoints(fb.getMatchPoints())
                                        .differences(fb.getDifferences())
                                        .conversationStarter(fb.getConversationStarter())
                                        .build());
                    }
                }
            }
        }

        // 6. 각 일자별로 DTO 조립
        List<HistoryDetailResponseDTO.HistoryDetailCardItem> historyCards = new ArrayList<>();
        for (var entry : dateGroups.entrySet()) {
            List<HistoryDetailProjection> dayRows = entry.getValue();

            // selectedYn='Y'인 행 필터
            List<HistoryDetailProjection> selectedRows = dayRows.stream()
                    .filter(c -> "Y".equals(c.getSelectedYn()))
                    .toList();

            if (!selectedRows.isEmpty()) {
                // 선택 완료: 전체 상세 정보 조립
                HistoryDetailProjection first = selectedRows.get(0);

                // questionNo로 2차 그룹핑
                LinkedHashMap<Integer, List<HistoryDetailProjection>> questionGroups = new LinkedHashMap<>();
                for (HistoryDetailProjection row : selectedRows) {
                    questionGroups.computeIfAbsent(row.getQuestionNo(), k -> new ArrayList<>()).add(row);
                }

                boolean user1HasAnswer = false;
                boolean user2HasAnswer = false;
                List<HistoryDetailResponseDTO.QuestionItem> questions = new ArrayList<>();

                for (var qEntry : questionGroups.entrySet()) {
                    List<HistoryDetailProjection> qRows = qEntry.getValue();
                    HistoryDetailProjection qFirst = qRows.get(0);

                    // 옵션 수집
                    List<HistoryDetailResponseDTO.OptionItem> options = new ArrayList<>();
                    for (HistoryDetailProjection qRow : qRows) {
                        if (qRow.getOptionNo() != null) {
                            options.add(HistoryDetailResponseDTO.OptionItem.builder()
                                    .optionNo(qRow.getOptionNo())
                                    .optionContent(qRow.getOptionCnts())
                                    .build());
                        }
                    }

                    String u1Answer = qFirst.getUser1Answer();
                    String u2Answer = qFirst.getUser2Answer();
                    if (u1Answer != null) user1HasAnswer = true;
                    if (u2Answer != null) user2HasAnswer = true;

                    questions.add(HistoryDetailResponseDTO.QuestionItem.builder()
                            .questionNo(qFirst.getQuestionNo())
                            .questionType(qFirst.getQuestionType())
                            .questionContent(qFirst.getQuestionCnts())
                            .answerRequired("Y".equals(qFirst.getAnswerReqYn()))
                            .options(options)
                            .user1Answer(u1Answer)
                            .user2Answer(u2Answer)
                            .build());
                }

                historyCards.add(HistoryDetailResponseDTO.HistoryDetailCardItem.builder()
                        .issuedDate(first.getIssuedDate())
                        .mode(first.getMode())
                        .subject(first.getSubject())
                        .selected(true)
                        .coupleCardId(first.getCoupleCardId())
                        .cardId(first.getCardId())
                        .cardTitle(first.getCardTitle())
                        .type(first.getType())
                        .situation(first.getSituation())
                        .user1Answered(user1HasAnswer)
                        .user2Answered(user2HasAnswer)
                        .questions(questions)
                        .feedback(feedbackMap.get(first.getCoupleCardId()))
                        .build());
            } else {
                // 미선택: 모드/주제만 노출
                HistoryDetailProjection first = dayRows.get(0);
                historyCards.add(HistoryDetailResponseDTO.HistoryDetailCardItem.builder()
                        .issuedDate(first.getIssuedDate())
                        .mode(first.getMode())
                        .subject(first.getSubject())
                        .selected(false)
                        .build());
            }
        }

        return HistoryDetailResponseDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .user1Id(couple.getUserId1())
                .user2Id(couple.getUserId2())
                .historyCards(historyCards)
                .build();
    }

    /**
     * 히스토리 데일리카드 단건 조회
     */
    @Override
    public HistoryDetailResponseDTO getHistoryDetailCard(Long userId, Long coupleCardId) {
        // 1. 커플 조회 (coupleId, userId1, userId2)
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 연결이 되어있지 않습니다."));

        if (couple.getUserId2() == null) {
            throw new IllegalStateException("커플이 아닙니다.");
        }

        List<HistoryDetailProjection> historyData = coupleDailyCardRepository.findByCoupleCardId(
                            coupleCardId, couple.getUserId1(), couple.getUserId2());

        // 2. 피드백 배치 조회
        Map<Long, HistoryDetailResponseDTO.FeedbackItem> feedbackMap = new LinkedHashMap<>();
        List<Long> selectedCoupleCardIds = new ArrayList<>();
        selectedCoupleCardIds.add(coupleCardId);

        List<CoupleDailyCardFeedbackEntity> feedbackMappings =
                coupleDailyCardFeedbackRepository.findAllByCoupleCardIdInAndDelYn(selectedCoupleCardIds, "N");

        if (!feedbackMappings.isEmpty()) {
            List<Long> feedbackIds = feedbackMappings.stream()
                    .map(CoupleDailyCardFeedbackEntity::getFeedbackId)
                    .distinct()
                    .toList();
            Map<Long, DailyCardFeedbackEntity> feedbackEntities = new LinkedHashMap<>();
            dailyCardFeedbackRepository.findAllById(feedbackIds)
                    .forEach(fb -> feedbackEntities.put(fb.getFeedbackId(), fb));

            for (CoupleDailyCardFeedbackEntity mapping : feedbackMappings) {
                DailyCardFeedbackEntity fb = feedbackEntities.get(mapping.getFeedbackId());
                if (fb != null && "N".equals(fb.getDelYn())) {
                    feedbackMap.put(mapping.getCoupleCardId(),
                            HistoryDetailResponseDTO.FeedbackItem.builder()
                                    .feedbackId(fb.getFeedbackId())
                                    .summary(fb.getSummary())
                                    .matchPoints(fb.getMatchPoints())
                                    .differences(fb.getDifferences())
                                    .conversationStarter(fb.getConversationStarter())
                                    .build());
                }
            }
        }

        // 3. 각 일자별로 DTO 조립
        List<HistoryDetailResponseDTO.HistoryDetailCardItem> historyCards = new ArrayList<>();

        // 첫번째 행 조회
        HistoryDetailProjection first = historyData.get(0);

        // questionNo로 그룹핑
        LinkedHashMap<Integer, List<HistoryDetailProjection>> questionGroups = new LinkedHashMap<>();
        for (HistoryDetailProjection row : historyData) {
            questionGroups.computeIfAbsent(row.getQuestionNo(), k -> new ArrayList<>()).add(row);
        }

        List<HistoryDetailResponseDTO.QuestionItem> questions = new ArrayList<>();

        for (var qEntry : questionGroups.entrySet()) {
            List<HistoryDetailProjection> qRows = qEntry.getValue();
            HistoryDetailProjection firstRow = qRows.getFirst();

            // 옵션 수집
            List<HistoryDetailResponseDTO.OptionItem> options = new ArrayList<>();
            for (HistoryDetailProjection qRow : qRows) {
                if (qRow.getOptionNo() != null) {
                    options.add(HistoryDetailResponseDTO.OptionItem.builder()
                            .optionNo(qRow.getOptionNo())
                            .optionContent(qRow.getOptionCnts())
                            .build());
                }
            }

            questions.add(HistoryDetailResponseDTO.QuestionItem.builder()
                    .questionNo(firstRow.getQuestionNo())
                    .questionType(firstRow.getQuestionType())
                    .questionContent(firstRow.getQuestionCnts())
                    .answerRequired("Y".equals(firstRow.getAnswerReqYn()))
                    .options(options)
                    .user1Answer(firstRow.getUser1Answer())
                    .user2Answer(firstRow.getUser2Answer())
                    .build());
        }

        historyCards.add(HistoryDetailResponseDTO.HistoryDetailCardItem.builder()
                .issuedDate(first.getIssuedDate())
                .mode(first.getMode())
                .subject(first.getSubject())
                .selected(true)
                .coupleCardId(first.getCoupleCardId())
                .cardId(first.getCardId())
                .cardTitle(first.getCardTitle())
                .type(first.getType())
                .situation(first.getSituation())
                .user1Answered(true)
                .user2Answered(true)
                .questions(questions)
                .feedback(feedbackMap.get(first.getCoupleCardId()))
                .build());


        return HistoryDetailResponseDTO.builder()
                .user1Id(couple.getUserId1())
                .user2Id(couple.getUserId2())
                .historyCards(historyCards)
                .build();
    }

    private CardMode advanceMode(CardMode startMode, int offset) {
        CardMode[] modes = CardMode.values();
        int startIndex = startMode.ordinal();
        int nextIndex = (startIndex + offset) % modes.length;
        return modes[nextIndex];
    }
}
