package com.todaktodot.TDTD.domain.guide.service;

import com.todaktodot.TDTD.domain.guide.dto.response.GuideResponseDTO;
import com.todaktodot.TDTD.domain.guide.repository.GuideRepository;
import com.todaktodot.TDTD.domain.guide.repository.entity.Guide;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {
    private final GuideRepository guideRepository;

    /**
     *  가장 최신 버전의 가이드 툴팁 조회
     */
    @Override
    public GuideResponseDTO getGuide() {
        Guide guide = guideRepository.findTopByDelYnOrderByGuideIdDesc("N")
                .orElseThrow(() -> new IllegalStateException("활성중인 가이드 툴팁이 존재하지 않습니다."));

        return GuideResponseDTO.of(guide.getContent());
    }
}
