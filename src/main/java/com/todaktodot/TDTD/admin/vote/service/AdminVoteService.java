package com.todaktodot.TDTD.admin.vote.service;

import com.todaktodot.TDTD.admin.vote.dto.AdminVoteDetailDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteListDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteSearchCondition;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminVoteService {

    Page<AdminVoteListDTO> getList(AdminVoteSearchCondition condition, Pageable pageable);

    AdminVoteStatsDTO getStats();

    AdminVoteDetailDTO getDetail(Long voteId);

    void hide(Long voteId, String actor);

    void restore(Long voteId, String actor);

    void delete(Long voteId, String actor);
}
