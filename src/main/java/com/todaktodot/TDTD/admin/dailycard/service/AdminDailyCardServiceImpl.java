package com.todaktodot.TDTD.admin.dailycard.service;

import com.todaktodot.TDTD.admin.dailycard.dto.AiGenerationInfoDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.CardStatisticsDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardDetailDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardListDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardSearchDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardUpdateDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.AiCardGenerationInfoRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardOptionRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardQueryRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardQuestionRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDailyCardServiceImpl implements AdminDailyCardService {

    private final DailyCardRepository dailyCardRepository;
    private final DailyCardQueryRepository dailyCardQueryRepository;
    private final DailyCardQuestionRepository questionRepository;
    private final DailyCardOptionRepository optionRepository;
    private final AiCardGenerationInfoRepository aiCardGenerationInfoRepository;

    private static final Long ADMIN_USER_ID = 0L;

    /**
     * 데일리카드 목록 검색
     */
    @Override
    public Page<DailyCardListDTO> searchDailyCards(DailyCardSearchDTO searchDTO) {
        return dailyCardQueryRepository.searchDailyCards(searchDTO);
    }

    @Override
    public DailyCardDetailDTO getDailyCardDetail(Long cardId) {
        DailyCardEntity card = dailyCardRepository.findByIdWithQuestionsAndOptions(cardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + cardId));

        return DailyCardDetailDTO.from(card);
    }

    @Override
    @Transactional
    public Long updateDailyCard(DailyCardUpdateDTO updateDTO) {
        DailyCardEntity card = dailyCardRepository.findByIdWithQuestionsAndOptions(updateDTO.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + updateDTO.getCardId()));

        DailyCardEntity newCard = DailyCardEntity.builder()
                .regrId(ADMIN_USER_ID)
                .updrId(ADMIN_USER_ID)
                .build();

        newCard.update(
                updateDTO.getMode(),
                updateDTO.getSubject(),
                updateDTO.getType(),
                updateDTO.getCardTitle(),
                updateDTO.getSituation(),
                updateDTO.getUseYn(),
                ADMIN_USER_ID
        );

        DailyCardEntity savedCard = dailyCardRepository.save(newCard);

        aiCardGenerationInfoRepository.findByCardId(card.getCardId())
                .ifPresent(info -> {
                    String finalPrompt = "이 데일리카드는 데일리카드 ID " + card.getCardId()
                            + "로부터 수정되어 재발급되었습니다.\n\n" + info.getFinalPrompt();

                    AiCardGenerationInfoEntity newInfo = AiCardGenerationInfoEntity.builder()
                            .cardId(savedCard.getCardId())
                            .promptId(info.getPromptId())
                            .aiModel(info.getAiModel())
                            .temperature(info.getTemperature())
                            .mode(info.getMode())
                            .subject(info.getSubject())
                            .type(info.getType())
                            .situationCategory(info.getSituationCategory())
                            .finalPrompt(finalPrompt)
                            .aiResponse(info.getAiResponse())
                            .regrId(ADMIN_USER_ID)
                            .build();

                    aiCardGenerationInfoRepository.save(newInfo);
                });

        if (updateDTO.getQuestions() != null) {
            for (DailyCardUpdateDTO.QuestionUpdateDTO questionDTO : updateDTO.getQuestions()) {
                String answerReqYn = "Y".equals(questionDTO.getAnswerReqYn()) ? "Y" : "N";
                DailyCardQuestionEntity question = DailyCardQuestionEntity.builder()
                        .cardId(savedCard.getCardId())
                        .questionNo(questionDTO.getQuestionNo())
                        .questionType(questionDTO.getQuestionType())
                        .answerReqYn(answerReqYn)
                        .questionCnts(questionDTO.getQuestionCnts())
                        .regrId(ADMIN_USER_ID)
                        .updrId(ADMIN_USER_ID)
                        .build();

                questionRepository.save(question);

                if (questionDTO.getOptions() != null) {
                    for (DailyCardUpdateDTO.OptionUpdateDTO optionDTO : questionDTO.getOptions()) {
                        DailyCardOptionEntity option = DailyCardOptionEntity.builder()
                                .cardId(savedCard.getCardId())
                                .questionNo(questionDTO.getQuestionNo())
                                .optionNo(optionDTO.getOptionNo())
                                .optionCnts(optionDTO.getOptionCnts())
                                .regrId(ADMIN_USER_ID)
                                .updrId(ADMIN_USER_ID)
                                .build();

                        optionRepository.save(option);
                    }
                }
            }
        }

        card.update(
                card.getMode(),
                card.getSubject(),
                card.getType(),
                card.getCardTitle(),
                card.getSituation(),
                "N",
                ADMIN_USER_ID
        );

        return savedCard.getCardId();
    }

    @Override
    @Transactional
    public void deleteDailyCard(Long cardId) {
        DailyCardEntity card = dailyCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + cardId));

        dailyCardRepository.softDelete(cardId, ADMIN_USER_ID);
    }

    @Override
    @Transactional
    public void toggleUseYn(Long cardId) {
        DailyCardEntity card = dailyCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + cardId));

        String newUseYn = "Y".equals(card.getUseYn()) ? "N" : "Y";
        dailyCardRepository.updateUseYn(cardId, newUseYn, ADMIN_USER_ID);
    }

    /**
     * 통계 정보를 단일 쿼리로 조회
     */
    @Override
    public CardStatisticsDTO getCardStatistics() {
        List<Object[]> result = dailyCardRepository.getCardStatistics();
        if (result.isEmpty()) {
            return new CardStatisticsDTO(0L, 0L, 0L);
        }
        return CardStatisticsDTO.from(result.get(0));
    }

    @Override
    public long getTotalCount() {
        return dailyCardRepository.countByDelYn("N");
    }

    @Override
    public long getActiveCount() {
        return dailyCardRepository.countByUseYnAndDelYn("Y", "N");
    }

    @Override
    public long getInactiveCount() {
        return dailyCardRepository.countByUseYnAndDelYn("N", "N");
    }

    @Override
    public Optional<AiGenerationInfoDTO> getAiGenerationInfo(Long cardId) {
        return aiCardGenerationInfoRepository.findByCardId(cardId)
                .map(AiGenerationInfoDTO::from);
    }
}
