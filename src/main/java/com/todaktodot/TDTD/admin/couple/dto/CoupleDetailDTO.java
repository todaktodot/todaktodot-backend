package com.todaktodot.TDTD.admin.couple.dto;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.QuestionType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class CoupleDetailDTO {

    private final Long coupleId;
    private final Long userId1;
    private final Long userId2;
    private final LocalDate firstMetDt;
    private final String relationshipStage;
    private final LocalDateTime connectedDt;
    private final String delYn;
    private final LocalDateTime regDt;
    private final LocalDateTime updDt;
    private final UserSummaryDTO user1;
    private final UserSummaryDTO user2;
    private final List<CoupleDailyCardDTO> dailyCards;

    public CoupleDetailDTO(Long coupleId, Long userId1, Long userId2, LocalDate firstMetDt,
                           String relationshipStage, LocalDateTime connectedDt, String delYn,
                           LocalDateTime regDt, LocalDateTime updDt, UserSummaryDTO user1, UserSummaryDTO user2,
                           List<CoupleDailyCardDTO> dailyCards) {
        this.coupleId = coupleId;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.firstMetDt = firstMetDt;
        this.relationshipStage = relationshipStage;
        this.connectedDt = connectedDt;
        this.delYn = delYn;
        this.regDt = regDt;
        this.updDt = updDt;
        this.user1 = user1;
        this.user2 = user2;
        this.dailyCards = dailyCards;
    }

    public static CoupleDetailDTO of(Long coupleId, Long userId1, Long userId2, LocalDate firstMetDt,
                                     String relationshipStage, LocalDateTime connectedDt, String delYn,
                                     LocalDateTime regDt, LocalDateTime updDt, UserSummaryDTO user1,
                                     UserSummaryDTO user2, List<CoupleDailyCardDTO> dailyCards) {
        return new CoupleDetailDTO(
                coupleId,
                userId1,
                userId2,
                firstMetDt,
                relationshipStage,
                connectedDt,
                delYn,
                regDt,
                updDt,
                user1,
                user2,
                dailyCards
        );
    }

    @Getter
    public static class CoupleDailyCardDTO {
        private final Long coupleCardId;
        private final Long cardId;
        private final LocalDate issuedDate;
        private final String selectedYn;
        private final String delYn;
        private final CardMode mode;
        private final CardSubject subject;
        private final CardType type;
        private final String cardTitle;
        private final List<DailyCardQuestionDTO> questions;

        public CoupleDailyCardDTO(Long coupleCardId, Long cardId, LocalDate issuedDate,
                                  String selectedYn, String delYn, CardMode mode,
                                  CardSubject subject, CardType type, String cardTitle,
                                  List<DailyCardQuestionDTO> questions) {
            this.coupleCardId = coupleCardId;
            this.cardId = cardId;
            this.issuedDate = issuedDate;
            this.selectedYn = selectedYn;
            this.delYn = delYn;
            this.mode = mode;
            this.subject = subject;
            this.type = type;
            this.cardTitle = cardTitle;
            this.questions = questions;
        }
    }

    @Getter
    public static class DailyCardQuestionDTO {
        private final Integer questionNo;
        private final QuestionType questionType;
        private final String questionCnts;
        private final List<DailyCardOptionDTO> options;
        private final String user1Answer;
        private final String user2Answer;

        public DailyCardQuestionDTO(Integer questionNo, QuestionType questionType, String questionCnts,
                                    List<DailyCardOptionDTO> options, String user1Answer, String user2Answer) {
            this.questionNo = questionNo;
            this.questionType = questionType;
            this.questionCnts = questionCnts;
            this.options = options;
            this.user1Answer = user1Answer;
            this.user2Answer = user2Answer;
        }

        public String getQuestionTypeDisplayName() {
            return questionType == QuestionType.MULTIPLE_CHOICE ? "객관식" : "주관식";
        }
    }

    @Getter
    public static class DailyCardOptionDTO {
        private final Integer optionNo;
        private final String optionCnts;

        public DailyCardOptionDTO(Integer optionNo, String optionCnts) {
            this.optionNo = optionNo;
            this.optionCnts = optionCnts;
        }
    }
}
