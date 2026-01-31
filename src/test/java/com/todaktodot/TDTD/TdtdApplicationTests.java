package com.todaktodot.TDTD;

import com.todaktodot.TDTD.global.jwt.JwtTokenProvider;
import com.todaktodot.TDTD.global.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TdtdApplicationTests {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void contextLoads() {
	}

	@Test
	void generateTestToken() {
		// 테스트용 사용자 정보
		Long userId = 1L;
		String email = "test@example.com";
		Role role = Role.USER;

		// 토큰 생성
		String accessToken = jwtTokenProvider.createAccessToken(userId, role);
		String refreshToken = jwtTokenProvider.createRefreshToken(userId);

		// 콘솔에 출력
		System.out.println("\n========================================");
		System.out.println("테스트용 JWT 토큰 생성");
		System.out.println("========================================");
		System.out.println("User ID: " + userId);
		System.out.println("Email: " + email);
		System.out.println("Role: " + role);
		System.out.println("----------------------------------------");
		System.out.println("Access Token:");
		System.out.println(accessToken);
		System.out.println("----------------------------------------");
		System.out.println("Refresh Token:");
		System.out.println(refreshToken);
		System.out.println("========================================\n");
	}

}
