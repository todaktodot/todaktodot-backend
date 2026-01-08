package com.todaktodot.TDTD.domain.login.service;

import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.response.LoginResponseDTO;

public interface LoginService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
}
