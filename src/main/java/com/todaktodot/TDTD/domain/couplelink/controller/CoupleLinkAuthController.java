package com.todaktodot.TDTD.domain.couplelink.controller;

import com.todaktodot.TDTD.domain.couplelink.dto.request.IssueLinkCodeRequestDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.IssueLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.service.CoupleLinkAuthService;
import com.todaktodot.TDTD.domain.couplelink.dto.request.ConnectLinkCodeRequestDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.ConnectLinkCodeResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/couple-link")
public class CoupleLinkAuthController {

    private final CoupleLinkAuthService coupleLinkAuthService;

    @PostMapping("/issue")
    public ResponseEntity<IssueLinkCodeResponseDTO> issueLinkCode(
            @Valid @RequestBody IssueLinkCodeRequestDTO requestDTO) {

        IssueLinkCodeResponseDTO response = coupleLinkAuthService.issueLinkCode(requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/connect")
    public ResponseEntity<ConnectLinkCodeResponseDTO> connectLinkCode(
            @Valid @RequestBody ConnectLinkCodeRequestDTO requestDTO) {

        ConnectLinkCodeResponseDTO response = coupleLinkAuthService.connectLinkCode(requestDTO);
        return ResponseEntity.ok(response);
    }
}