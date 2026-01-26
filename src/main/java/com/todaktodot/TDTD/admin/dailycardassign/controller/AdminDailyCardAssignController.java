package com.todaktodot.TDTD.admin.dailycardassign.controller;

import com.todaktodot.TDTD.admin.dailycardassign.dto.AssignmentHistoryDTO;
import com.todaktodot.TDTD.admin.dailycardassign.dto.AssignmentHistorySearchDTO;
import com.todaktodot.TDTD.admin.dailycardassign.service.AdminDailyCardAssignService;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import com.todaktodot.TDTD.domain.dailycard.dto.request.AssignBatchRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignBatchResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.service.DailyCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/admin/daily-card-assign")
@RequiredArgsConstructor
public class AdminDailyCardAssignController {

    private final DailyCardService dailyCardService;
    private final AdminDailyCardAssignService adminDailyCardAssignService;

    @GetMapping
    public String assignPage(Model model) {
        model.addAttribute("activeMenu", "dailycard-assign");
        model.addAttribute("modes", CardMode.values());
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("types", CardType.values());
        return "admin/dailycardassign/assign";
    }

    @PostMapping("/batch")
    @ResponseBody
    public ResponseEntity<AssignBatchResponseDTO> assignBatch(
            @Valid @RequestBody AssignBatchRequestDTO requestDTO) {
        AssignBatchResponseDTO response = dailyCardService.assignDailyCardsForCouples(
                requestDTO.getStartDate(),
                requestDTO.getEndDate());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<List<AssignmentHistoryDTO>> getHistory(
            AssignmentHistorySearchDTO searchDTO,
            @RequestParam(required = false, defaultValue = "100") int limit) {

        List<AssignmentHistoryDTO> history = adminDailyCardAssignService
                .getAssignmentHistory(searchDTO, limit);
        return ResponseEntity.ok(history);
    }
}
