package com.todaktodot.TDTD.batch.tasklet;

import com.todaktodot.TDTD.batch.report.DailyCardBatchReportFormatter;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignBatchResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.service.DailyCardService;
import com.todaktodot.TDTD.global.alert.DiscordNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class DailyCardBatchAssignTasklet implements Tasklet {

    private final DailyCardService dailyCardService;
    private final DailyCardBatchReportFormatter dailyCardBatchReportFormatter;
    private final DiscordNotificationService discordNotificationService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("=====데일리 카드 자동 배정 배치 시작=====");

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(6);

        try {
            AssignBatchResponseDTO response = dailyCardService.assignDailyCardsForCouples(startDate, endDate);

            log.info(
                    "데일리 카드 자동 배정 결과: startDate={}, endDate={}, days={}, coupleCount={}, assignedCount={}, skippedDateCount={}",
                    response.getStartDate(),
                    response.getEndDate(),
                    response.getDays(),
                    response.getCoupleCount(),
                    response.getAssignedCount(),
                    response.getSkippedDateCount()
            );
            sendSuccessReport(response);

        } catch (Exception e) {
            log.error("데일리 카드 자동 배정 배치 중 오류 발생", e);
            try {
                discordNotificationService.sendErrorNotificationForBatch(
                        String.format("데일리카드 자동 배정 실패: %s", e.getMessage()));
            } catch (Exception notificationException) {
                log.warn("데일리카드 배치 에러 알림 전송 실패", notificationException);
            }
            throw e;
        }

        log.debug("=====데일리 카드 자동 배정 배치 완료=====");
        return RepeatStatus.FINISHED;
    }

    private void sendSuccessReport(AssignBatchResponseDTO response) {
        try {
            String successMessage = dailyCardBatchReportFormatter.formatDetailedSuccessMessage(response);
            discordNotificationService.sendSuccessNotificationForBatch(successMessage);
        } catch (Exception e) {
            log.warn("데일리카드 배치 상세 보고 생성 실패, 기본 요약으로 대체합니다.", e);
            sendFallbackSuccessReport(response);
        }
    }

    private void sendFallbackSuccessReport(AssignBatchResponseDTO response) {
        try {
            String fallbackMessage = dailyCardBatchReportFormatter.formatFallbackSuccessMessage(response);
            discordNotificationService.sendSuccessNotificationForBatch(fallbackMessage);
        } catch (Exception e) {
            log.warn("데일리카드 배치 기본 보고 전송 실패", e);
        }
    }
}
