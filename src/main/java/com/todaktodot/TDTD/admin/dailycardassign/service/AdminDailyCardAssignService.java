package com.todaktodot.TDTD.admin.dailycardassign.service;

import com.todaktodot.TDTD.admin.dailycardassign.dto.AssignmentHistoryDTO;
import com.todaktodot.TDTD.admin.dailycardassign.dto.AssignmentHistorySearchDTO;

import java.util.List;

public interface AdminDailyCardAssignService {

    List<AssignmentHistoryDTO> getAssignmentHistory(AssignmentHistorySearchDTO searchDTO, int limit);

    List<AssignmentHistoryDTO> getRecentAssignments(int limit);
}
