package com.todaktodot.TDTD.domain.sample.service;

import com.todaktodot.TDTD.domain.sample.dto.reqeust.SampleRequestDTO;
import com.todaktodot.TDTD.domain.sample.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SampleServiceImpl implements SampleService{

    private final SampleRepository sampleRepository;

    @Override
    public void getList(SampleRequestDTO sampleRequestDTO) {

    }
    // 테스트커밋용10
}
