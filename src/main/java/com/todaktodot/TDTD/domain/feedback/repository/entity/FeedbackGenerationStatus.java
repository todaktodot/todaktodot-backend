package com.todaktodot.TDTD.domain.feedback.repository.entity;

public enum FeedbackGenerationStatus {
    NOT_STARTED,
    GENERATING,
    COMPLETED,
    FAILED;

    public static final String SWAGGER_DESCRIPTION = """
            AI 피드백 생성 상태
            - NOT_STARTED: 아직 피드백 생성 요청/결과가 없음. HTTP 200 응답이며 feedback은 null입니다.
            - GENERATING: 피드백 생성 중입니다. HTTP 200 응답이며 feedback은 null입니다. 앱은 잠시 후 조회 API를 다시 호출해 완료 여부를 확인할 수 있습니다.
            - COMPLETED: 피드백 생성 완료 상태입니다. HTTP 200 응답이며 feedback에 피드백 본문이 포함됩니다.
            - FAILED: 피드백 생성 실패 상태입니다. 조회 API에서는 HTTP 200 응답이며 feedback은 null입니다. 생성 API에서는 기존 매핑이 FAILED면 예외 응답으로 처리됩니다.
            """;

    public static final String GET_API_SWAGGER_DESCRIPTION = """
            커플 데일리카드의 AI 피드백 생성 상태와 결과를 조회합니다.

            앱 처리 기준:
            - HTTP 200 + feedbackStatus=NOT_STARTED: 아직 생성 요청/결과가 없습니다. feedback=null 입니다. 앱은 생성 버튼을 노출할 수 있습니다.
            - HTTP 200 + feedbackStatus=GENERATING: 생성 중입니다. feedback=null 입니다. 앱은 로딩/생성 중 상태를 보여주고, 일정 시간 뒤 이 조회 API를 다시 호출할 수 있습니다.
            - HTTP 200 + feedbackStatus=COMPLETED: 생성 완료입니다. feedback 객체에 summary, matchPoints, differences, conversationStarter가 포함됩니다.
            - HTTP 200 + feedbackStatus=FAILED: 생성 실패입니다. feedback=null 입니다. 앱은 실패 상태를 표시하거나 재시도 정책에 따라 생성 API 호출 여부를 결정합니다.

            인증되지 않은 요청은 401, 접근 권한이 없거나 카드가 존재하지 않는 경우는 예외 응답으로 처리됩니다.
            """;

    public static final String GENERATE_API_SWAGGER_DESCRIPTION = """
            커플 두 명의 필수 답변이 완료된 데일리카드에 대해 AI 피드백 생성을 요청합니다.

            앱 처리 기준:
            - HTTP 200 + feedbackStatus=GENERATING: 이미 다른 요청이 피드백을 생성 중입니다. feedback=null 입니다. 앱은 생성 중 상태를 보여주고 조회 API로 완료 여부를 확인할 수 있습니다.
            - HTTP 200 + feedbackStatus=COMPLETED: 피드백이 생성 완료되었거나, 이미 완료된 기존 피드백이 있습니다. feedback 객체에 피드백 본문이 포함됩니다.
            - feedbackStatus=NOT_STARTED는 생성 API의 정상 200 응답으로는 내려오지 않는 것이 원칙입니다.
            - 기존 생성 상태가 FAILED이면 200 DTO가 아니라 예외 응답으로 처리됩니다.

            답변 미완료, SOLO 커플, 접근 권한 없음, 카드 없음, AI 호출/파싱 실패 등은 예외 응답으로 처리됩니다. 인증되지 않은 요청은 401입니다.
            """;
}
