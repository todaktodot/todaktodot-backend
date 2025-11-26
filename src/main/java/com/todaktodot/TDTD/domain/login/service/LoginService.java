package com.todaktodot.TDTD.domain.login.service;

import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.response.LoginTokenResponseDTO;

public interface LoginService {
    LoginTokenResponseDTO login(LoginRequestDTO loginRequestDTO);
}
