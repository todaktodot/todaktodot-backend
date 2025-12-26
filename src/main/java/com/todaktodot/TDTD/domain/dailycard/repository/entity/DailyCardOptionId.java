package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DailyCardOptionId implements Serializable {
    private Long cardId;
    private Integer questionNo;
    private Integer optionNo;
}
