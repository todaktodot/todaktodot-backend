package com.todaktodot.TDTD.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // @Async 어노테이션 활성화
    // 기본 SimpleAsyncTaskExecutor 사용
    // 필요시 커스텀 ThreadPoolTaskExecutor 설정 가능
}
