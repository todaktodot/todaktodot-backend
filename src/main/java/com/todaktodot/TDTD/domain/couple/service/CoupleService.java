package com.todaktodot.TDTD.domain.couple.service;

import com.todaktodot.TDTD.domain.couple.dto.request.UpdateCoupleInfoRequestDTO;
import com.todaktodot.TDTD.domain.couple.dto.response.CoupleInfoResponseDTO;

public interface CoupleService {

    CoupleInfoResponseDTO updateCoupleInfo(Long userId, UpdateCoupleInfoRequestDTO requestDTO);

    CoupleInfoResponseDTO getCoupleInfo(Long userId);
}
