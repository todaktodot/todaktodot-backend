package com.todaktodot.TDTD.domain.insight.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.admin.prompt.repository.AiPromptRepository;
import com.todaktodot.TDTD.admin.prompt.repository.entity.AiPromptEntity;
import com.todaktodot.TDTD.domain.aireport.repository.ReportRepository;
import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.QuestionType;
import com.todaktodot.TDTD.domain.insight.dto.*;
import com.todaktodot.TDTD.domain.insight.repository.AiReportInsightInfoRespository;
import com.todaktodot.TDTD.domain.insight.repository.InsightConfigRepository;
import com.todaktodot.TDTD.domain.insight.repository.InsightRepository;
import com.todaktodot.TDTD.domain.insight.repository.entity.AiReportInsightInfo;
import com.todaktodot.TDTD.domain.insight.repository.entity.Insight;
import com.todaktodot.TDTD.domain.insight.repository.entity.InsightConfig;
import com.todaktodot.TDTD.domain.insight.repository.entity.InsightStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService{

    private final InsightRepository insightRepository;
    private final CoupleRepository coupleRepository;
    private final InsightConfigRepository insightConfigRepository;
    private final ReportRepository reportRepository;
    private final AiReportInsightInfoRespository aiReportInsightInfoRespository;
    private final AiPromptRepository aiPromptRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public GenerateInsightResponseDTO generateInsight(Long userId, GenerateInsightRequestDTO requestDTO) {
        // 1단계. 한 주간 응답 데이터 조회
        CoupleEntity couple = coupleRepository.findById(requestDTO.getCoupleId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 커플입니다."));

        if (couple.isSolo()) {
            throw new IllegalStateException("혼자 둘러보기는 해당 기능을 사용할 수 없습니다.");
        }

        LocalDate endDate = requestDTO.getEndDt();
        //인사이트 생성 요청 종료날짜가 월요일인지 검증
        if (endDate.getDayOfWeek().getValue() != 1) {
            throw new IllegalStateException("인사이트 생성 시작 일자가 월요일이 아닙니다.");
        }

        // 종료: 월요일 00:00
        LocalDateTime endDt = endDate.atTime(00, 0);
        // 시작: 전 주 월요일
        LocalDateTime strtDt = endDt.minusWeeks(1);

        //중복시 리턴
        checkDuplicate(couple.getCoupleId(),strtDt.toLocalDate(), endDt.toLocalDate());

        GenerateInsightContext insightContext = loadInsightContext(couple, strtDt, endDt);

        // 2단계. AI 호출
        InsightConfig config = insightConfigRepository
                .findTopByDelYnOrderByConfigIdDesc("N")
                .orElseThrow(() -> new IllegalStateException("인사이트 생성 설정이 없습니다. Admin에서 설정해주세요."));

        if (config.getPromptId() == null) {
            throw new IllegalStateException("적용 중인 인사이트 프롬프트가 없습니다. Admin에서 프롬프트를 설정해주세요.");
        }

        AiPromptEntity promptEntity = aiPromptRepository.findById(config.getPromptId())
                .orElseThrow(() -> new IllegalStateException("프롬프트(ID: " + config.getPromptId() + ")를 찾을 수 없습니다."));

        String aiModel = config.getAiModel();
        double temperature = config.getTemperature().doubleValue();

        InsightGenerationResult insightResult = callAiForInsight(insightContext, promptEntity.getPromptContent(), aiModel, temperature);

        // 3단계. Insight 저장
        // ==================== 3단계: 저장 트랜잭션 ====================
        return transactionTemplate.execute(status -> {
            GenerateInsightResponseDTO generateInsightResponseDTO = saveInsightResult(couple.getCoupleId(), userId, insightContext, insightResult, aiModel, config.getPromptId());

            reportRepository.findByCoupleEntityAndStrtDtAndEndDtAndDelYn(couple, endDate.minusWeeks(1), endDate, "N")
                    .ifPresent(report -> {
                        report.updateInsight(generateInsightResponseDTO.getInsightId());
                    });
            return generateInsightResponseDTO;
        });
    }

    private void checkDuplicate(Long coupleId,LocalDate startDt, LocalDate endDt) {
        boolean alreadyExists = insightRepository
                .findByCoupleIdAndStartDtAndEndDtAndDelYn(coupleId, startDt, endDt, "N")
                .isPresent();

        if (alreadyExists) {
            throw new IllegalStateException("이미 해당 카드에 대한 인사이트가 생성되었습니다. 삭제 후 다시 생성해주세요");
        }
    }
    //인사이트 생성에 필요한 응답 정보
    private GenerateInsightContext loadInsightContext(CoupleEntity couple, LocalDateTime startDt, LocalDateTime endDt) {
        Long userId1 = couple.getUserId1();
        Long userId2 = couple.getUserId2();
        if (userId1 == null || userId2 == null) {
            throw new IllegalStateException("필요한 사용자 정보가 충분하지 않습니다.");
        }

        //한 주동안 둘다 응답 데이터 주제별로 수집
        // 1. 경재관
        List<InsightNeedDataDTO> economyData = insightRepository.findInsightDataByCouple(CardSubject.ECONOMY.name(), userId1, userId2, startDt, endDt);
        Map<Long, List<InsightNeedDataDTO>> economyByCard = economyData.stream()
                .collect(Collectors.groupingBy(
                        InsightNeedDataDTO::cardId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        // 2. 생활관
        List<InsightNeedDataDTO> lifestyleData = insightRepository.findInsightDataByCouple(CardSubject.LIFESTYLE.name(), userId1, userId2, startDt, endDt);
        Map<Long, List<InsightNeedDataDTO>> lifeStyleByCard = lifestyleData.stream()
                .collect(Collectors.groupingBy(
                        InsightNeedDataDTO::cardId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        // 3. 연애관
        List<InsightNeedDataDTO> loveData = insightRepository.findInsightDataByCouple(CardSubject.LOVE.name(), userId1, userId2, startDt, endDt);
        Map<Long, List<InsightNeedDataDTO>> loveByCard = loveData.stream()
                .collect(Collectors.groupingBy(
                        InsightNeedDataDTO::cardId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<GenerateInsightContext.QuestionData> economyAnswerData = extracted(economyByCard);
        List<GenerateInsightContext.QuestionData> lifestyleAnswerData = extracted(lifeStyleByCard);
        List<GenerateInsightContext.QuestionData> loveAnswerData = extracted(loveByCard);

        GenerateInsightContext insightContext = new GenerateInsightContext(
                couple.getCoupleId(),
                userId1,
                userId2,
                startDt.toLocalDate(),
                endDt.toLocalDate(),
                economyAnswerData,
                lifestyleAnswerData,
                loveAnswerData
        );

        return insightContext;
    }

    private List<GenerateInsightContext.QuestionData> extracted(Map<Long, List<InsightNeedDataDTO>> groupByCard) {
        List<GenerateInsightContext.QuestionData> question = new ArrayList<>();
        for (Map.Entry<Long, List<InsightNeedDataDTO>> entry : groupByCard.entrySet()) {
            List<InsightNeedDataDTO> cardAndAnswerData = entry.getValue();

            if (cardAndAnswerData.isEmpty()) {
                throw new IllegalStateException("두 명 모두 응답하지 않은 데일리카드입니다.");
            }

            InsightNeedDataDTO firstData = cardAndAnswerData.getFirst();
            GenerateInsightContext.OptionData optionData = null;
            GenerateInsightContext.SubjectiveData subjectiveData = null;

            for (InsightNeedDataDTO data : cardAndAnswerData) {
                //객관식 응답
                if (data.questionType().equals(QuestionType.MULTIPLE_CHOICE.name())) {
                    optionData = new GenerateInsightContext.OptionData(
                            data.questionNo(),
                            data.questionType(),
                            data.questionCnts(),
                            data.optionAnswer1(),
                            data.optionAnswer2());
                }
                //주관식 응답
                else {
                    subjectiveData = new GenerateInsightContext.SubjectiveData(
                            data.questionNo(),
                            data.questionType(),
                            data.questionCnts(),
                            data.subjectiveAnswer1(),
                            data.subjectiveAnswer2());
                }
            }

            question.add(new GenerateInsightContext.QuestionData(
                    firstData.cardId(),
                    firstData.cardTitle(),
                    firstData.mode(),
                    firstData.subject(),
                    firstData.type(),
                    optionData,
                    subjectiveData
            ));
        }
        return question;
    }

    private InsightGenerationResult callAiForInsight(GenerateInsightContext insightContext, String promptContent, String aiModel, double temperature) {
        String dynamicContext = buildDynamicContext(insightContext);
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
            throw new IllegalStateException("AI 인사이트 생성 호출에 실패했습니다.", e);
        }

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI 인사이트 응답이 비어있습니다.");
        }

        try {
            String jsonContent = extractJsonFromResponse(response);
            AiGeneratedInsightDTO parsed = objectMapper.readValue(jsonContent, AiGeneratedInsightDTO.class);
            return new InsightGenerationResult(prompt, response, parsed, temperature);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("AI 인사이트 응답을 파싱할 수 없습니다.", e);
        }
    }

    //응답 데이터 프롬프트화
    private String buildDynamicContext(GenerateInsightContext context) {

        StringBuilder builder = new StringBuilder();
        builder.append("===<경제관>===\n");
        for(GenerateInsightContext.QuestionData questionDate : context.getEconomyData()) {
            builder.append("[카드 정보]\n");
            builder.append("- 제목: ").append(questionDate.getCardTitle()).append("\n");
            builder.append("- 모드: ").append(questionDate.getMode()).append("\n");
            builder.append("- 주제: ").append(questionDate.getSubject()).append("\n\n");

            GenerateInsightContext.OptionData optionData = questionDate.getOptionData();
            GenerateInsightContext.SubjectiveData subjectiveData = questionDate.getSubjectiveData();

            if (optionData != null) {
                builder.append("[객관식]\n");
                builder.append("- 질문: ").append(optionData.getQuestionCnt()).append("\n");
                builder.append("- 유저1 답변: ").append(optionData.getOptionAnswer1()).append("\n");
                builder.append("- 유저2 답변: ").append(optionData.getOptionAnswer2()).append("\n\n");
            }

            if (subjectiveData != null) {
                builder.append("[주관식]\n");
                builder.append("- 질문: ").append(subjectiveData.getQuestionCnt()).append("\n");
                builder.append("- 유저1 답변: ").append(subjectiveData.getSubjectiveAnswer1()).append("\n");
                builder.append("- 유저2 답변: ").append(subjectiveData.getSubjectiveAnswer2()).append("\n\n");
            }
            builder.append("\n");
        }

        builder.append("===<생활관>===\n");
        for(GenerateInsightContext.QuestionData questionDate : context.getLifestyleData()) {
            builder.append("[카드 정보]\n");
            builder.append("- 제목: ").append(questionDate.getCardTitle()).append("\n");
            builder.append("- 모드: ").append(questionDate.getMode()).append("\n");
            builder.append("- 주제: ").append(questionDate.getSubject()).append("\n\n");

            GenerateInsightContext.OptionData optionData = questionDate.getOptionData();
            GenerateInsightContext.SubjectiveData subjectiveData = questionDate.getSubjectiveData();

            if (optionData != null) {
                builder.append("[객관식]\n");
                builder.append("- 질문: ").append(optionData.getQuestionCnt()).append("\n");
                builder.append("- 유저1 답변: ").append(optionData.getOptionAnswer1()).append("\n");
                builder.append("- 유저2 답변: ").append(optionData.getOptionAnswer2()).append("\n\n");
            }

            if (subjectiveData != null) {
                builder.append("[주관식]\n");
                builder.append("- 질문: ").append(subjectiveData.getQuestionCnt()).append("\n");
                builder.append("- 유저1 답변: ").append(subjectiveData.getSubjectiveAnswer1()).append("\n");
                builder.append("- 유저2 답변: ").append(subjectiveData.getSubjectiveAnswer2()).append("\n\n");
            }
            builder.append("\n");
        }

        builder.append("===<연애관>===\n");
        for(GenerateInsightContext.QuestionData questionDate : context.getLoveData()) {
            builder.append("[카드 정보]\n");
            builder.append("- 제목: ").append(questionDate.getCardTitle()).append("\n");
            builder.append("- 모드: ").append(questionDate.getMode()).append("\n");
            builder.append("- 주제: ").append(questionDate.getSubject()).append("\n\n");

            GenerateInsightContext.OptionData optionData = questionDate.getOptionData();
            GenerateInsightContext.SubjectiveData subjectiveData = questionDate.getSubjectiveData();

            if (optionData != null) {
                builder.append("[객관식]\n");
                builder.append("- 질문: ").append(optionData.getQuestionCnt()).append("\n");
                builder.append("- 유저1 답변: ").append(optionData.getOptionAnswer1()).append("\n");
                builder.append("- 유저2 답변: ").append(optionData.getOptionAnswer2()).append("\n\n");
            }

            if (subjectiveData != null) {
                builder.append("[주관식]\n");
                builder.append("- 질문: ").append(subjectiveData.getQuestionCnt()).append("\n");
                builder.append("- 유저1 답변: ").append(subjectiveData.getSubjectiveAnswer1()).append("\n");
                builder.append("- 유저2 답변: ").append(subjectiveData.getSubjectiveAnswer2()).append("\n\n");
            }
            builder.append("\n");
        }

        return builder.toString();
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

    private GenerateInsightResponseDTO saveInsightResult(Long coupleId,
                                                         Long userId,
                                                         GenerateInsightContext context,
                                                         InsightGenerationResult insightResult,
                                                         String aiModel,
                                                         Long promptId) {

        AiGeneratedInsightDTO aiInsight = insightResult.getParsedResponse();

        Insight insight = insightRepository.save(
                Insight.builder()
                        .coupleId(coupleId)
                        .startDt(context.getStartDt())
                        .endDt(context.getEndDt())
                        .summary(aiInsight.getSummary())
                        .economyPart(aiInsight.getEconomyPart())
                        .lifestylePart(aiInsight.getLifestylePart())
                        .lovePart(aiInsight.getLovePart())
                        .regrId(userId)
                        .updrId(userId)
                        .build()
        );


        aiReportInsightInfoRespository.save(
                AiReportInsightInfo.builder()
                        .insightId(insight.getId())
                        .promptId(promptId)
                        .aiModel(aiModel)
                        .temperature(String.valueOf(insightResult.getActualTemperatur()))
                        .finalPrompt(insightResult.getFinalPrompt())
                        .status(InsightStatus.SUCCESS.name())
                        .regrId(userId)
                        .updrId(userId)
                        .build());

        return GenerateInsightResponseDTO.from(insight);
    }
}
