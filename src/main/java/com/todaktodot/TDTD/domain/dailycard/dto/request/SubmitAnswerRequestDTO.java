package com.todaktodot.TDTD.domain.dailycard.dto.request;

import com.todaktodot.TDTD.global.validation.UserAnswer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "데일리카드 답변 제출 요청")
public class SubmitAnswerRequestDTO {

    @NotNull(message = "커플 카드 ID는 필수입니다")
    @Schema(description = "커플 카드 ID", example = "1")
    private Long coupleCardId;

    @NotNull(message = "카드 ID는 필수입니다")
    @Schema(description = "데일리카드 ID", example = "1")
    private Long cardId;

    @NotEmpty(message = "답변 목록은 비어있을 수 없습니다")
    @Valid
    @Schema(description = "질문별 답변 목록")
    private List<AnswerItem> answers;

    @Getter
    @NoArgsConstructor
    @Schema(description = "개별 질문에 대한 답변")
    public static class AnswerItem {

        @NotNull(message = "질문 번호는 필수입니다")
        @Schema(description = "질문 번호", example = "1")
        private Integer questionNo;

        @UserAnswer(max = 500)
        @Schema(description = "답변 내용 (객관식: 옵션 번호, 주관식: 텍스트)", example = "2")
        private String answerContent;
    }
}
