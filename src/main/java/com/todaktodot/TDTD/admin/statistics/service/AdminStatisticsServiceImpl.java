package com.todaktodot.TDTD.admin.statistics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.admin.statistics.dto.StatisticsPageDTO;
import com.todaktodot.TDTD.admin.statistics.dto.WeeklyStatisticsDTO;
import com.todaktodot.TDTD.admin.statistics.repository.AdminStatisticsRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private static final LocalDate LAUNCH_DATE = LocalDate.of(2026, 4, 18);
    private static final long MAX_SEARCH_DAYS = 366;

    private final AdminStatisticsRepository adminStatisticsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public StatisticsPageDTO getWeeklyStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEndDate = endDate != null ? endDate : LocalDate.now();
        LocalDate resolvedStartDate = startDate != null ? startDate : LAUNCH_DATE;

        validateDateRange(resolvedStartDate, resolvedEndDate);

        List<WeeklyStatisticsDTO> weeklyStatistics = adminStatisticsRepository
                .findWeeklyStatistics(resolvedStartDate, resolvedEndDate)
                .stream()
                .map(WeeklyStatisticsDTO::from)
                .toList();

        WeeklyStatisticsDTO latestWeek = CollectionUtils.isEmpty(weeklyStatistics)
                ? null
                : weeklyStatistics.get(weeklyStatistics.size() - 1);

        return StatisticsPageDTO.builder()
                .startDate(resolvedStartDate)
                .endDate(resolvedEndDate)
                .weeklyStatistics(weeklyStatistics)
                .latestWeek(latestWeek)
                .chartDataJson(toChartDataJson(weeklyStatistics))
                .build();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이후일 수 없습니다.");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > MAX_SEARCH_DAYS) {
            throw new IllegalArgumentException("통계 조회 기간은 최대 1년까지 가능합니다.");
        }
    }

    @Override
    public byte[] exportWeeklyStatisticsCsv(LocalDate startDate, LocalDate endDate) {
        StatisticsPageDTO page = getWeeklyStatistics(startDate, endDate);
        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff');
        csv.append("기간,시작일,종료일,전체 유저 수,전체 커플 수,선택 완료 데일리카드 수,답변 완료 유저 수,개인 답변 완료율(%),히스토리카드 수,AI 피드백 생성 수,커플 둘 다 완료율(%)\n");

        for (WeeklyStatisticsDTO statistic : page.getWeeklyStatistics()) {
            appendCsvRow(csv, statistic);
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendCsvRow(StringBuilder csv, WeeklyStatisticsDTO statistic) {
        csv.append(csvValue(statistic.getPeriodLabel())).append(',')
                .append(statistic.getPeriodStartDate()).append(',')
                .append(statistic.getPeriodEndDate()).append(',')
                .append(statistic.getTotalUserCount()).append(',')
                .append(statistic.getTotalCoupleCount()).append(',')
                .append(statistic.getDailyCardCount()).append(',')
                .append(statistic.getAnsweredUserCount()).append(',')
                .append(statistic.getPersonalAnswerRate()).append(',')
                .append(statistic.getHistoryCardCount()).append(',')
                .append(statistic.getAiFeedbackCount()).append(',')
                .append(statistic.getCoupleBothAnswerRate()).append('\n');
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String toChartDataJson(List<WeeklyStatisticsDTO> statistics) {
        Map<String, Object> chartData = new LinkedHashMap<>();
        chartData.put("labels", statistics.stream().map(WeeklyStatisticsDTO::getPeriodLabel).toList());
        chartData.put("totalUsers", statistics.stream().map(WeeklyStatisticsDTO::getTotalUserCount).toList());
        chartData.put("totalCouples", statistics.stream().map(WeeklyStatisticsDTO::getTotalCoupleCount).toList());
        chartData.put("dailyCards", statistics.stream().map(WeeklyStatisticsDTO::getDailyCardCount).toList());
        chartData.put("answeredUsers", statistics.stream().map(WeeklyStatisticsDTO::getAnsweredUserCount).toList());
        chartData.put("personalAnswerRates", statistics.stream().map(WeeklyStatisticsDTO::getPersonalAnswerRate).toList());
        chartData.put("historyCards", statistics.stream().map(WeeklyStatisticsDTO::getHistoryCardCount).toList());
        chartData.put("aiFeedbacks", statistics.stream().map(WeeklyStatisticsDTO::getAiFeedbackCount).toList());
        chartData.put("coupleBothAnswerRates", statistics.stream().map(WeeklyStatisticsDTO::getCoupleBothAnswerRate).toList());

        try {
            return objectMapper.writeValueAsString(chartData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("통계 차트 데이터를 생성하지 못했습니다.", e);
        }
    }
}
