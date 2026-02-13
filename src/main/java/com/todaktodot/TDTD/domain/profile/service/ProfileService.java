package com.todaktodot.TDTD.domain.profile.service;

import com.todaktodot.TDTD.domain.profile.dto.request.SetNicknameRequestDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.SetNicknameResponseDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.UserDetailResponseDTO;

public interface ProfileService {

    SetNicknameResponseDTO setNickname(Long userId, SetNicknameRequestDTO requestDTO);

    UserDetailResponseDTO getDetail(long userId);
}
