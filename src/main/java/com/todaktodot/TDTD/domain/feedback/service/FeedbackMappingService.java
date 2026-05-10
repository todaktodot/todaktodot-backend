package com.todaktodot.TDTD.domain.feedback.service;

import com.todaktodot.TDTD.domain.feedback.dto.response.FeedbackResponseDTO;
import com.todaktodot.TDTD.domain.feedback.repository.CoupleDailyCardFeedbackRepository;
import com.todaktodot.TDTD.domain.feedback.repository.DailyCardFeedbackRepository;
import com.todaktodot.TDTD.domain.feedback.repository.entity.CoupleDailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.FeedbackGenerationStatus;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackMappingService {

    private final CoupleDailyCardFeedbackRepository coupleDailyCardFeedbackRepository;
    private final DailyCardFeedbackRepository dailyCardFeedbackRepository;

    @Transactional
    public FeedbackResponseDTO createGeneratingOrReturnExisting(Long coupleCardId, Long userId) {
        return coupleDailyCardFeedbackRepository.findByCoupleCardIdAndDelYn(coupleCardId, "N")
                .map(mapping -> claimExistingOrReturnResponse(mapping, userId))
                .orElseGet(() -> insertGenerating(coupleCardId, userId));
    }

    @Transactional
    public FeedbackResponseDTO retryClaimNotStartedOrReturnExisting(Long coupleCardId, Long userId) {
        int updated = transitionNotStartedToGenerating(coupleCardId, userId);
        if (updated == 1) {
            return null;
        }
        return responseForExistingGenerateMapping(coupleCardId);
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDTO responseForExistingGenerateMapping(Long coupleCardId) {
        return coupleDailyCardFeedbackRepository.findByCoupleCardIdAndDelYn(coupleCardId, "N")
                .map(this::responseForExistingMappingForGenerate)
                .orElseThrow(() -> new IllegalStateException("피드백 생성 요청이 중복되었지만 기존 매핑을 찾을 수 없습니다."));
    }

    @Transactional
    public FeedbackResponseDTO markCompletedIfGenerating(Long coupleCardId, Long feedbackId, Long userId) {
        if (feedbackId == null) {
            throw new IllegalArgumentException("완료 상태에는 feedbackId가 필요합니다.");
        }

        int updated = coupleDailyCardFeedbackRepository.markCompletedIfGenerating(
                coupleCardId,
                feedbackId,
                userId,
                FeedbackGenerationStatus.GENERATING,
                FeedbackGenerationStatus.COMPLETED,
                LocalDateTime.now());

        if (updated == 1) {
            DailyCardFeedbackEntity feedback = dailyCardFeedbackRepository.findById(feedbackId)
                    .filter(saved -> "N".equals(saved.getDelYn()))
                    .orElseThrow(() -> new IllegalStateException("완료된 피드백을 찾을 수 없습니다."));
            return FeedbackResponseDTO.from(feedback);
        }

        return responseForExistingGenerateMapping(coupleCardId);
    }

    @Transactional
    public boolean markFailedIfGenerating(Long coupleCardId, RuntimeException cause, Long userId) {
        return coupleDailyCardFeedbackRepository.markFailedIfGenerating(
                coupleCardId,
                toFailureMessage(cause),
                userId,
                FeedbackGenerationStatus.GENERATING,
                FeedbackGenerationStatus.FAILED,
                LocalDateTime.now()) == 1;
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDTO getFeedbackResponseForGet(Long coupleCardId) {
        return coupleDailyCardFeedbackRepository.findByCoupleCardIdAndDelYn(coupleCardId, "N")
                .map(this::responseForExistingMappingForGet)
                .orElseGet(FeedbackResponseDTO::notStarted);
    }

    private FeedbackResponseDTO claimExistingOrReturnResponse(CoupleDailyCardFeedbackEntity mapping, Long userId) {
        FeedbackGenerationStatus status = mapping.resolveStatus();
        if (status != FeedbackGenerationStatus.NOT_STARTED) {
            return responseForExistingMappingForGenerate(mapping);
        }

        int updated = transitionNotStartedToGenerating(mapping.getCoupleCardId(), userId);
        if (updated == 1) {
            return null;
        }

        return responseForExistingGenerateMapping(mapping.getCoupleCardId());
    }

    private int transitionNotStartedToGenerating(Long coupleCardId, Long userId) {
        return coupleDailyCardFeedbackRepository.transitionNotStartedToGenerating(
                coupleCardId,
                userId,
                FeedbackGenerationStatus.NOT_STARTED,
                FeedbackGenerationStatus.GENERATING,
                LocalDateTime.now());
    }

    private FeedbackResponseDTO insertGenerating(Long coupleCardId, Long userId) {
        CoupleDailyCardFeedbackEntity mapping = CoupleDailyCardFeedbackEntity.builder()
                .coupleCardId(coupleCardId)
                .regrId(userId)
                .updrId(userId)
                .build();
        mapping.markGenerating(userId);

        coupleDailyCardFeedbackRepository.saveAndFlush(mapping);
        return null;
    }

    private FeedbackResponseDTO responseForExistingMappingForGenerate(CoupleDailyCardFeedbackEntity mapping) {
        FeedbackGenerationStatus status = mapping.resolveStatus();

        if (status == FeedbackGenerationStatus.NOT_STARTED) {
            return null;
        }
        if (status == FeedbackGenerationStatus.GENERATING) {
            return FeedbackResponseDTO.generating();
        }
        if (status == FeedbackGenerationStatus.COMPLETED) {
            return FeedbackResponseDTO.from(loadCompletedFeedback(mapping));
        }

        throw duplicateFeedbackRequestException(mapping, null);
    }

    private FeedbackResponseDTO responseForExistingMappingForGet(CoupleDailyCardFeedbackEntity mapping) {
        FeedbackGenerationStatus status = mapping.resolveStatus();

        if (status == FeedbackGenerationStatus.NOT_STARTED) {
            return FeedbackResponseDTO.notStarted();
        }
        if (status == FeedbackGenerationStatus.GENERATING) {
            return FeedbackResponseDTO.generating();
        }
        if (status == FeedbackGenerationStatus.FAILED) {
            return FeedbackResponseDTO.failed();
        }
        if (status == FeedbackGenerationStatus.COMPLETED) {
            return FeedbackResponseDTO.from(loadCompletedFeedback(mapping));
        }

        throw duplicateFeedbackRequestException(mapping, null);
    }

    private IllegalStateException duplicateFeedbackRequestException(CoupleDailyCardFeedbackEntity mapping, Throwable cause) {
        FeedbackGenerationStatus status = mapping.resolveStatus();

        if (status == FeedbackGenerationStatus.FAILED) {
            return new IllegalStateException("해당 카드의 피드백 생성이 실패했습니다. 재시도 정책 확정 후 다시 요청해주세요.", cause);
        }

        return new IllegalStateException("이미 해당 카드에 대한 피드백 생성 요청이 존재합니다.", cause);
    }

    private DailyCardFeedbackEntity loadCompletedFeedback(CoupleDailyCardFeedbackEntity mapping) {
        Long feedbackId = mapping.getFeedbackId();
        if (feedbackId == null) {
            throw new IllegalStateException("완료된 피드백 매핑에 feedbackId가 없습니다.");
        }

        return dailyCardFeedbackRepository.findById(feedbackId)
                .filter(feedback -> "N".equals(feedback.getDelYn()))
                .orElseThrow(() -> new IllegalStateException("완료된 피드백을 찾을 수 없습니다."));
    }

    private String toFailureMessage(RuntimeException cause) {
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
