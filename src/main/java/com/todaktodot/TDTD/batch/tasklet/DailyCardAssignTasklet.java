package com.todaktodot.TDTD.batch.tasklet;

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

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DailyCardAssignTasklet implements Tasklet {

   private final CoupleRepository coupleRepository;
   private final FcmService fcmService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("=====데일리 카드 도착 알림 배치 시작=====");

        //모든 커플 조회
        List<CoupleEntity> connectedCouples = coupleRepository.findConnectedCouples();

        if (connectedCouples.isEmpty()) {
            log.debug("연결된 커플이 없습니다.");
            log.debug("=====데일리카드 도착 알림 배치 완료=====");
            return RepeatStatus.FINISHED;
        }

        for(CoupleEntity couple : connectedCouples) {
            Long userId1 = couple.getUserId1();
            Long userId2 = couple.getUserId2();

            if (userId1 == null || userId2 == null) continue;

            //푸시알림 메세지 생성
            PushMessage pushMessage = PushMessage.dailyCard(couple.getCoupleId(), 0L);

            //전송
            fcmService.sendToUsers(List.of(userId1, userId2), pushMessage);
        }

        log.debug("=====데일리 카드 도착 알림 배치 완료=====");
        return RepeatStatus.FINISHED;
    }
}
