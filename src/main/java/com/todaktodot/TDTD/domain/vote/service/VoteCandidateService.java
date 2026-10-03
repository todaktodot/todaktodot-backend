package com.todaktodot.TDTD.domain.vote.service;

import com.todaktodot.TDTD.domain.vote.dto.request.VoteCandidateApproveRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCandidateDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCandidateApproveResultDTO;

import java.util.List;

public interface VoteCandidateService {

    /**
     * AI 후보 생성
     * 최근 투표 제목을 재료로 중복되지 않는 후보를 만들어 PENDING 으로 저장한다.
     * @return 저장된 후보 목록
     */
    List<VoteCandidateDTO> generate();

    /**
     * 대기 중인 후보 목록 조회
     */
    List<VoteCandidateDTO> getPendingList();

    /**
     * 후보 등록 - 선택한 후보를 실제 투표로 게시
     * @param requests 후보 ID 와 어드민이 수정한 문구
     * @param actor 처리한 어드민 계정명
     */
    VoteCandidateApproveResultDTO approve(List<VoteCandidateApproveRequestDTO> requests, String actor);

    /**
     * 후보 반려
     * @param candidateIds 반려할 후보 ID 목록
     * @param actor 처리한 어드민 계정명
     */
    int reject(List<Long> candidateIds, String actor);
}
