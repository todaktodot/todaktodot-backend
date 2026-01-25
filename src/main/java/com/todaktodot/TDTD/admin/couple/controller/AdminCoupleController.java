package com.todaktodot.TDTD.admin.couple.controller;

import com.todaktodot.TDTD.admin.couple.dto.CoupleDetailDTO;
import com.todaktodot.TDTD.admin.couple.dto.CoupleListDTO;
import com.todaktodot.TDTD.admin.couple.service.AdminCoupleService;
import com.todaktodot.TDTD.domain.feedback.dto.reqeust.GenerateFeedbackRequestDTO;
import com.todaktodot.TDTD.domain.feedback.dto.response.GenerateFeedbackResponseDTO;
import com.todaktodot.TDTD.domain.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/couple")
@Slf4j
public class AdminCoupleController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AdminCoupleService adminCoupleService;
    private final FeedbackService feedbackService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String delYn,
                       Model model) {
        Page<CoupleListDTO> couples = adminCoupleService.getCouples(delYn, PageRequest.of(page, DEFAULT_PAGE_SIZE));

        model.addAttribute("couples", couples);
        model.addAttribute("delYn", delYn);
        model.addAttribute("totalCount", adminCoupleService.getTotalCount());
        model.addAttribute("activeCount", adminCoupleService.getActiveCount());
        model.addAttribute("inactiveCount", adminCoupleService.getInactiveCount());
        model.addAttribute("activeMenu", "couple");

        return "admin/couple/list";
    }

    @GetMapping("/{coupleId}")
    public String detail(@PathVariable Long coupleId, Model model) {
        model.addAttribute("couple", adminCoupleService.getCouple(coupleId));
        model.addAttribute("activeMenu", "couple");

        return "admin/couple/detail";
    }

    /**
     * Admin에서 AI 피드백 생성
     * api쪽 FeedbackService를 재사용, 커플의 userId1을 전달하여 검증 통과하도록 구현
     */
    @PostMapping("/{coupleId}/feedback/generate")
    @ResponseBody
    public ResponseEntity<GenerateFeedbackResponseDTO> generateFeedback(
            @PathVariable Long coupleId,
            @RequestBody GenerateFeedbackRequestDTO requestDTO) {

        log.info("[Admin] 피드백 생성 요청: coupleId={}, coupleCardId={}, cardId={}",
                coupleId, requestDTO.getCoupleCardId(), requestDTO.getCardId());

        // 커플 정보 조회하여 userId1 획득 (검증 통과용)
        CoupleDetailDTO couple = adminCoupleService.getCouple(coupleId);
        Long userId = couple.getUserId1();

        GenerateFeedbackResponseDTO response = feedbackService.generateFeedback(userId, requestDTO);

        log.info("[Admin] 피드백 생성 완료: coupleId={}, feedbackId={}",
                coupleId, response.getFeedbackId());

        return ResponseEntity.ok(response);
    }
}
