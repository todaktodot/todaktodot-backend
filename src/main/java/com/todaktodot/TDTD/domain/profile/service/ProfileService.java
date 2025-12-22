package com.todaktodot.TDTD.domain.profile.service;

import com.todaktodot.TDTD.domain.profile.dto.request.SetNicknameRequestDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.SetNicknameResponseDTO;

public interface ProfileService {

    SetNicknameResponseDTO setNickname(Long userId, SetNicknameRequestDTO requestDTO);
}
