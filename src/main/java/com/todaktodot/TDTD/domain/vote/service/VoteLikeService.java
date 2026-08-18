package com.todaktodot.TDTD.domain.vote.service;

public interface VoteLikeService {

    void like(Long userId, Long voteId);

    void cancelLike(Long userId, Long voteId);
}
