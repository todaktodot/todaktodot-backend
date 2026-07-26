package com.todaktodot.TDTD.admin.couple.controller;

import com.todaktodot.TDTD.admin.couple.dto.CoupleDetailDTO;
import com.todaktodot.TDTD.admin.couple.dto.CoupleListDTO;
import com.todaktodot.TDTD.admin.couple.service.AdminCoupleService;
import com.todaktodot.TDTD.domain.feedback.dto.reqeust.GenerateFeedbackRequestDTO;
import com.todaktodot.TDTD.domain.feedback.dto.response.FeedbackResponseDTO;
import com.todaktodot.TDTD.domain.feedback.service.FeedbackService;
import com.todaktodot.TDTD.domain.insight.dto.GenerateInsightRequestDTO;
import com.todaktodot.TDTD.domain.insight.dto.GenerateInsightResponseDTO;
import com.todaktodot.TDTD.domain.insight.service.InsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/couple")
@Slf4j
public class AdminCoupleController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AdminCoupleService adminCoupleService;
    private final FeedbackService feedbackService;
    private final InsightService insightService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String delYn,
                       Model model) {
        String resolvedDelYn = (delYn == null || delYn.isBlank()) ? "N" : delYn;
        Page<CoupleListDTO> couples = adminCoupleService.getCouples(
                resolvedDelYn,
                PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "regDt"))
        );

        model.addAttribute("couples", couples);
        model.addAttribute("delYn", resolvedDelYn);
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
    public ResponseEntity<FeedbackResponseDTO> generateFeedback(
            @PathVariable Long coupleId,
            @RequestBody GenerateFeedbackRequestDTO requestDTO) {

        log.info("[Admin] 피드백 생성 요청: coupleId={}, coupleCardId={}, cardId={}",
                coupleId, requestDTO.getCoupleCardId(), requestDTO.getCardId());

        // 커플 정보 조회하여 userId1 획득 (검증 통과용)
        CoupleDetailDTO couple = adminCoupleService.getCouple(coupleId);
        Long userId = couple.getUserId1();

        FeedbackResponseDTO response = feedbackService.generateFeedback(userId, requestDTO);

        Long feedbackId = response.getFeedback() != null ? response.getFeedback().getFeedbackId() : null;
        log.info("[Admin] 피드백 생성 완료: coupleId={}, feedbackStatus={}, feedbackId={}",
                coupleId, response.getFeedbackStatus(), feedbackId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{coupleId}/feedback")
    @ResponseBody
    public ResponseEntity<?> deleteFeedback(@PathVariable Long coupleId,
                                             @RequestParam Long coupleCardId) {
        log.info("[Admin] 피드백 삭제 요청: coupleId={}, coupleCardId={}", coupleId, coupleCardId);
        adminCoupleService.deleteFeedback(coupleCardId);
        log.info("[Admin] 피드백 삭제 완료: coupleId={}, coupleCardId={}", coupleId, coupleCardId);
        return ResponseEntity.ok(java.util.Map.of("message", "피드백이 삭제되었습니다."));
    }

    @PostMapping("/{coupleId}/insight/generate")
    @ResponseBody
    public ResponseEntity<GenerateInsightResponseDTO> generateInsight(
            @PathVariable Long coupleId,
            @RequestBody GenerateInsightRequestDTO requestDTO) {

        log.info("[Admin] 인사이트 생성 요청: coupleId={}, endDt={}", coupleId, requestDTO.getEndDt());

        //임시 - 어드민에서 생성하는 경우 일요일 -> 월요일로 조정
        requestDTO.setEndDt(requestDTO.getEndDt().plusDays(1));
        GenerateInsightResponseDTO response = insightService.generateInsight(requestDTO);

        log.info("[Admin] 인사이트 생성 완료: coupleId={}, InsightId={}",
                coupleId, response.getInsightId());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{coupleId}/{reportId}/insight")
    @ResponseBody
    public ResponseEntity<?> deleteInsight(@PathVariable Long coupleId,
                                           @PathVariable Long reportId,
                                           @RequestParam LocalDate startDt,
                                           @RequestParam LocalDate endDt) {
        log.info("[Admin] 인사이트 삭제 요청: coupleId={}, startDt={}, endDt={}", coupleId, startDt, endDt);
        adminCoupleService.deleteInsight(coupleId, reportId, startDt, endDt);
        log.info("[Admin] 인사이트 삭제 완료: coupleId={}, startDt={}, endDt={}", coupleId, startDt, endDt);
        return ResponseEntity.ok(java.util.Map.of("message", "인사이트가 삭제되었습니다."));
    }
}
