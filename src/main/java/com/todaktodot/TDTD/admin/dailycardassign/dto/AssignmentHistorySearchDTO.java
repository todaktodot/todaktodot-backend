package com.todaktodot.TDTD.admin.dailycardassign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentHistorySearchDTO {

    private String coupleName;
    private String cardTitle;
    private String cardMode;
    private String cardSubject;
    private String cardType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issuedStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issuedEndDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate regStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate regEndDate;

    public boolean hasAnyFilter() {
        return isNotBlank(coupleName)
                || isNotBlank(cardTitle)
                || isNotBlank(cardMode)
                || isNotBlank(cardSubject)
                || isNotBlank(cardType)
                || issuedStartDate != null
                || issuedEndDate != null
                || regStartDate != null
                || regEndDate != null;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
