package com.todaktodot.TDTD.domain.login.service;

import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.request.TokenReissueRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.response.LoginResponseDTO;
import com.todaktodot.TDTD.domain.login.dto.response.TokenReissueResponseDTO;

public interface LoginService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    TokenReissueResponseDTO reissue(TokenReissueRequestDTO tokenReissueRequestDTO);
}
