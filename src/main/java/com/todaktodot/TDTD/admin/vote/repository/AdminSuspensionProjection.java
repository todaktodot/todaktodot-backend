package com.todaktodot.TDTD.admin.vote.repository;

import java.time.LocalDateTime;

public interface AdminSuspensionProjection {
    String getReason();
    LocalDateTime getRegDt();
}
