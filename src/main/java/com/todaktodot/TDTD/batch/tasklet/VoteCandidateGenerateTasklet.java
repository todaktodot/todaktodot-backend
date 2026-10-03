package com.todaktodot.TDTD.batch.tasklet;

import com.todaktodot.TDTD.batch.report.VoteCandidateReportFormatter;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCandidateDTO;
import com.todaktodot.TDTD.domain.vote.service.VoteCandidateService;
import com.todaktodot.TDTD.global.alert.DiscordNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class VoteCandidateGenerateTasklet implements Tasklet {

    private final VoteCandidateService voteCandidateService;
    private final VoteCandidateReportFormatter voteCandidateReportFormatter;
    private final DiscordNotificationService discordNotificationService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("=====투표 AI 후보 생성 배치 시작=====");

        try {
            List<VoteCandidateDTO> candidates = voteCandidateService.generate();

            log.info("투표 AI 후보 생성 결과: candidateCount={}", candidates.size());
            sendSuccessReport(candidates);

        } catch (Exception e) {
            //스케줄러가 예외를 삼키므로 실패 알림은 여기서 직접 보낸다.
            log.error("투표 AI 후보 생성 배치 중 오류 발생", e);
            try {
                discordNotificationService.sendErrorNotificationForBatch(
                        String.format("투표 AI 후보 생성 실패: %s", e.getMessage()));
            } catch (Exception notificationException) {
                log.warn("투표 후보 배치 에러 알림 전송 실패", notificationException);
            }
            throw e;
        }

        log.debug("=====투표 AI 후보 생성 배치 완료=====");
        return RepeatStatus.FINISHED;
    }

    private void sendSuccessReport(List<VoteCandidateDTO> candidates) {
        try {
            discordNotificationService.sendVoteCandidateReport(
                    voteCandidateReportFormatter.buildDescription(candidates),
                    voteCandidateReportFormatter.buildFields(candidates));
        } catch (Exception e) {
            //알림 실패로 생성된 후보를 날리지 않는다.
            log.warn("투표 후보 생성 보고 전송 실패", e);
        }
    }
}
