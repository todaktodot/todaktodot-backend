package com.todaktodot.TDTD.domain.dailycard.dto.response;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.EmojiType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(description = "히스토리카드 상세 리스트 조회 응답")
public class HistoryDetailResponseDTO {

    @Schema(description = "조회 시작일")
    private LocalDate startDate;

    @Schema(description = "조회 종료일")
    private LocalDate endDate;

    @Schema(description = "유저1 ID (커플 테이블의 USER_ID_1)")
    private Long user1Id;

    @Schema(description = "유저2 ID (커플 테이블의 USER_ID_2, 혼자 둘러보기 시 null)")
    private Long user2Id;

    @Schema(description = "히스토리 카드 목록")
    private List<HistoryDetailCardItem> historyCards;

    @Getter
    @Builder
    @Schema(name = "HistoryDetailCardItem", description = "히스토리 카드 항목")
    public static class HistoryDetailCardItem {
        @Schema(description = "배정 날짜")
        private LocalDate issuedDate;

        @Schema(description = "모드")
        private String mode;

        @Schema(description = "주제")
        private String subject;

        @Schema(description = "유형 선택 완료 여부")
        private boolean selected;

        @Schema(description = "유형 선택한 사용자 ID (미선택 시 null)")
        private Long selectedByUserId;

        @Schema(description = "커플 카드 ID (선택 완료 시)")
        private Long coupleCardId;

        @Schema(description = "카드 ID (선택 완료 시)")
        private Long cardId;

        @Schema(description = "카드 제목 (선택 완료 시)")
        private String cardTitle;

        @Schema(description = "유형 (선택 완료 시)")
        private String type;

        @Schema(description = "상황 (선택 완료 시)")
        private String situation;

        @Schema(description = "유저1 답변 여부 (선택 완료 시)")
        private Boolean user1Answered;

        @Schema(description = "유저2 답변 여부 (선택 완료 시)")
        private Boolean user2Answered;

        @Schema(description = "질문 목록 (선택 완료 시)")
        private List<QuestionItem> questions;

        @Schema(description = "AI 피드백 (미생성 시 null)")
        private FeedbackItem feedback;

        @Schema(description = "콕 찌르기 여부 (이미 찌른경우 true)")
        private boolean isPocked;
    }

    @Getter
    @Builder
    @Schema(name = "HistoryDetailQuestionItem", description = "히스토리 상세 질문 항목")
    public static class QuestionItem {
        @Schema(description = "질문 번호")
        private Integer questionNo;

        @Schema(description = "질문 유형 (MULTIPLE_CHOICE / SUBJECTIVE)")
        private String questionType;

        @Schema(description = "질문 내용")
        private String questionContent;

        @Schema(description = "답변 필수 여부")
        private boolean answerRequired;

        @Schema(description = "선택지 목록 (주관식이면 빈 배열)")
        private List<OptionItem> options;

        @Schema(description = "유저1 답변 (미답변 시 null)")
        private String user1Answer;

        @Schema(description = "유저1 답변에 대해 유저2가 남긴 이모지 반응 (미선택 시 null). GOOD=좋아요, HEART=하트, SURPRISE=놀람, CRY=슬픔, ANGRY=화남, POOP=똥", example = "HEART")
        private EmojiType user1Emoji;

        @Schema(description = "유저2 답변 (미답변 시 null)")
        private String user2Answer;

        @Schema(description = "유저2 답변에 대해 유저1이 남긴 이모지 반응 (미선택 시 null). GOOD=좋아요, HEART=하트, SURPRISE=놀람, CRY=슬픔, ANGRY=화남, POOP=똥", example = "GOOD")
        private EmojiType user2Emoji;
    }

    @Getter
    @Builder
    @Schema(name = "HistoryDetailOptionItem", description = "히스토리 상세 선택지 항목")
    public static class OptionItem {
        @Schema(description = "선택지 번호")
        private Integer optionNo;

        @Schema(description = "선택지 내용")
        private String optionContent;
    }

    @Getter
    @Builder
    @Schema(name = "HistoryDetailFeedbackItem", description = "히스토리 상세 AI 피드백")
    public static class FeedbackItem {
        @Schema(description = "피드백 ID")
        private Long feedbackId;

        @Schema(description = "요약")
        private String summary;

        @Schema(description = "공감 포인트")
        private String matchPoints;

        @Schema(description = "차이점")
        private String differences;

        @Schema(description = "대화 시작 질문")
        private String conversationStarter;
    }
}
