package com.todaktodot.TDTD.domain.feedback.event;

import com.todaktodot.TDTD.domain.feedback.dto.reqeust.GenerateFeedbackRequestDTO;
import com.todaktodot.TDTD.domain.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BothAnswersCompletedEventListener {

    private final FeedbackService feedbackService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBothAnswersCompleted(BothAnswersCompletedEvent event) {
        log.info("양쪽 답변 완료 이벤트 수신: coupleCardId={}, userId={}",
                event.coupleCardId(), event.userId());
        try {
            GenerateFeedbackRequestDTO requestDTO = new GenerateFeedbackRequestDTO(
                    event.coupleCardId(),
                    event.cardId(),
                    event.issuedDate()
            );
            feedbackService.generateFeedback(event.userId(), requestDTO);
            log.info("AI 피드백 자동 생성 완료: coupleCardId={}", event.coupleCardId());
        } catch (Exception e) {
            log.error("AI 피드백 자동 생성 실패: coupleCardId={}, error={}",
                    event.coupleCardId(), e.getMessage(), e);
        }
    }
}
