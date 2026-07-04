package com.todaktodot.TDTD.domain.dailycard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SharePageController {
    @GetMapping("/share/card")
    public String shareCard() {
        return "forward:/share.html";
    }
}
