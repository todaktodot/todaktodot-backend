package com.todaktodot.TDTD.admin.vote.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReleaseRequestDTO {
    private Long userId;
    private String type;
    private String reason;
}
