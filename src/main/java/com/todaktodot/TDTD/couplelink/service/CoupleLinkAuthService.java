package com.todaktodot.TDTD.couplelink.service;

import com.todaktodot.TDTD.couplelink.dto.request.ConnectLinkCodeRequestDTO;
import com.todaktodot.TDTD.couplelink.dto.request.IssueLinkCodeRequestDTO;
import com.todaktodot.TDTD.couplelink.dto.response.ConnectLinkCodeResponseDTO;
import com.todaktodot.TDTD.couplelink.dto.response.IssueLinkCodeResponseDTO;

public interface CoupleLinkAuthService {
    IssueLinkCodeResponseDTO issueLinkCode(IssueLinkCodeRequestDTO requestDTO);
    ConnectLinkCodeResponseDTO connectLinkCode(ConnectLinkCodeRequestDTO requestDTO);
}