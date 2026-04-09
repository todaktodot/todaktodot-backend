package com.todaktodot.TDTD.batch.report;

import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignBatchResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class DailyCardBatchReportFormatter {

    private static final int EXAMPLE_LIMIT = 3;
    private static final int TITLE_LIMIT = 42;
    private static final int COUPLE_NAME_LIMIT = 18;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd(E)", Locale.KOREAN);

    public String formatDetailedSuccessMessage(AssignBatchResponseDTO response) {
        List<AssignBatchResponseDTO.AssignedDayDetail> assignedDetails = getAssignedDetails(response);

        return String.format(
                "**데일리카드 자동 배정 완료**\n"
                        + "- **기간**: %s ~ %s (%d일)\n"
                        + "- **처리 커플**: %d쌍\n"
                        + "- **신규 배정**: %d장\n"
                        + "- **기존 배정 유지**: %d일\n"
                        + "- **이번 실행 모드 분포**: %s\n"
                        + "- **이번 실행 주제 분포**: %s\n"
                        + "- **이번 실행 유형 분포**: %s\n"
                        + "- **이번 실행 일자별 결과**\n%s\n"
                        + "- **이번 실행 배정 예시**\n%s",
                response.getStartDate(),
                response.getEndDate(),
                response.getDays(),
                response.getCoupleCount(),
                response.getAssignedCount(),
                response.getSkippedDateCount(),
                formatDistribution(assignedDetails, AssignBatchResponseDTO.AssignedDayDetail::getModeDisplayName),
                formatDistribution(assignedDetails, AssignBatchResponseDTO.AssignedDayDetail::getSubjectDisplayName),
                formatCardTypeDistribution(assignedDetails),
                formatDaySummaries(response.getAssignedDayDetails()),
                formatExamples(assignedDetails)
        );
    }

    public String formatFallbackSuccessMessage(AssignBatchResponseDTO response) {
        return String.format(
                "**데일리카드 자동 배정 완료**\n"
                        + "- **기간**: %s ~ %s (%d일)\n"
                        + "- **처리 커플**: %d쌍\n"
                        + "- **신규 배정**: %d장\n"
                        + "- **기존 배정 유지**: %d일",
                response.getStartDate(),
                response.getEndDate(),
                response.getDays(),
                response.getCoupleCount(),
                response.getAssignedCount(),
                response.getSkippedDateCount()
        );
    }

    private List<AssignBatchResponseDTO.AssignedDayDetail> getAssignedDetails(AssignBatchResponseDTO response) {
        return response.getAssignedDayDetails().stream()
                .filter(detail -> !detail.isSkipped())
                .toList();
    }

    private String formatDistribution(List<AssignBatchResponseDTO.AssignedDayDetail> details,
                                      Function<AssignBatchResponseDTO.AssignedDayDetail, String> valueExtractor) {
        Map<String, Long> counts = details.stream()
                .map(valueExtractor)
                .filter(this::hasText)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (counts.isEmpty()) {
            return "없음";
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> entry.getKey() + " " + entry.getValue() + "건")
                .collect(Collectors.joining(" | "));
    }

    private String formatCardTypeDistribution(List<AssignBatchResponseDTO.AssignedDayDetail> details) {
        Map<String, Long> counts = details.stream()
                .flatMap(detail -> detail.getCards().stream())
                .map(AssignBatchResponseDTO.AssignedCardDetail::getTypeDisplayName)
                .filter(this::hasText)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (counts.isEmpty()) {
            return "없음";
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> entry.getKey() + " " + entry.getValue() + "장")
                .collect(Collectors.joining(" | "));
    }

    private String formatDaySummaries(List<AssignBatchResponseDTO.AssignedDayDetail> dayDetails) {
        if (dayDetails.isEmpty()) {
            return "  - 없음";
        }

        Map<LocalDate, List<AssignBatchResponseDTO.AssignedDayDetail>> dayGroups = new TreeMap<>();
        for (AssignBatchResponseDTO.AssignedDayDetail detail : dayDetails) {
            dayGroups.computeIfAbsent(detail.getIssuedDate(), key -> new ArrayList<>()).add(detail);
        }

        return dayGroups.entrySet().stream()
                .map(entry -> formatDaySummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
    }

    private String formatDaySummary(LocalDate issuedDate, List<AssignBatchResponseDTO.AssignedDayDetail> dayDetails) {
        long assignedCoupleCount = dayDetails.stream()
                .filter(detail -> !detail.isSkipped())
                .count();
        long skippedCoupleCount = dayDetails.stream()
                .filter(AssignBatchResponseDTO.AssignedDayDetail::isSkipped)
                .count();
        int assignedCardCount = dayDetails.stream()
                .filter(detail -> !detail.isSkipped())
                .mapToInt(detail -> detail.getCards().size())
                .sum();

        return String.format(
                "  - %s: 신규 %d장 | 배정 %d쌍 | 스킵 %d쌍",
                formatDate(issuedDate),
                assignedCardCount,
                assignedCoupleCount,
                skippedCoupleCount
        );
    }

    private String formatExamples(List<AssignBatchResponseDTO.AssignedDayDetail> assignedDetails) {
        if (assignedDetails.isEmpty()) {
            return "  1. 없음";
        }

        List<AssignBatchResponseDTO.AssignedDayDetail> sortedDetails = assignedDetails.stream()
                .sorted(Comparator.comparing(AssignBatchResponseDTO.AssignedDayDetail::getIssuedDate)
                        .thenComparing(detail -> defaultIfBlank(detail.getCoupleName(), buildFallbackCoupleName(detail.getCoupleId()))))
                .toList();

        return IntStream.range(0, Math.min(EXAMPLE_LIMIT, sortedDetails.size()))
                .mapToObj(index -> formatExampleRow(index + 1, sortedDetails.get(index)))
                .collect(Collectors.joining("\n"));
    }

    private String formatExampleRow(int index, AssignBatchResponseDTO.AssignedDayDetail detail) {
        String formattedDate = formatDate(detail.getIssuedDate());
        String coupleName = truncate(defaultIfBlank(detail.getCoupleName(), buildFallbackCoupleName(detail.getCoupleId())), COUPLE_NAME_LIMIT);
        String cardTitles = truncate(formatCardTitles(detail.getCards()), TITLE_LIMIT);
        String cardMeta = buildCardMeta(detail);

        return String.format("  %d. %s %s - %s (%s)",
                index,
                formattedDate,
                coupleName,
                cardTitles,
                cardMeta);
    }

    private String buildCardMeta(AssignBatchResponseDTO.AssignedDayDetail detail) {
        String cardMeta = Stream.of(
                        detail.getModeDisplayName(),
                        detail.getSubjectDisplayName()
                )
                .filter(this::hasText)
                .collect(Collectors.joining("·"));

        return hasText(cardMeta) ? cardMeta : "정보없음";
    }

    private String formatCardTitles(List<AssignBatchResponseDTO.AssignedCardDetail> cards) {
        String titles = cards.stream()
                .map(AssignBatchResponseDTO.AssignedCardDetail::getCardTitle)
                .filter(this::hasText)
                .collect(Collectors.joining(" / "));

        return hasText(titles) ? titles : "-";
    }

    private String formatDate(LocalDate issuedDate) {
        return issuedDate != null ? issuedDate.format(DATE_FORMAT) : "--.--";
    }

    private String buildFallbackCoupleName(Long coupleId) {
        return coupleId != null ? "커플#" + coupleId : "? / ?";
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
