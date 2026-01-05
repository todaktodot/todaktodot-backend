package com.todaktodot.TDTD.admin.dailycard.dto;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyCardSearchDTO {

    private CardMode mode;
    private CardSubject subject;
    private CardType type;
    private String useYn;
    private String keyword;
    private int page = 0;
    private int size = 10;

    public boolean hasMode() {
        return mode != null;
    }

    public boolean hasSubject() {
        return subject != null;
    }

    public boolean hasType() {
        return type != null;
    }

    public boolean hasUseYn() {
        return useYn != null && !useYn.isEmpty();
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
