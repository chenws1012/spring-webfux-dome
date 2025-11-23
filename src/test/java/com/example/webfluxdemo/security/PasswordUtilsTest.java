package com.example.webfluxdemo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordUtilsTest {

    @InjectMocks
    private PasswordUtils passwordUtils;

    private String validPassword;
    private String weakPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        validPassword = "ValidPassword123!";
        weakPassword = "weak";
        encodedPassword = passwordUtils.encodePassword(validPassword);
    }

    @Test
    void shouldHaveComponentAnnotation() {
        // Given
        Component componentAnnotation = PasswordUtils.class.getAnnotation(Component.class);

        // Then
        assertNotNull(componentAnnotation, "PasswordUtils should have @Component annotation");
    }

    @Test
    void shouldInitializePasswordEncoder() {
        // Given & When
        PasswordUtils utils = new PasswordUtils();

        // Then
        assertNotNull(utils);
        // The passwordEncoder should be initialized in the constructor
    }

    @Test
    void encodePassword_ShouldReturnEncodedPassword() {
        // Given
        String rawPassword = "TestPassword123!";

        // When
        String result = passwordUtils.encodePassword(rawPassword);

        // Then
        assertNotNull(result);
        assertNotEquals(rawPassword, result, "Encoded password should be different from raw password");
        assertTrue(result.startsWith("$2a$"), "BCrypt password should start with $2a$");
    }

    @Test
    void encodePassword_ShouldHandleNullInput() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            passwordUtils.encodePassword(null);
        }, "encodePassword should throw NullPointerException for null input");
    }

    @Test
    void encodePassword_ShouldHandleEmptyString() {
        // When
        String result = passwordUtils.encodePassword("");

        // Then
        assertNotNull(result);
        assertNotEquals("", result, "Encoded empty string should not be empty");
    }

    @Test
    void matches_WithCorrectPassword_ShouldReturnTrue() {
        // Given
        String rawPassword = "ValidPassword123!";

        // When
        boolean result = passwordUtils.matches(rawPassword, passwordUtils.encodePassword(rawPassword));

        // Then
        assertTrue(result, "Password match should return true for correct password");
    }

    @Test
    void matches_WithIncorrectPassword_ShouldReturnFalse() {
        // Given
        String incorrectPassword = "WrongPassword123!";

        // When
        boolean result = passwordUtils.matches(incorrectPassword, encodedPassword);

        // Then
        assertFalse(result, "Password match should return false for incorrect password");
    }

    @Test
    void matches_WithNullRawPassword_ShouldThrowException() {
        // Given
        String encoded = passwordUtils.encodePassword("test");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            passwordUtils.matches(null, encoded);
        }, "matches should throw NullPointerException for null raw password");
    }

    @Test
    void matches_WithNullEncodedPassword_ShouldThrowException() {
        // Given
        String raw = "test";

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            passwordUtils.matches(raw, null);
        }, "matches should throw NullPointerException for null encoded password");
    }

    @Test
    void matches_WithEmptyRawPassword_ShouldReturnFalse() {
        // Given
        String encoded = passwordUtils.encodePassword("test");

        // When
        boolean result = passwordUtils.matches("", encoded);

        // Then
        assertFalse(result, "Empty password should not match any encoded password");
    }

    @Test
    void matches_WithEmptyEncodedPassword_ShouldReturnFalse() {
        // Given
        String raw = "test";

        // When
        boolean result = passwordUtils.matches(raw, "");

        // Then
        assertFalse(result, "Raw password should not match empty encoded password");
    }

    @Test
    void isPasswordStrong_WithValidPassword_ShouldReturnTrue() {
        // Given
        String strongPassword = "StrongPassword123!";

        // When
        boolean result = passwordUtils.isPasswordStrong(strongPassword);

        // Then
        assertTrue(result, "Strong password should pass validation");
    }

    @Test
    void isPasswordStrong_WithShortPassword_ShouldReturnFalse() {
        // Given
        String shortPassword = "Short1!";

        // When
        boolean result = passwordUtils.isPasswordStrong(shortPassword);

        // Then
        assertFalse(result, "Short password should fail validation");
    }

    @Test
    void isPasswordStrong_WithoutUpperCase_ShouldReturnFalse() {
        // Given
        String noUpperCase = "lowercase123!";

        // When
        boolean result = passwordUtils.isPasswordStrong(noUpperCase);

        // Then
        assertFalse(result, "Password without uppercase should fail validation");
    }

    @Test
    void isPasswordStrong_WithoutLowerCase_ShouldReturnFalse() {
        // Given
        String noLowerCase = "UPPERCASE123!";

        // When
        boolean result = passwordUtils.isPasswordStrong(noLowerCase);

        // Then
        assertFalse(result, "Password without lowercase should fail validation");
    }

    @Test
    void isPasswordStrong_WithoutDigit_ShouldReturnFalse() {
        // Given
        String noDigit = "NoDigits!";

        // When
        boolean result = passwordUtils.isPasswordStrong(noDigit);

        // Then
        assertFalse(result, "Password without digits should fail validation");
    }

    @Test
    void isPasswordStrong_WithoutSpecialChar_ShouldReturnFalse() {
        // Given
        String noSpecialChar = "NoSpecialChar123";

        // When
        boolean result = passwordUtils.isPasswordStrong(noSpecialChar);

        // Then
        assertFalse(result, "Password without special characters should fail validation");
    }

    @Test
    void isPasswordStrong_WithNull_ShouldReturnFalse() {
        // When
        boolean result = passwordUtils.isPasswordStrong(null);

        // Then
        assertFalse(result, "Null password should fail validation");
    }

    @Test
    void isPasswordStrong_WithEmptyString_ShouldReturnFalse() {
        // When
        boolean result = passwordUtils.isPasswordStrong("");

        // Then
        assertFalse(result, "Empty password should fail validation");
    }

    @Test
    void isPasswordStrong_WithOnlySpecialChars_ShouldReturnFalse() {
        // Given
        String onlySpecialChars = "!@#$%";

        // When
        boolean result = passwordUtils.isPasswordStrong(onlySpecialChars);

        // Then
        assertFalse(result, "Password with only special chars should fail validation");
    }

    @Test
    void isPasswordStrong_WithValidComplexPassword_ShouldReturnTrue() {
        // Given
        String complexPassword = "MyComplexP@ssw0rd123";

        // When
        boolean result = passwordUtils.isPasswordStrong(complexPassword);

        // Then
        assertTrue(result, "Complex password should pass validation");
    }

    @Test
    void encodeAndMatch_ShouldBeConsistent() {
        // Given
        String originalPassword = "TestPassword123!";

        // When
        String encoded = passwordUtils.encodePassword(originalPassword);
        boolean matches = passwordUtils.matches(originalPassword, encoded);

        // Then
        assertTrue(matches, "Encoded password should match original password");
    }

    @Test
    void encodeDifferentPasswords_ShouldProduceDifferentHashes() {
        // Given
        String password1 = "Password1!";
        String password2 = "Password2!";

        // When
        String encoded1 = passwordUtils.encodePassword(password1);
        String encoded2 = passwordUtils.encodePassword(password2);

        // Then
        assertNotEquals(encoded1, encoded2, "Different passwords should produce different hashes");
    }

    @Test
    void samePasswordEncodedMultipleTimes_ShouldProduceDifferentHashes() {
        // Given
        String password = "SamePassword123!";

        // When
        String encoded1 = passwordUtils.encodePassword(password);
        String encoded2 = passwordUtils.encodePassword(password);

        // Then
        assertNotEquals(encoded1, encoded2, "Same password encoded multiple times should produce different hashes due to salt");
    }

    @Test
    void bothPasswordsShouldMatchAfterMultipleEncodings() {
        // Given
        String password = "TestPassword123!";

        // When
        String encoded1 = passwordUtils.encodePassword(password);
        String encoded2 = passwordUtils.encodePassword(password);

        // Then
        assertTrue(passwordUtils.matches(password, encoded1), "First encoding should match");
        assertTrue(passwordUtils.matches(password, encoded2), "Second encoding should match");
    }
}