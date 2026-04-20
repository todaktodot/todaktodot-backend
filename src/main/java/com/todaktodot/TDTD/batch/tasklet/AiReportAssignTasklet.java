package com.todaktodot.TDTD.batch.tasklet;

import com.todaktodot.TDTD.domain.aireport.repository.ReportRepository;
import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.notification.dto.PushMessage;
import com.todaktodot.TDTD.domain.notification.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiReportAssignTasklet implements Tasklet {

   private final ReportRepository reportRepository;
   private final FcmService fcmService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("=====AI 리포트 도착 알림 배치 시작=====");

        LocalDate endDt = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate startDt = endDt.minusWeeks(1);
        endDt = endDt.minusDays(1);

        //한주동안 생성된 모든 리포트 조회
        List<Report> createdReports = reportRepository.findCreatedReport(startDt, endDt);

        if (createdReports.isEmpty()) {
            log.debug("해당 기간에 생성된 리포트가 없습니다");
            log.debug("=====AI리포트 도착 알림 배치 완료=====");
            return RepeatStatus.FINISHED;
        }

        for(Report report : createdReports) {
            CoupleEntity couple = report.getCoupleEntity();
            if (couple == null) continue;
            if (couple.getDelYn().equals("Y") || !couple.isComplete()) continue;

            Long userId1 = couple.getUserId1();
            Long userId2 = couple.getUserId2();

            //푸시알림 메세지 생성
            PushMessage pushMessage = PushMessage.aiReport(couple.getCoupleId(), report.getId());

            //전송
            fcmService.sendToUsers(List.of(userId1, userId2), pushMessage);
            log.debug("리포트 알림 전송, coupleId : {}, userId1 : {}, userId2 : {}", couple.getCoupleId(), userId1, userId2);
        }

        log.debug("=====AI리포트 도착 알림 배치 완료=====");
        return RepeatStatus.FINISHED;
    }
}
