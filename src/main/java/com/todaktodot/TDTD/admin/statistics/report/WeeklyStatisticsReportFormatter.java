package com.todaktodot.TDTD.admin.statistics.report;

import com.todaktodot.TDTD.admin.statistics.dto.WeeklyStatisticsDTO;
import com.todaktodot.TDTD.global.alert.DiscordNotificationService.DiscordEmbedField;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WeeklyStatisticsReportFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String buildTitle(WeeklyStatisticsDTO statistic) {
        return "📊 투닥투닷 주간 핵심 지표";
    }

    public String buildDescription(WeeklyStatisticsDTO statistic) {
        return String.format(
                "**%s ~ %s** 기간의 요청 기준 주간 통계입니다.",
                statistic.getPeriodStartDate().format(DATE_FORMATTER),
                statistic.getPeriodEndDate().format(DATE_FORMATTER)
        );
    }

    public List<DiscordEmbedField> buildFields(WeeklyStatisticsDTO statistic) {
        List<DiscordEmbedField> fields = new ArrayList<>();
        fields.add(new DiscordEmbedField("기간", statistic.getPeriodLabel(), false));
        fields.add(new DiscordEmbedField("전체 유저 수", formatCount(statistic.getTotalUserCount()), true));
        fields.add(new DiscordEmbedField("전체 커플 수", formatCount(statistic.getTotalCoupleCount()), true));
        fields.add(new DiscordEmbedField("선택 완료 데일리카드 수", formatCount(statistic.getDailyCardCount()), true));
        fields.add(new DiscordEmbedField("답변 완료 유저 수", formatCount(statistic.getAnsweredUserCount()), true));
        fields.add(new DiscordEmbedField("답변 완료율(개인)", formatRate(statistic.getPersonalAnswerRate()), true));
        fields.add(new DiscordEmbedField("히스토리카드 수", formatCount(statistic.getHistoryCardCount()), true));
        fields.add(new DiscordEmbedField("AI 피드백 생성 수", formatCount(statistic.getAiFeedbackCount()), true));
        fields.add(new DiscordEmbedField("답변 완료율(커플 둘 다)", formatRate(statistic.getCoupleBothAnswerRate()), true));
        fields.add(new DiscordEmbedField("산출식 메모",
                "개인 완료율은 `답변한 유저-카드 수 / 선택 완료 카드의 답변 기회 수`, 커플 완료율은 `양쪽 모두 답변한 카드 수 / 선택 완료 카드 수` 기준입니다.",
                false));
        return fields;
    }

    private String formatCount(Long value) {
        return String.format("%,d", value != null ? value : 0L);
    }

    private String formatRate(Object value) {
        return value != null ? value + "%" : "0%";
    }
}
