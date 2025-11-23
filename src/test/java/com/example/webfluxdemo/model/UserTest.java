package com.example.webfluxdemo.model;

import com.example.webfluxdemo.security.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private PasswordUtils passwordUtils;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("Password123!");
        user.setBio("Test bio");
        user.setIsActive(true);

        // Set timestamps
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void shouldHaveRequiredAnnotations() {
        // Given
        Data dataAnnotation = User.class.getAnnotation(Data.class);
        NoArgsConstructor noArgsConstructorAnnotation = User.class.getAnnotation(NoArgsConstructor.class);
        AllArgsConstructor allArgsConstructorAnnotation = User.class.getAnnotation(AllArgsConstructor.class);
        Table tableAnnotation = User.class.getAnnotation(Table.class);

        // Then
        assertNotNull(dataAnnotation, "User class should have @Data annotation");
        assertNotNull(noArgsConstructorAnnotation, "User class should have @NoArgsConstructor annotation");
        assertNotNull(allArgsConstructorAnnotation, "User class should have @AllArgsConstructor annotation");
        assertNotNull(tableAnnotation, "User class should have @Table annotation");
        assertEquals("users", tableAnnotation.value(), "Table name should be 'users'");
    }

    @Test
    void shouldHaveValidConstructor() {
        // Given
        User newUser = new User("testuser", "test@example.com", "Password123!");

        // Then
        assertEquals("testuser", newUser.getUsername());
        assertEquals("test@example.com", newUser.getEmail());
        assertEquals("Password123!", newUser.getPassword());
        assertTrue(newUser.getIsActive());
        assertNotNull(newUser.getCreatedAt());
        assertNotNull(newUser.getUpdatedAt());
    }

    @Test
    void shouldHaveValidGettersAndSetters() {
        // Test ID
        user.setId(2L);
        assertEquals(2L, user.getId());

        // Test Username
        user.setUsername("newusername");
        assertEquals("newusername", user.getUsername());

        // Test Email
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());

        // Test Password
        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword());

        // Test Bio
        user.setBio("Updated bio");
        assertEquals("Updated bio", user.getBio());

        // Test isActive
        user.setIsActive(false);
        assertFalse(user.getIsActive());
    }

    @Test
    void shouldHaveValidValidationAnnotations() {
        // Test username validation
        assertThrows(IllegalArgumentException.class, () -> {
            user.setUsername("");
        }, "Username should not be blank");

        // Test email validation
        assertThrows(IllegalArgumentException.class, () -> {
            user.setEmail("");
        }, "Email should not be blank");

        // Test password validation
        assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("");
        }, "Password should not be blank");
    }

    @Test
    void setPasswordEncoded_WithValidPassword() {
        // Given
        String rawPassword = "ValidPassword123!";
        String encodedPassword = "encodedHash123";

        given(passwordUtils.isPasswordStrong(rawPassword)).willReturn(true);
        given(passwordUtils.encodePassword(rawPassword)).willReturn(encodedPassword);

        // When
        user.setPasswordEncoded(rawPassword, passwordUtils);

        // Then
        assertEquals(encodedPassword, user.getPassword());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void setPasswordEncoded_WithInvalidPassword() {
        // Given
        String weakPassword = "weak";

        given(passwordUtils.isPasswordStrong(weakPassword)).willReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> user.setPasswordEncoded(weakPassword, passwordUtils)
        );
        assertEquals("密码不符合强度要求", exception.getMessage());

        // Password should not be updated
        assertNotEquals(weakPassword, user.getPassword());
    }

    @Test
    void setPasswordEncoded_WithNullPasswordUtils() {
        // Given
        String rawPassword = "ValidPassword123!";

        // When & Then
        assertThrows(
            NullPointerException.class,
            () -> user.setPasswordEncoded(rawPassword, null)
        );
    }

    @Test
    void verifyPassword_WithCorrectPassword() {
        // Given
        String rawPassword = "ValidPassword123!";
        String encodedPassword = "encodedHash123";

        given(passwordUtils.matches(rawPassword, encodedPassword)).willReturn(true);

        user.setPassword(encodedPassword);

        // When
        boolean result = user.verifyPassword(rawPassword, passwordUtils);

        // Then
        assertTrue(result);
        verify(passwordUtils).matches(rawPassword, encodedPassword);
    }

    @Test
    void verifyPassword_WithIncorrectPassword() {
        // Given
        String rawPassword = "WrongPassword123!";
        String encodedPassword = "encodedHash123";

        given(passwordUtils.matches(rawPassword, encodedPassword)).willReturn(false);

        user.setPassword(encodedPassword);

        // When
        boolean result = user.verifyPassword(rawPassword, passwordUtils);

        // Then
        assertFalse(result);
        verify(passwordUtils).matches(rawPassword, encodedPassword);
    }

    @Test
    void verifyPassword_WithNullPasswordUtils() {
        // Given
        String rawPassword = "ValidPassword123!";
        String encodedPassword = "encodedHash123";

        user.setPassword(encodedPassword);

        // When & Then
        assertThrows(
            NullPointerException.class,
            () -> user.verifyPassword(rawPassword, null)
        );
    }

    @Test
    void settersShouldUpdateTimestamps() throws InterruptedException {
        // Given
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();

        // When
        user.setPassword("newpassword");
        Thread.sleep(1); // Ensure timestamp difference
        LocalDateTime afterPasswordUpdate = user.getUpdatedAt();

        user.setBio("updated bio");
        Thread.sleep(1); // Ensure timestamp difference
        LocalDateTime afterBioUpdate = user.getUpdatedAt();

        // Then
        assertNotNull(originalUpdatedAt);
        assertNotNull(afterPasswordUpdate);
        assertNotNull(afterBioUpdate);
        assertTrue(afterPasswordUpdate.isAfter(originalUpdatedAt));
        assertTrue(afterBioUpdate.isAfter(afterPasswordUpdate));
    }

    @Test
    void shouldHaveValidFieldTypes() {
        // Test id field type
        assertEquals(Long.class, getFieldType("id"));

        // Test username field type
        assertEquals(String.class, getFieldType("username"));

        // Test email field type
        assertEquals(String.class, getFieldType("email"));

        // Test password field type
        assertEquals(String.class, getFieldType("password"));

        // Test isActive field type
        assertEquals(Boolean.class, getFieldType("isActive"));

        // Test bio field type
        assertEquals(String.class, getFieldType("bio"));

        // Test createdAt field type
        assertEquals(LocalDateTime.class, getFieldType("createdAt"));

        // Test updatedAt field type
        assertEquals(LocalDateTime.class, getFieldType("updatedAt"));
    }

    private Class<?> getFieldType(String fieldName) {
        try {
            Field field = User.class.getDeclaredField(fieldName);
            return field.getType();
        } catch (NoSuchFieldException e) {
            fail("Field " + fieldName + " not found");
            return null;
        }
    }

    @Test
    void shouldHaveDefaultValues() {
        // Test default constructor
        User defaultUser = new User();

        // Then
        assertNull(defaultUser.getId());
        assertNull(defaultUser.getUsername());
        assertNull(defaultUser.getEmail());
        assertNull(defaultUser.getPassword());
        assertNull(defaultUser.getBio());
        assertTrue(defaultUser.getIsActive()); // Default value should be true
        assertNull(defaultUser.getCreatedAt());
        assertNull(defaultUser.getUpdatedAt());
    }

    @Test
    void equalsAndHashCodeShouldWork() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("testuser");

        User user3 = new User();
        user3.setId(2L);
        user3.setUsername("differentuser");

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }
}