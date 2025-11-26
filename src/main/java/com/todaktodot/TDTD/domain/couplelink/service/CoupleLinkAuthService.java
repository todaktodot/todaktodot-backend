package com.todaktodot.TDTD.domain.couplelink.service;

import com.todaktodot.TDTD.domain.couplelink.dto.request.IssueLinkCodeRequestDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.IssueLinkCodeResponseDTO;

public interface CoupleLinkAuthService {
    IssueLinkCodeResponseDTO issueLinkCode(IssueLinkCodeRequestDTO requestDTO);
}