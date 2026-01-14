package com.todaktodot.TDTD.domain.aireport.service;

import com.todaktodot.TDTD.domain.aireport.dto.response.ReportCreateStatusResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportDetailResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportResponseWrapDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.SyncAnswerDTO;
import com.todaktodot.TDTD.domain.aireport.repository.ReportRepository;
import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.CoupleDailyCardRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardUserAnswerRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final CoupleRepository coupleRepository;
    private final ReportRepository reportRepository;
    private final DailyCardUserAnswerRepository dailyCardUserAnswerRepository;
    private final CoupleDailyCardRepository coupleDailyCardRepository;

    /**
     * 지난 한 주 AI 리포트 생성 여부 확인
     */
    @Override
    public ReportResponseWrapDTO checkCreatable(Long userId) {
        //커플 찾기 -> 커플이 아니면?
        CoupleEntity coupleInfo = coupleRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException());
        Long userId1 = coupleInfo.getUserId1();
        Long userId2 = coupleInfo.getUserId2();

        //생성된적 있는지 확인
        //오늘 날짜
        LocalDate today = LocalDate.now();
        // 시작: 월요일 08:00
        LocalDateTime startDT = today.with(DayOfWeek.MONDAY).atTime(8, 0);
        // 종료: 다음 주 월요일 07:59:59.999999999
        LocalDateTime endDT = startDT.plusWeeks(1).minusNanos(1);
        //해당 주차에 이미 생성된게 있는 경우 -> 조회 후 반환
        if (reportRepository.existsByCoupleEntityAndRegDtGreaterThanEqualAndRegDtLessThanAndDelYn(coupleInfo, startDT, endDT, "N")) {
            //생성 가능 여부 및 초기 진입 여부 정보
            ReportCreateStatusResponseDTO createStatusResponse = ReportCreateStatusResponseDTO.builder()
                    .isCreatable(false)
                    .isInitalize(false)
                    .build();

            //이미 생성된 AI 리포트 상세 정보
            ReportDetailResponseDTO reportDetailResponse = getReport();

            return new ReportResponseWrapDTO(createStatusResponse, reportDetailResponse);
        }
        //생성된게 없는 경우
        else {
            //커플 - 둘 다 답변 완료 갯수 확인
            boolean isCreatable = dailyCardUserAnswerRepository.existsSameDailyCardAnswerInPeriod(userId1, userId2, startDT, endDT, "N");
            ReportCreateStatusResponseDTO createStatusResponse = ReportCreateStatusResponseDTO.builder()
                    .isCreatable(isCreatable)
                    .isInitalize(true)
                    .build();
            //신규 AI 생성
            Report createdReport = createReport(startDT, endDT, userId1, userId2);

            //생성된 AI리포트 상세 정보

            return new ReportResponseWrapDTO(createStatusResponse, null);
        }
    }

    /**
     * AI 리포트 생성
     */
    public Report createReport(LocalDateTime startDt, LocalDateTime endDt, Long userId1, Long userId2) {
        //주간 일자
        //데일리 카드 중 경제관인 것
        List<SyncAnswerDTO> eonomyCardAnswerList = dailyCardUserAnswerRepository.findDailyCardAnswerBySubject(CardSubject.ECONOMY, userId1, userId2, startDt, endDt, "N");
        int economyCardCnt = coupleDailyCardRepository.findDailyCardBySubject(CardSubject.ECONOMY, startDt, endDt, "N");
        //경제관 싱크로율

        //데일리 카드 중 생활관인 것
        List<SyncAnswerDTO> lifeCardAnswerList = dailyCardUserAnswerRepository.findDailyCardAnswerBySubject(CardSubject.LIFESTYLE, userId1, userId2, startDt, endDt, "N");
        int lifeCardCnt = coupleDailyCardRepository.findDailyCardBySubject(CardSubject.LIFESTYLE, startDt, endDt, "N");
        //생활관 싱크로율

        //데일리 카드 중 연애관인 것
        List<SyncAnswerDTO> loveCardAnswerList = dailyCardUserAnswerRepository.findDailyCardAnswerBySubject(CardSubject.LOVE, userId1, userId2, startDt, endDt, "N");
        int loveCardCnt = coupleDailyCardRepository.findDailyCardBySubject(CardSubject.LOVE, startDt, endDt, "N");
        //연애관 싱크로율

        //대화참여율
        //대화누적자산

        //비슷했던 주제
        //대화가 더 필요한 주제
        return null;
    }

    /**
     * AI 리포트 상세조회
     */
    public ReportDetailResponseDTO getReport() {
        return null;
    }
}
