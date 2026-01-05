package com.todaktodot.TDTD.admin.dailycard.service;

import com.todaktodot.TDTD.admin.dailycard.dto.CardStatisticsDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardDetailDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardListDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardSearchDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardUpdateDTO;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDailyCardServiceImpl implements AdminDailyCardService {

    private final DailyCardRepository dailyCardRepository;
    private final DailyCardQueryRepository dailyCardQueryRepository;
    private final DailyCardQuestionRepository questionRepository;
    private final DailyCardOptionRepository optionRepository;

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
    public void updateDailyCard(DailyCardUpdateDTO updateDTO) {
        // 카드와 질문, 옵션을 함께 조회
        DailyCardEntity card = dailyCardRepository.findByIdWithQuestionsAndOptions(updateDTO.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다: " + updateDTO.getCardId()));

        // 1. 카드 기본 정보 업데이트
        card.update(
                updateDTO.getMode(),
                updateDTO.getSubject(),
                updateDTO.getType(),
                updateDTO.getCardTitle(),
                updateDTO.getUseYn(),
                ADMIN_USER_ID
        );

        // 2. 질문 업데이트
        if (updateDTO.getQuestions() != null) {
            for (DailyCardUpdateDTO.QuestionUpdateDTO questionDTO : updateDTO.getQuestions()) {
                card.getQuestions().stream()
                        .filter(q -> q.getQuestionNo().equals(questionDTO.getQuestionNo()))
                        .findFirst()
                        .ifPresent(question -> {
                            question.update(
                                    questionDTO.getQuestionType(),
                                    questionDTO.getAnswerReqYn(),
                                    questionDTO.getQuestionCnts(),
                                    ADMIN_USER_ID
                            );

                            // 3. 옵션 업데이트
                            if (questionDTO.getOptions() != null) {
                                for (DailyCardUpdateDTO.OptionUpdateDTO optionDTO : questionDTO.getOptions()) {
                                    question.getOptions().stream()
                                            .filter(o -> o.getOptionNo().equals(optionDTO.getOptionNo()))
                                            .findFirst()
                                            .ifPresent(option -> option.update(
                                                    optionDTO.getOptionCnts(),
                                                    ADMIN_USER_ID
                                            ));
                                }
                            }
                        });
            }
        }
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
}
