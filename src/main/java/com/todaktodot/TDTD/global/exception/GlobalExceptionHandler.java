package com.todaktodot.TDTD.global.exception;

import com.todaktodot.TDTD.domain.vote.exception.VoteException;
import com.todaktodot.TDTD.global.alert.DiscordNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordNotificationService discordNotificationService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("유효하지 않은 요청입니다");

        Map<String, String> response = new HashMap<>();
        response.put("message", errorMessage);

        discordNotificationService.sendErrorNotificationForAPI(e, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());

        discordNotificationService.sendErrorNotificationForAPI(e, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());

        discordNotificationService.sendErrorNotificationForAPI(e, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "요청한 리소스를 찾을 수 없습니다.");

        if (!"/favicon.ico".equals(request.getRequestURI())) {
            discordNotificationService.sendErrorNotificationForAPI(e, request);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CustomJwtException.class)
    public ResponseEntity<Map<String, String>> handleJwtException(CustomJwtException e, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());

        discordNotificationService.sendErrorNotificationForAPI(e, request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception e, HttpServletRequest request) {

        Map<String, String> response = new HashMap<>();
        response.put("message", "서버 내부 오류가 발생했습니다.");

        discordNotificationService.sendErrorNotificationForAPI(e, request);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(VoteException.class)
    public ResponseEntity<ErrorResponse> handleVoteException(VoteException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode));
    }
}