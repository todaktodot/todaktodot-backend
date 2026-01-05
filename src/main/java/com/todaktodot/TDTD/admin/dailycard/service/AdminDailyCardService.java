package com.todaktodot.TDTD.admin.dailycard.service;

import com.todaktodot.TDTD.admin.dailycard.dto.CardStatisticsDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardDetailDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardListDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardSearchDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardUpdateDTO;
import org.springframework.data.domain.Page;

public interface AdminDailyCardService {

    Page<DailyCardListDTO> searchDailyCards(DailyCardSearchDTO searchDTO);

    DailyCardDetailDTO getDailyCardDetail(Long cardId);

    void updateDailyCard(DailyCardUpdateDTO updateDTO);

    void deleteDailyCard(Long cardId);

    void toggleUseYn(Long cardId);

    CardStatisticsDTO getCardStatistics();

    long getTotalCount();

    long getActiveCount();

    long getInactiveCount();
}
