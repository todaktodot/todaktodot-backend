package com.todaktodot.TDTD.admin.couple.controller;

import com.todaktodot.TDTD.admin.couple.dto.CoupleListDTO;
import com.todaktodot.TDTD.admin.couple.service.AdminCoupleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/couple")
public class AdminCoupleController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AdminCoupleService adminCoupleService;

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
}
