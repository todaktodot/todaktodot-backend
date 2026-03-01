package com.todaktodot.TDTD.domain.notification.dto.reqeust;

import com.todaktodot.TDTD.domain.notification.dto.PushMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "알림 히스토리 등록 요청")
public class NotificationSaveRequest {

    private String fcmToken;
    private Long  receiveUser;
    private PushMessage pushMessage;
    private String successYn;
}
