package com.todaktodot.TDTD.domain.dailycard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.domain.dailycard.dto.ai.AiGeneratedCardDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.GenerateDailyCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.GenerateDailyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardOptionRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardQuestionRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_USER = "SYSTEM";

    @Override
    @Transactional
    public GenerateDailyCardResponseDTO generateDailyCard(GenerateDailyCardRequestDTO requestDTO) {
        CardMode mode = requestDTO.getMode();
        CardSubject subject = requestDTO.getSubject();
        CardType type = requestDTO.getType();

        log.info("========================================");
        log.info("데일리카드 AI 생성 시작");
        log.info("모드: {}, 주제: {}, 유형: {}", mode, subject, type);
        log.info("========================================");

        // 1. AI 호출하여 콘텐츠 생성
        AiGeneratedCardDTO aiResponse = callAiForCardGeneration(mode, subject, type);

        // 2. DailyCard 엔티티 생성 및 저장
        DailyCardEntity dailyCard = DailyCardEntity.builder()
                .mode(mode)
                .subject(subject)
                .type(type)
                .cardTitle(aiResponse.getCardTitle())
                .regrId(SYSTEM_USER)
                .updrId(SYSTEM_USER)
                .build();

        DailyCardEntity savedCard = dailyCardRepository.save(dailyCard);
        Long cardId = savedCard.getCardId();

        log.info("데일리카드 저장 완료: cardId={}", cardId);

        // 3. Question 및 Option 저장 (수동 save만 사용, Cascade 충돌 방지)
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

    private AiGeneratedCardDTO callAiForCardGeneration(CardMode mode, CardSubject subject, CardType type) {
        String prompt = buildPrompt(mode, subject, type);

        log.info("AI 프롬프트 생성 완료, API 호출 시작...");

        ChatClient chatClient = chatClientBuilder.build();

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.info("AI 응답 수신 완료");
        log.debug("AI 응답 내용: {}", response);

        // JSON 파싱
        try {
            // AI 응답에서 JSON 부분만 추출 (마크다운 코드블록 제거)
            String jsonContent = extractJsonFromResponse(response);
            return objectMapper.readValue(jsonContent, AiGeneratedCardDTO.class);
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

    private String buildPrompt(CardMode mode, CardSubject subject, CardType type) {
        int optionCount = type.getOptionCount();
        String typeDescription = type == CardType.ROLEPLAY
                ? "특정 상황을 제시하고, 그 상황에서 어떻게 행동할지 묻는 상황극"
                : "A vs B 중 하나를 선택하는 밸런스게임";

        // 랜덤 시드로 다양성 확보
        int randomSeed = (int) (Math.random() * 1000);
        String situationCategory = getRandomSituationCategory(subject);

        return String.format("""
            당신은 커플 대화 질문을 만드는 창의적인 콘텐츠 기획자입니다.
            아래 조건에 맞는 데일리카드 질문을 만들어주세요.

            [랜덤 시드: %d - 이 숫자를 참고하여 매번 완전히 새로운 상황을 만들어주세요]

            [조건]
            - 질문 모드: %s (%s)
            - 질문 주제: %s (%s)
            - 질문 유형: %s (%s)
            - 객관식 선택지 개수: %d개
            - 상황 카테고리: %s

            [모드별 특성]
            - 디저트(DESSERT): 가벼운 취향/선호 질문. "좋아한다/싫어한다" 수준의 가벼운 대화
            - 커피(COFFEE): 경험/방식에 대한 질문. "보통은 이렇게 한다"는 패턴 공유
            - 위스키(WHISKEY): 가치관/철학에 대한 질문. "나는 반드시 이렇게 한다"는 원칙 공유

            [주제별 상황 카테고리 - 반드시 지정된 카테고리로 상황 구성]
            - 연애관(LOVE): 기념일/이벤트, 질투/불안, 연락 빈도, 친구/가족 소개, 미래 계획, 싸움 후 화해, 서프라이즈, 고민 상담, 스킨십, 취미 공유
            - 경제관(ECONOMY): 외식비, 공과금/생활비, 선물 예산, 저축/투자, 경조사비, 구독 서비스, 취미 지출, 여행 경비, 이사/인테리어, 용돈/개인지출
            - 생활관(LIFESTYLE): 집안일 분담, 반려동물, 식사/요리, 수면 습관, 주말 계획, 운동/건강, 청소/정리, TV/넷플릭스, 친구 만남, 귀가 시간

            [유형별 특성]
            - 상황극(ROLEPLAY): 구체적인 장소, 시간, 상황을 묘사하여 현실감 있게. "~했습니다", "~인 상황입니다" 형식으로 생생하게 묘사. 선택지는 구체적인 행동/반응으로 구성.
            - 밸런스게임(BALANCE): 명확하게 대비되는 두 가지 선택지. 둘 다 장단점이 있어서 고민되는 상황으로.

            [중요: 피해야 할 흔한 패턴]
            - "쇼핑 중 비싼 옷 발견" 류의 상황 금지
            - "감정 표현 방식" 같은 추상적 질문 금지
            - "기분이 안 좋은 상대방" 류의 상황 금지
            - 구체적인 금액(10만원, 15만원 등)을 반복하지 말 것

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
            
            [출력 예시 - DESSERT, ECONOMY, ROLEPLAY]
            
            ```json
            {
              "cardTitle": "둘이 처음 가는 고급 레스토랑에서 식사를 마쳤습니다. 계산서가 15만원이 나왔는데, 마침 둘 다 지갑을 꺼내려고 하는 상황이에요!",
              "questions": [
                {
                  "questionNo": 1,
                  "questionType": "MULTIPLE_CHOICE",
                  "answerRequired": true,
                  "content": "객관식 질문 내용",
                  "options": [
                    {"optionNo": 1, "content": "내가 먼저 카드를 내밀며 '내가 낼게'라고 한다"},
                    {"optionNo": 2, "content": "반반 하자'고 제안한다"},
                    {"optionNo": 3, "content": "상대방이 내려고 하면 '고마워, 다음엔 내가 낼게' 한다"},
                    {"optionNo": 4, "content": "'가위바위보로 정하자'며 재미있게 풀어간다"},
                    {"optionNo": 5, "content": "조용히 기다리며 상대방의 반응을 본다"}
                  ]
                },
                {
                  "questionNo": 2,
                  "questionType": "SUBJECTIVE",
                  "answerRequired": false,
                  "content": "그렇게 생각한 이유는 무엇인가요?(선택)",
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
            """,
                randomSeed,
                mode.getDisplayName(), mode.getDescription(),
                subject.getDisplayName(), subject.getDescription(),
                type.getDisplayName(), typeDescription,
                optionCount,
                situationCategory,
                optionCount
        );
    }

    private String getRandomSituationCategory(CardSubject subject) {
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
}