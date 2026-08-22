package com.todaktodot.TDTD.domain.vote.service;

import com.todaktodot.TDTD.domain.vote.dto.request.VoteSelectRequestDTO;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteCreateRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteReportRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteUpdateRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCreateResponseDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteListResponseDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteResponseDTO;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteSortCondition;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteStatus;

import java.util.List;

public interface VoteService {
    /**
     * 투표 목록 조회
     * @param userId 사용자ID
     * @param categories 카테고리 목록
     * @param status 투표 상태
     * @param isMine 내가 생성한 투표 여부
     * @param sortBy 정렬 방식
     * @param cursor 커서
     * @param size 페이지 크기
     */
    VoteListResponseDTO getList(Long userId, List<VoteCategory> categories, VoteStatus status, String isMine, VoteSortCondition sortBy, String cursor, int size);

    /**
     * 투표 단건 조회
     * @param userId 사용자 ID
     * @param voteId 투표 ID
     */
    VoteResponseDTO getDetail(Long userId, Long voteId);

    /**
     * 투표 생성
     */
    VoteCreateResponseDTO create(Long userId, VoteCreateRequestDTO request);

    /**
     * 투표 수정
     */
    void update(Long userId, VoteUpdateRequestDTO request);

    /**
     * 투표 삭제
     */
    void delete(Long userId, Long voteId);

    /**
     * 투표 선택
     */
    VoteResponseDTO select(Long userId, VoteSelectRequestDTO requestDTO);

    /**
     * 투표 취소
     */
    VoteResponseDTO cancelSelect(Long userId, Long voteId);

    /**
     * 투표 좋아요
     */
    void like(Long userId, Long voteId);

    /**
     * 투표 좋아요 취소
     */
    void cancelLike(Long userId, Long voteId);

    /**
     * 투표 신고
     */
    void report(Long userId, VoteReportRequestDTO request);
}
