package com.todaktodot.TDTD.batch.tasklet;

import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import com.todaktodot.TDTD.domain.aireport.service.ReportService;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.insight.dto.GenerateInsightRequestDTO;
import com.todaktodot.TDTD.domain.insight.dto.GenerateInsightResponseDTO;
import com.todaktodot.TDTD.domain.insight.service.InsightService;
import com.todaktodot.TDTD.global.alert.DiscordNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiReportCreateTasklet implements Tasklet {

   private final ReportService reportService;
   private final InsightService insightService;
   private final DiscordNotificationService discordNotificationService;
   private final CoupleRepository coupleRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("=====AI 리포트 생성 배치 시작=====");

        //만약 현재가 월요일이 아니라면 월요일로 맞추기
        LocalDate endDt = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startDt = endDt.minusWeeks(1);

        //모든 커플 리스트 조회
        List<CoupleEntity> connectedCouples = coupleRepository.findConnectedCouples();

        if (connectedCouples.isEmpty()) {
            log.debug("연결된 커플이 없습니다");
            log.debug("=====AI리포트 생성 배치 완료=====");
            return RepeatStatus.FINISHED;
        }

        for(CoupleEntity couple : connectedCouples) {

            Long userId1 = couple.getUserId1();
            Long userId2 = couple.getUserId2();

            if (userId1 == null || userId2 == null) continue;

            try {
                //1. 인사이트 생성
                GenerateInsightResponseDTO generateInsightResponseDTO = insightService.generateInsight(new GenerateInsightRequestDTO(couple.getCoupleId(), endDt));

                if (generateInsightResponseDTO == null) continue;

                Long insightId = generateInsightResponseDTO.getInsightId();
                if (insightId == null) continue;

                //2. 리포트 생성
                Report createdReport = reportService.createReport(couple, insightId, startDt, endDt);
                if (createdReport == null) continue;
                log.debug("====AI리포트가 생성되었습니다.====");
                log.debug("커플 ID :{}", couple.getCoupleId());
                log.debug("AI리포트 ID {}",createdReport.getId());
                log.debug("생성 날짜 : {} ~ {}", startDt, endDt);
                log.debug("============================");

            } catch (IllegalStateException | IllegalArgumentException e) {
                String message = String.format("배치에서 AI 리포트 생성 실패, 커플 ID : %s, 메세지 : %s", couple.getCoupleId(), e.getMessage());
                log.error(message);

                if (!e.getMessage().equals("모두 응답한 데일리카드가 존재하지 않습니다.")) {
                    discordNotificationService.sendErrorNotificationForBatch(message);
                }
            } catch (Exception e) {
                log.error("알 수 없는 예외로 AI리포트 생성 실패, 커플ID : {}, 메세지 : {}", couple.getCoupleId(), e.getMessage());
            }
        }

        log.debug("=====AI리포트 생성 배치 완료=====");
        return RepeatStatus.FINISHED;
    }
}
