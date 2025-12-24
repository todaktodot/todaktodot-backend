package com.todaktodot.TDTD.domain.couplelink.service;

import com.todaktodot.TDTD.domain.couplelink.dto.request.ConnectLinkCodeRequestDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.ConnectLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.IssueLinkCodeResponseDTO;

public interface CoupleLinkAuthService {
    IssueLinkCodeResponseDTO issueLinkCode(Long userId);
    ConnectLinkCodeResponseDTO connectLinkCode(Long userId, ConnectLinkCodeRequestDTO requestDTO);
}