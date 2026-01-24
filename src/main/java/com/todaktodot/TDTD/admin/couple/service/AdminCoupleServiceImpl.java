package com.todaktodot.TDTD.admin.couple.service;

import com.todaktodot.TDTD.admin.couple.dto.CoupleDetailDTO;
import com.todaktodot.TDTD.admin.couple.dto.CoupleListDTO;
import com.todaktodot.TDTD.admin.couple.dto.UserSummaryDTO;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.CoupleDailyCardRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.DailyCardUserAnswerRepository;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CoupleDailyCardEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardQuestionEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardUserAnswerEntity;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCoupleServiceImpl implements AdminCoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final CoupleDailyCardRepository coupleDailyCardRepository;
    private final DailyCardRepository dailyCardRepository;
    private final DailyCardUserAnswerRepository dailyCardUserAnswerRepository;

    @Override
    public Page<CoupleListDTO> getCouples(String delYn, Pageable pageable) {
        Page<CoupleEntity> couples = Optional.ofNullable(delYn)
                .filter(value -> !value.isBlank())
                .map(value -> coupleRepository.findByDelYn(value, pageable))
                .orElseGet(() -> coupleRepository.findAll(pageable));

        return couples.map(CoupleListDTO::from);
    }

    @Override
    public CoupleDetailDTO getCouple(Long coupleId) {
        CoupleEntity couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new IllegalArgumentException("커플 정보를 찾을 수 없습니다: " + coupleId));

        List<User> users = userRepository.findByIdIn(List.of(couple.getUserId1(), couple.getUserId2()));
        UserSummaryDTO user1 = null;
        UserSummaryDTO user2 = null;

        for (User user : users) {
            if (user.getId().equals(couple.getUserId1())) {
                user1 = UserSummaryDTO.from(user);
            } else if (user.getId().equals(couple.getUserId2())) {
                user2 = UserSummaryDTO.from(user);
            }
        }

        List<CoupleDailyCardEntity> coupleCards = coupleDailyCardRepository
                .findAllByCoupleIdAndDelYnOrderByIssuedDateDescCoupleCardIdDesc(coupleId, "N")
                .stream()
                .filter(card -> "Y".equals(card.getSelectedYn()))
                .toList();

        if (coupleCards.isEmpty()) {
            return CoupleDetailDTO.of(
                    couple.getCoupleId(),
                    couple.getUserId1(),
                    couple.getUserId2(),
                    couple.getFirstMetDt(),
                    couple.getRelationshipStage() != null ? couple.getRelationshipStage().name() : "-",
                    couple.getConnectedDt(),
                    couple.getDelYn(),
                    couple.getRegDt(),
                    couple.getUpdDt(),
                    user1,
                    user2,
                    List.of()
            );
        }

        List<Long> cardIds = coupleCards.stream()
                .map(CoupleDailyCardEntity::getCardId)
                .distinct()
                .toList();

        Map<Long, DailyCardEntity> cardMap = dailyCardRepository.findAllByIdWithQuestionsAndOptions(cardIds).stream()
                .collect(Collectors.toMap(DailyCardEntity::getCardId, card -> card));

        List<Long> coupleCardIds = coupleCards.stream()
                .map(CoupleDailyCardEntity::getCoupleCardId)
                .toList();

        Map<Long, List<DailyCardUserAnswerEntity>> answersByCoupleCardId = dailyCardUserAnswerRepository
                .findAllByCoupleCardIds(coupleCardIds).stream()
                .collect(Collectors.groupingBy(DailyCardUserAnswerEntity::getCoupleCardId));

        List<CoupleDetailDTO.CoupleDailyCardDTO> dailyCards = new ArrayList<>();

        for (CoupleDailyCardEntity coupleCard : coupleCards) {
            DailyCardEntity dailyCard = cardMap.get(coupleCard.getCardId());
            if (dailyCard == null) {
                continue;
            }

            List<DailyCardUserAnswerEntity> answers = answersByCoupleCardId.getOrDefault(
                    coupleCard.getCoupleCardId(), List.of());

            Map<Integer, List<DailyCardUserAnswerEntity>> answersByQuestion = answers.stream()
                    .collect(Collectors.groupingBy(DailyCardUserAnswerEntity::getQuestionNo));

            List<CoupleDetailDTO.DailyCardQuestionDTO> questions = new ArrayList<>();

            for (DailyCardQuestionEntity question : dailyCard.getQuestions()) {
                List<DailyCardUserAnswerEntity> questionAnswers = answersByQuestion
                        .getOrDefault(question.getQuestionNo(), List.of());

                Map<Long, String> answerByUserId = new HashMap<>();
                for (DailyCardUserAnswerEntity answer : questionAnswers) {
                    answerByUserId.put(answer.getUserId(), answer.getAnswerContent());
                }

                String user1Answer = resolveAnswerText(question, answerByUserId.get(couple.getUserId1()));
                String user2Answer = resolveAnswerText(question, answerByUserId.get(couple.getUserId2()));

                List<CoupleDetailDTO.DailyCardOptionDTO> options = question.getOptions().stream()
                        .map(option -> new CoupleDetailDTO.DailyCardOptionDTO(
                                option.getOptionNo(),
                                option.getOptionCnts()
                        ))
                        .sorted((a, b) -> a.getOptionNo().compareTo(b.getOptionNo()))
                        .toList();

                questions.add(new CoupleDetailDTO.DailyCardQuestionDTO(
                        question.getQuestionNo(),
                        question.getQuestionType(),
                        question.getQuestionCnts(),
                        options,
                        user1Answer,
                        user2Answer
                ));
            }

            questions.sort((a, b) -> a.getQuestionNo().compareTo(b.getQuestionNo()));

            dailyCards.add(new CoupleDetailDTO.CoupleDailyCardDTO(
                    coupleCard.getCoupleCardId(),
                    coupleCard.getCardId(),
                    coupleCard.getIssuedDate(),
                    coupleCard.getSelectedYn(),
                    coupleCard.getDelYn(),
                    dailyCard.getMode(),
                    dailyCard.getSubject(),
                    dailyCard.getType(),
                    dailyCard.getCardTitle(),
                    questions
            ));
        }

        return CoupleDetailDTO.of(
                couple.getCoupleId(),
                couple.getUserId1(),
                couple.getUserId2(),
                couple.getFirstMetDt(),
                couple.getRelationshipStage() != null ? couple.getRelationshipStage().name() : "-",
                couple.getConnectedDt(),
                couple.getDelYn(),
                couple.getRegDt(),
                couple.getUpdDt(),
                user1,
                user2,
                dailyCards
        );
    }

    private String resolveAnswerText(DailyCardQuestionEntity question, String answerContent) {
        if (answerContent == null || answerContent.isBlank()) {
            return null;
        }

        if (question.getQuestionType() == com.todaktodot.TDTD.domain.dailycard.repository.entity.QuestionType.MULTIPLE_CHOICE) {
            for (var option : question.getOptions()) {
                if (answerContent.equals(String.valueOf(option.getOptionNo()))) {
                    return option.getOptionNo() + ". " + option.getOptionCnts();
                }
            }
        }

        return answerContent;
    }

    @Override
    public long getTotalCount() {
        return coupleRepository.count();
    }

    @Override
    public long getActiveCount() {
        return coupleRepository.countByDelYn("N");
    }

    @Override
    public long getInactiveCount() {
        return coupleRepository.countByDelYn("Y");
    }
}
