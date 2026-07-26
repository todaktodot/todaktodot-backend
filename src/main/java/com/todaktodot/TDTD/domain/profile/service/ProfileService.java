package com.todaktodot.TDTD.domain.profile.service;

import com.todaktodot.TDTD.domain.profile.dto.request.SetNicknameRequestDTO;
import com.todaktodot.TDTD.domain.profile.dto.request.SetOnboardingRequestDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.SetNicknameResponseDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.SetOnboardingResponseDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.UserDetailResponseDTO;

public interface ProfileService {
    SetOnboardingResponseDTO setOnboarding(Long id, SetOnboardingRequestDTO requestDTO);

    SetNicknameResponseDTO setNickname(Long userId, SetNicknameRequestDTO requestDTO);

    UserDetailResponseDTO getDetail(long userId);

    void withdraw(long userId);

}
