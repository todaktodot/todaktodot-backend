package com.todaktodot.TDTD.global.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainTextValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("닉네임은 일반 텍스트를 허용한다")
    void nicknameAllowsPlainText() {
        var violations = validator.validate(new NicknameFixture("미쿠_01"));

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("닉네임은 HTML 태그처럼 보이는 입력을 거절한다")
    void nicknameRejectsHtmlTagLikeText() {
        var violations = validator.validate(new NicknameFixture("<b>미쿠</b>"));

        assertThat(violations)
                .extracting(v -> v.getMessage())
                .contains("닉네임에는 HTML 태그를 입력할 수 없습니다");
    }

    @Test
    @DisplayName("사용자 답변은 줄바꿈이 포함된 일반 텍스트를 허용한다")
    void userAnswerAllowsPlainMultilineText() {
        var violations = validator.validate(new UserAnswerFixture("오늘은 좋았어\n내일도 이야기하자"));

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("사용자 답변은 HTML 태그처럼 보이는 입력을 거절한다")
    void userAnswerRejectsHtmlTagLikeText() {
        var violations = validator.validate(new UserAnswerFixture("<b>안녕</b>"));

        assertThat(violations)
                .extracting(v -> v.getMessage())
                .contains("답변에는 HTML 태그를 입력할 수 없습니다");
    }

    @Test
    @DisplayName("Y/N 값은 Y와 N만 허용한다")
    void ynAllowsOnlyYOrN() {
        assertThat(validator.validate(new YnFixture("Y"))).isEmpty();
        assertThat(validator.validate(new YnFixture("N"))).isEmpty();

        assertThat(validator.validate(new YnFixture(null))).isNotEmpty();
        assertThat(validator.validate(new YnFixture(""))).isNotEmpty();
        assertThat(validator.validate(new YnFixture("y"))).isNotEmpty();
        assertThat(validator.validate(new YnFixture("true"))).isNotEmpty();
    }

    private static class NicknameFixture {
        @Nickname
        private final String value;

        private NicknameFixture(String value) {
            this.value = value;
        }
    }

    private static class UserAnswerFixture {
        @UserAnswer(max = 20)
        private final String value;

        private UserAnswerFixture(String value) {
            this.value = value;
        }
    }

    private static class YnFixture {
        @Yn
        private final String value;

        private YnFixture(String value) {
            this.value = value;
        }
    }
}
