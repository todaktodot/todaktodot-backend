package com.todaktodot.TDTD.domain.term.service;

import com.todaktodot.TDTD.domain.term.dto.request.TermRequestDTO;

public interface TermService {
    void saveTerm(TermRequestDTO termRequestDTO);
}
