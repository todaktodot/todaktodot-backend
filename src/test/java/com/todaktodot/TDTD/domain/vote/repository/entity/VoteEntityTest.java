package com.todaktodot.TDTD.domain.vote.repository.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("투표 초안 상태 테스트")
class VoteEntityTest {

    private static final Long BOT_USER = 777L;
    private static final Long ADMIN_USER = 0L;

    private VoteEntity draft() {
        return VoteEntity.draft(BOT_USER, "용감한 수달0001", VoteCategory.LOVE, "기념일 챙기는 편이야?", ADMIN_USER);
    }

    @Test
    @DisplayName("초안 생성 - 마감 시각 없이 DRAFT 로 만들어진다")
    void draft_HasNoClosedAt() {
        VoteEntity vote = draft();

        assertThat(vote.getStatus()).isEqualTo(VoteDisplayStatus.DRAFT);
        assertThat(vote.isDraft()).isTrue();
        //게시 전부터 마감 시계가 돌면 안 되므로 비어 있어야 한다
        assertThat(vote.getClosedAt()).isNull();
        assertThat(vote.getUserId()).isEqualTo(BOT_USER);
        assertThat(vote.getParticipantCnt()).isZero();
    }

    @Test
    @DisplayName("초안 게시 - POSTED 로 바뀌고 마감 시각이 채워진다")
    void publish_SetsPostedAndClosedAt() {
        VoteEntity vote = draft();
        LocalDateTime closedAt = LocalDateTime.now().plusHours(24);

        vote.publish(vote.getTitle(), closedAt, ADMIN_USER);

        assertThat(vote.getStatus()).isEqualTo(VoteDisplayStatus.POSTED);
        assertThat(vote.isDraft()).isFalse();
        assertThat(vote.getClosedAt()).isEqualTo(closedAt);
    }

    @Test
    @DisplayName("이미 게시한 투표는 다시 게시할 수 없다")
    void publish_AlreadyPosted_Throws() {
        VoteEntity vote = draft();
        vote.publish(vote.getTitle(),LocalDateTime.now().plusHours(24), ADMIN_USER);

        //두 번 게시되면 마감 시각이 밀려 참여 가능 시간이 늘어난다
        assertThatThrownBy(() -> vote.publish(vote.getTitle(),LocalDateTime.now().plusHours(24), ADMIN_USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("초안 상태의 투표만");
    }

    @Test
    @DisplayName("초안은 숨김 처리할 수 없다")
    void hide_Draft_Throws() {
        VoteEntity vote = draft();

        assertThatThrownBy(() -> vote.hide(HideReason.ADMIN, ADMIN_USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("초안은 숨길 수 없습니다");
    }

    @Test
    @DisplayName("초안은 복구 대상이 아니다")
    void restore_Draft_Throws() {
        VoteEntity vote = draft();

        assertThatThrownBy(() -> vote.restore(ADMIN_USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("초안은 복구 대상이");
    }

    @Test
    @DisplayName("초안은 표출 상태 변경으로 게시할 수 없다")
    void updateDisplayStatus_Draft_Throws() {
        VoteEntity vote = draft();

        //이 경로로 POSTED 가 되면 마감 시각이 비어 있는 투표가 피드에 올라간다
        assertThatThrownBy(() -> vote.updateDisplayStatus(VoteDisplayStatus.POSTED, ADMIN_USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("초안 상태는");
    }

    @Test
    @DisplayName("게시된 투표를 DRAFT 로 되돌릴 수 없다")
    void updateDisplayStatus_ToDraft_Throws() {
        VoteEntity vote = draft();
        vote.publish(vote.getTitle(), LocalDateTime.now().plusHours(24), ADMIN_USER);

        assertThatThrownBy(() -> vote.updateDisplayStatus(VoteDisplayStatus.DRAFT, ADMIN_USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("초안 상태는");
    }

    @Test
    @DisplayName("게시된 투표는 기존대로 숨김·복구가 된다")
    void hideAndRestore_Posted_Works() {
        VoteEntity vote = draft();
        vote.publish(vote.getTitle(),LocalDateTime.now().plusHours(24), ADMIN_USER);

        vote.hide(HideReason.AUTO, ADMIN_USER);
        assertThat(vote.getStatus()).isEqualTo(VoteDisplayStatus.HIDDEN);
        assertThat(vote.getHideReason()).isEqualTo(HideReason.AUTO);

        vote.restore(ADMIN_USER);
        assertThat(vote.getStatus()).isEqualTo(VoteDisplayStatus.POSTED);
        assertThat(vote.getHideReason()).isNull();
    }
}
