package com.todaktodot.TDTD.domain.aireport.service;

import com.todaktodot.TDTD.domain.aireport.dto.response.*;
import com.todaktodot.TDTD.domain.aireport.repository.ReportRepository;
import com.todaktodot.TDTD.domain.aireport.repository.entity.DiffrentAnswer;
import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import com.todaktodot.TDTD.domain.aireport.repository.entity.SimilarAnswer;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.CoupleDailyCardRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardUserAnswerRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CoupleDailyCardEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.QuestionType;
import com.todaktodot.TDTD.domain.insight.repository.InsightRepository;
import com.todaktodot.TDTD.domain.insight.repository.entity.Insight;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final CoupleRepository coupleRepository;
    private final ReportRepository reportRepository;
    private final DailyCardUserAnswerRepository dailyCardUserAnswerRepository;
    private final CoupleDailyCardRepository coupleDailyCardRepository;
    private final InsightRepository insightRepository;

    /**
     * 지난 한 주 AI 리포트 생성 여부 확인
     */
    @Override
    @Transactional
    public ReportResponseWrapDTO checkCreatable(Long userId) {
        //커플 찾기 -> 커플이 아니면?
        CoupleEntity coupleInfo = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("[userId : " + userId+ " ] 에 해당하는 커플 정보가 없습니다."));
        Long userId1 = coupleInfo.getUserId1();
        Long userId2 = coupleInfo.getUserId2();

        // [TDTDBE-55] SOLO 커플(userId2가 NULL)은 리포트 생성 불가
        if (userId2 == null) {
            throw new IllegalStateException("커플 연결 후 이용 가능한 기능입니다.");
        }

        //생성된적 있는지 확인
        //오늘 날짜
        LocalDate today = LocalDate.now();
        // 시작: 월요일 08:00
        LocalDateTime endDT = today.with(DayOfWeek.MONDAY).atTime(8, 0);
        // 종료: 저번 주 월요일 07:59:59.999999999
        LocalDateTime startDT = endDT.minusWeeks(1).minusNanos(1);

        Optional<Report> findReport = reportRepository.findByCoupleEntityAndStrtDtAndEndDtAndDelYn(coupleInfo, startDT.toLocalDate(), endDT.toLocalDate(), "N");
        //해당 주차에 이미 생성된게 있는 경우 -> 조회 후 반환
        if (findReport.isPresent()) {
            //생성 가능 여부 및 초기 진입 여부 정보
            ReportCreateStatusResponseDTO createStatusResponse = ReportCreateStatusResponseDTO.builder()
                    .isCreatable(false)
                    .isInitalize(false)
                    .build();

            //이미 생성된 AI 리포트 상세 정보
            Report report = findReport.get();
            ReportDetailResponseDTO reportDetailResponse = getReportInfo(report, coupleInfo);

            //인사이트 조회
            Long insightId = report.getInsightId();
            String insightContent = getInsight(insightId);
            ReportDetailResponseDTO.InsightInfo insightInfo = ReportDetailResponseDTO.InsightInfo.from(insightId, insightContent);
            reportDetailResponse.setInsightInfo(insightInfo);

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

            // AI리포트 생성 불가능한 경우
            ReportDetailResponseDTO reportDetailResponse = null;
            // AI리포트 생성 가능한 경우
            if (isCreatable) {
                //신규 AI 생성
                Report createdReport = createReport(startDT, endDT, userId1, userId2, coupleInfo);
                reportDetailResponse = getReportInfo(createdReport, coupleInfo);

                //인사이트 조회
                Long insightId = createdReport.getInsightId();
                String insightContent = getInsight(insightId);
                ReportDetailResponseDTO.InsightInfo insightInfo = ReportDetailResponseDTO.InsightInfo.from(insightId, insightContent);
                reportDetailResponse.setInsightInfo(insightInfo);
            }

            //생성된 AI리포트 상세 정보
            return new ReportResponseWrapDTO(createStatusResponse, reportDetailResponse);
        }
    }

    /**
     * AI리포트 히스토리 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReportListResponseDTO> getReportList(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException();
        }
        //커플 찾기
        CoupleEntity coupleInfo = coupleRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("커플정보를 찾을 수 없습니다."));
        //커플이 생성한 AI리포트 목록
        List<Report> reportList = reportRepository.findAllByCoupleEntityAndDelYnOrderByRegDtDesc(coupleInfo, "N");

        return reportList.stream()
                .map(r -> {
                    //리포트 생성 연, 월, 주차 정보
                    LocalDate regLocalDate = r.getRegDt().toLocalDate();
                    String year = String.valueOf(regLocalDate.getYear());
                    String month = String.valueOf(regLocalDate.getMonth().getValue());
                    WeekFields wf = WeekFields.of(java.util.Locale.KOREA);
                    String week = String.valueOf(regLocalDate.get(wf.weekOfMonth()));
                    return new ReportListResponseDTO(year, month, week, r.getId());
                }).toList();
    }

    /**
     * AI 리포트 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public ReportDetailResponseDTO getReportDetail(Long userId, Long reportId) {
        if (reportId == null || userId == null) {
            throw new IllegalArgumentException();
        }

        //커플 찾기
        Optional<CoupleEntity> coupleInfo = coupleRepository.findByUserId(userId);
        if (coupleInfo.isEmpty() || coupleInfo.get().getDelYn().equals("Y")) {
            throw new IllegalArgumentException("커플정보를 찾을 수 없습니다.");
        }
        //AI 리포트 찾기
        Report findReport = reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("리포트 정보를 찾을 수 없습니다."));
        if (findReport.getDelYn().equals("Y")) {
            throw new IllegalArgumentException("삭제된 AI리포트입니다.");
        }

        //인사이트 조회
        Long insightId = findReport.getInsightId();
        String insightContent = getInsight(insightId);

        ReportDetailResponseDTO reportInfo = getReportInfo(findReport, coupleInfo.get());
        ReportDetailResponseDTO.InsightInfo insightInfo = ReportDetailResponseDTO.InsightInfo.from(insightId, insightContent);
        reportInfo.setInsightInfo(insightInfo);
        return reportInfo;
    }

    /**
     * AI 리포트 생성
     */
    @Transactional
    public Report createReport(LocalDateTime startDt, LocalDateTime endDt, Long userId1, Long userId2, CoupleEntity coupleEntity) {
        //주간 일자
        //모두 응답한 데일리 카드 중 경제관인 것
        List<SyncAnswerDTO> economyCardAnswerList = dailyCardUserAnswerRepository.findDailyCardAnswerBySubject(CardSubject.ECONOMY, userId1, userId2,QuestionType.MULTIPLE_CHOICE, startDt, endDt, "N");
        int totalCntOfEconomyCard = economyCardAnswerList.size();
        int sameAnswerOfEconomyCard = economyCardAnswerList.stream().filter(a-> Objects.equals(a.answerContent1(), a.answerContent2())).toList().size();
        //경제관 싱크로율 계산
        String economySyncRate = "0";
        if (totalCntOfEconomyCard != 0) {
            BigDecimal resultOfEconomy = BigDecimal.valueOf(sameAnswerOfEconomyCard)
                    .divide(BigDecimal.valueOf(totalCntOfEconomyCard), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP);
            economySyncRate = String.valueOf(resultOfEconomy.intValue());
        }

        //모두 응답한 데일리 카드 중 생활관인 것
        List<SyncAnswerDTO> lifeCardAnswerList = dailyCardUserAnswerRepository.findDailyCardAnswerBySubject(CardSubject.LIFESTYLE, userId1, userId2, QuestionType.MULTIPLE_CHOICE, startDt, endDt, "N");
        int totalCntOfLifeStyleCard = lifeCardAnswerList.size();
        int sameAnswerOfLifeStyleCard = lifeCardAnswerList.stream().filter(a-> Objects.equals(a.answerContent1(), a.answerContent2())).toList().size();
        //생활관 싱크로율 계산
        String lifeSyncRate = "0";
        if (totalCntOfLifeStyleCard != 0) {
            BigDecimal resultOfLifeStyle = BigDecimal.valueOf(sameAnswerOfLifeStyleCard)
                    .divide(BigDecimal.valueOf(totalCntOfLifeStyleCard), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP);

            lifeSyncRate = String.valueOf(resultOfLifeStyle.intValue());
        }

        //모두 응답한 데일리 카드 중 연애관인 것
        List<SyncAnswerDTO> loveCardAnswerList = dailyCardUserAnswerRepository.findDailyCardAnswerBySubject(CardSubject.LOVE, userId1, userId2,QuestionType.MULTIPLE_CHOICE, startDt, endDt, "N");
        int totalCntOfLoveCard = loveCardAnswerList.size();
        int sameAnswerOfLoveCard = loveCardAnswerList.stream().filter(a-> Objects.equals(a.answerContent1(), a.answerContent2())).toList().size();
        //연애관 싱크로율 계산
        String loveSyncRate = "0";
        if (totalCntOfLoveCard != 0) {
            BigDecimal resultOfLove = BigDecimal.valueOf(sameAnswerOfLoveCard)
                    .divide(BigDecimal.valueOf(totalCntOfLoveCard), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP);

            loveSyncRate = String.valueOf(resultOfLove.intValue());
        }

        //전체싱크로율 계산
        BigDecimal resultOfTotalSyncRate = BigDecimal.valueOf(Integer.valueOf(economySyncRate)+
                        Integer.valueOf(lifeSyncRate)+
                        Integer.valueOf(loveSyncRate))
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)
                .setScale(0, RoundingMode.HALF_UP);

        String totalSyncRate = String.valueOf(resultOfTotalSyncRate.intValue());

        //대화참여율
        BigDecimal resultOfPaartipationRate = BigDecimal.valueOf(totalCntOfEconomyCard+totalCntOfLifeStyleCard+totalCntOfLoveCard)
                .divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);

        String participationRate = String.valueOf(resultOfPaartipationRate.intValue());
        
        //대화누적자산
        int bothAnswerCnt = dailyCardUserAnswerRepository.findAllDailyCardAnswer(userId1, userId2, QuestionType.MULTIPLE_CHOICE,"N");

        //비슷했던 주제
        List<SimilarAnswer> similarAnswerList = new ArrayList<>();
        List<DiffrentAnswer> diffrentAnswerList = new ArrayList<>();
        List<SyncAnswerDTO> totalAnswer = new ArrayList<>();
        totalAnswer.addAll(economyCardAnswerList);      //경제관
        totalAnswer.addAll(lifeCardAnswerList);         //생활관
        totalAnswer.addAll(loveCardAnswerList);         //연애관
        totalAnswer.forEach(ea -> {
            //Similar Answer Entity 생성
            if(ea.answerContent1().equals(ea.answerContent2())) {
                similarAnswerList.add(SimilarAnswer.builder()
                        .coupleCardId(ea.coupleCardId())
                        .answerId1(ea.answerId1())
                        .answerId2(ea.answerId2())
                        .delYn("N")
                        .regrId(userId1)
                        .updrId(userId1)
                        .build());
            }
            else {
                //Diffrent Answer Entity 생성
                diffrentAnswerList.add(DiffrentAnswer.builder()
                        .coupleCardId(ea.coupleCardId())
                        .answerId1(ea.answerId1())
                        .answerId2(ea.answerId2())
                        .delYn("N")
                        .regrId(userId1)
                        .updrId(userId1)
                        .build());
            }
        });

        //인사이트 매필
        //시작: 월요일 00:00
        LocalDate startDTOfInsight = startDt.toLocalDate();
        //종료: 저번 주 월요일 00:00
        LocalDate endDTOfInsight= endDt.toLocalDate();
        Long insightId = mappingInsight(coupleEntity, startDTOfInsight, endDTOfInsight);

        //리포트 생성
        Report newReport = Report.builder()
                .totalSyncRate(totalSyncRate)
                .economySyncRate(economySyncRate)
                .lifeSyncRate(lifeSyncRate)
                .loveSyncRate(loveSyncRate)
                .answerRate(participationRate)
                .totalAnswerCnt(String.valueOf(bothAnswerCnt))
                .insightId(insightId)
                .strtDt(startDt.toLocalDate())
                .endDt(endDt.toLocalDate())
                .regrId(userId1)
                .updrId(userId1)
                .delYn("N")
                .coupleEntity(coupleEntity)
                .build();

        newReport.addSimilarAnswer(similarAnswerList);
        newReport.addDifferentAnswer(diffrentAnswerList);

        //저장 후 반횐
        return reportRepository.save(newReport);
    }

    /**
     * AI 리포트 응답 DTO 생성
     */
    public ReportDetailResponseDTO getReportInfo(Report report, CoupleEntity coupleEntity) {
        //비슷했던 주제 목록
        List<ReportDetailResponseDTO.SimpleDailycardInfoDTO> similarAnswerInfoList = report.getSimilarAnswerList().stream()
                .map(answer -> {
                    CoupleDailyCardEntity coupleDailyCard = coupleDailyCardRepository.findByCoupleIdAndCoupleCardIdAndDelYn(coupleEntity.getCoupleId(), answer.getCoupleCardId(), "N").orElseThrow(() -> new IllegalArgumentException("커플에게 배정되지 않은 데일리카드입니다."));
                    DailyCardEntity dailyCard = coupleDailyCard.getDailyCard();

                    return ReportDetailResponseDTO.SimpleDailycardInfoDTO.builder()
//                            .answerId1(answer.getAnswerId1())
//                            .answerId2(answer.getAnswerId2())
                            .coupleCardId(answer.getCoupleCardId())
                            .issuedDt(coupleDailyCard.getIssuedDate())
                            .mode(dailyCard.getMode().getDisplayName())
                            .subject(dailyCard.getSubject().getDisplayName())
                            .build();
                }).toList();

        //대화가 더 필요한 주제 목록
        List<ReportDetailResponseDTO.SimpleDailycardInfoDTO> diffrentAnswerInfoList = report.getDifferentAnswerList().stream()
                .map(answer -> {
                    CoupleDailyCardEntity coupleDailyCard = coupleDailyCardRepository.findByCoupleIdAndCoupleCardIdAndDelYn(coupleEntity.getCoupleId(), answer.getCoupleCardId(), "N").orElseThrow(() -> new IllegalArgumentException("커플에게 배정되지 않은 데일리카드입니다."));
                    DailyCardEntity dailyCard = coupleDailyCard.getDailyCard();

                    return ReportDetailResponseDTO.SimpleDailycardInfoDTO.builder()
//                            .answerId1(answer.getAnswerId1())
//                            .answerId2(answer.getAnswerId2())
                            .coupleCardId(answer.getCoupleCardId())
                            .issuedDt(coupleDailyCard.getIssuedDate())
                            .mode(dailyCard.getMode().getDisplayName())
                            .subject(dailyCard.getSubject().getDisplayName())
                            .build();
                }).toList();

        return ReportDetailResponseDTO.builder()
                .reportId(report.getId())
                .totalSyncRate(report.getTotalSyncRate())
                .economySyncRate(report.getEconomySyncRate())
                .lifeSyncRate(report.getLifeSyncRate())
                .loveSyncRate(report.getLoveSyncRate())
                .dailycardAnswerRate(report.getAnswerRate())
                .totalDailycardAnswerCnt(report.getTotalAnswerCnt())
                .startDt(report.getStrtDt())
                .endDt(report.getEndDt())
                .similarSubjectList(similarAnswerInfoList)
                .diffrentSubjectList(diffrentAnswerInfoList)
                .build();
    }

    private Long mappingInsight(CoupleEntity couple, LocalDate startDt, LocalDate endDt) {
        Insight insight = insightRepository.findByCoupleIdAndStartDtAndEndDtAndDelYn(couple.getCoupleId(), startDt, endDt, "N")
                .orElse(null);

        return insight == null ? null : insight.getId();
    }

    private String getInsight(Long insightId) {
        if (insightId == null) {
            return null;
        }

        Insight insight = insightRepository.findById(insightId)
                .orElseThrow(() -> new IllegalStateException("[인사이트ID : " + insightId +" ]에 해당하는 인사이트가 존재하지 않습니다."));

        StringBuilder insightData = new StringBuilder();
        if (insight != null) {
            insightData.append(insight.getSummary()).append("\n");
            if (insight.getEconomyPart() != null) {
                insightData.append(insight.getEconomyPart()).append("\n");
            }
            if (insight.getLifestylePart() != null) {
                insightData.append(insight.getLifestylePart()).append("\n");
            }
            if (insight.getLovePart() != null) {
                insightData.append(insight.getLovePart());
            }
        }

        return insightData.toString();
    }
}
