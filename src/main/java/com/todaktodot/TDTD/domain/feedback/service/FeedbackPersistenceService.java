package com.todaktodot.TDTD.domain.feedback.service;

import com.todaktodot.TDTD.domain.feedback.dto.ai.AiGeneratedFeedbackDTO;
import com.todaktodot.TDTD.domain.feedback.repository.AiCardFeedbackInfoRepository;
import com.todaktodot.TDTD.domain.feedback.repository.DailyCardFeedbackRepository;
import com.todaktodot.TDTD.domain.feedback.repository.entity.AiCardFeedbackInfoEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.FeedbackStatus;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackPersistenceService {

    private final DailyCardFeedbackRepository dailyCardFeedbackRepository;
    private final AiCardFeedbackInfoRepository aiCardFeedbackInfoRepository;

    @Transactional
    public DailyCardFeedbackEntity saveFeedbackResult(SaveFeedbackCommand command) {
        AiGeneratedFeedbackDTO aiFeedback = command.aiFeedback();

        DailyCardFeedbackEntity feedback = dailyCardFeedbackRepository.saveAndFlush(
                DailyCardFeedbackEntity.builder()
                        .cardId(command.cardId())
                        .choiceCombinationHash(command.combinationHash())
                        .choiceCombinationRaw(command.rawCombination())
                        .hasSubjective(command.hasSubjectiveFlag())
                        .summary(aiFeedback.getSummary())
                        .matchPoints(toText(aiFeedback.getMatchPoints()))
                        .differences(toText(aiFeedback.getDifferences()))
                        .conversationStarter(aiFeedback.getConversationStarter())
                        .regrId(command.userId())
                        .updrId(command.userId())
                        .build());

        aiCardFeedbackInfoRepository.saveAndFlush(
                AiCardFeedbackInfoEntity.builder()
                        .feedbackId(feedback.getFeedbackId())
                        .promptId(command.promptId())
                        .aiModel(command.aiModel())
                        .temperature(String.valueOf(command.actualTemperature()))
                        .finalPrompt(command.finalPrompt())
                        .aiResponseRaw(command.rawResponse())
                        .status(FeedbackStatus.SUCCESS.name())
                        .regrId(command.userId())
                        .updrId(command.userId())
                        .build());

        return feedback;
    }

    @Transactional(readOnly = true)
    public DailyCardFeedbackEntity loadExistingFeedback(Long cardId, String combinationHash, String hasSubjectiveFlag) {
        return dailyCardFeedbackRepository
                .findByCardIdAndChoiceCombinationHashAndHasSubjectiveAndDelYn(
                        cardId, combinationHash, hasSubjectiveFlag, "N")
                .orElseThrow(() -> new IllegalStateException("피드백 저장에 실패했습니다."));
    }

    private String toText(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream()
                .filter(this::isNonEmpty)
                .collect(Collectors.joining("\n"));
    }

    private boolean isNonEmpty(String value) {
        return value != null && !value.isBlank();
    }

    public record SaveFeedbackCommand(
            Long userId,
            Long cardId,
            String combinationHash,
            String rawCombination,
            String hasSubjectiveFlag,
            AiGeneratedFeedbackDTO aiFeedback,
            String aiModel,
            Long promptId,
            String finalPrompt,
            String rawResponse,
            double actualTemperature
    ) {}
}
