package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteDisplayStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import com.todaktodot.TDTD.domain.vote.repository.projection.VoteCursorProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("투표 초안 조회 제외 테스트")
class VoteRepositoryDraftTest {

    private static final Long AUTHOR = 10L;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private TestEntityManager entityManager;

    private VoteEntity persistPosted(String title) {
        VoteEntity vote = VoteEntity.builder()
                .userId(AUTHOR)
                .randomNickname("용감한 수달0001")
                .category(VoteCategory.LOVE)
                .title(title)
                .closedAt(LocalDateTime.now().plusHours(24))
                .regrId(AUTHOR)
                .build();
        entityManager.persist(vote);
        return vote;
    }

    private VoteEntity persistDraft(String title) {
        VoteEntity vote = VoteEntity.draft(AUTHOR, "용감한 수달0002", VoteCategory.LOVE, title, 0L);
        entityManager.persist(vote);
        return vote;
    }

    @Test
    @DisplayName("초안은 마감 시각 없이 저장된다")
    void draft_SavedWithoutClosedAt() {
        // Given
        VoteEntity draft = persistDraft("초안 질문?");
        entityManager.flush();
        entityManager.clear();

        // When
        VoteEntity found = voteRepository.findById(draft.getVoteId()).orElseThrow();

        // Then - CLOSED_AT 이 NOT NULL 이면 저장 자체가 실패한다
        assertThat(found.getStatus()).isEqualTo(VoteDisplayStatus.DRAFT);
        assertThat(found.getClosedAt()).isNull();
    }

    @Test
    @DisplayName("하루 10개 제한 집계에서 초안은 빠진다")
    void dailyCount_ExcludesDraft() {
        // Given
        persistPosted("게시된 질문?");
        persistDraft("초안 질문?");
        entityManager.flush();

        LocalDate today = LocalDate.now();

        // When
        long count = voteRepository.countByUserIdAndRegDtBetweenAndDelYnAndStatusNot(
                AUTHOR, today.atStartOfDay(), today.plusDays(1).atStartOfDay(), "N", VoteDisplayStatus.DRAFT);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("마이페이지 목록에 초안은 나오지 않는다")
    void myPageList_ExcludesDraft() {
        // Given
        VoteEntity posted = persistPosted("게시된 질문?");
        persistDraft("초안 질문?");
        entityManager.flush();

        // When
        List<VoteCursorProjection> rows = voteRepository.findFirstForMyPageByLatest(AUTHOR, 10);

        // Then
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getVoteId()).isEqualTo(posted.getVoteId());
    }

    @Test
    @DisplayName("피드 목록에 초안은 나오지 않는다")
    void feedList_ExcludesDraft() {
        // Given
        VoteEntity posted = persistPosted("게시된 질문?");
        persistDraft("초안 질문?");
        entityManager.flush();

        // When - 다른 사용자가 피드를 본다
        List<VoteCursorProjection> rows = voteRepository.findFirstByLatest(
                99L, List.of(VoteCategory.LOVE.name()), null, null, 10);

        // Then
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getVoteId()).isEqualTo(posted.getVoteId());
    }

    @Test
    @DisplayName("AI 후보 생성용 최근 제목에 초안은 빠진다")
    void recentTitles_ExcludesDraft() {
        // Given
        persistPosted("게시된 질문?");
        persistDraft("초안 질문?");
        entityManager.flush();

        // When
        List<String> titles = voteRepository.findRecentTitles(30);

        // Then
        assertThat(titles).containsExactly("게시된 질문?");
    }
}
