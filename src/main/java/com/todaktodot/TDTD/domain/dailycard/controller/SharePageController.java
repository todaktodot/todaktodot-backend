package com.todaktodot.TDTD.domain.dailycard.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.core.io.Resource;

@Controller
public class SharePageController {
    @GetMapping("/share/card")
    public String shareCard() {
        return "forward:/share.html";
    }

    @GetMapping(
            value = "/.well-known/apple-app-site-association",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Resource> appleAppSiteAssociation() {
        Resource resource = new ClassPathResource("static/.well-known/apple-app-site-association");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resource);
    }
}
