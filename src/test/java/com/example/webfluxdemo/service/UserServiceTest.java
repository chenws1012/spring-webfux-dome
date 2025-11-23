package com.example.webfluxdemo.service;

import com.example.webfluxdemo.model.User;
import com.example.webfluxdemo.repository.UserRepository;
import com.example.webfluxdemo.security.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordUtils passwordUtils;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User newUserRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
        testUser.setBio("Test bio");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        newUserRequest = new User();
        newUserRequest.setUsername("newuser");
        newUserRequest.setEmail("newuser@example.com");
        newUserRequest.setPassword("Password123!");
        newUserRequest.setBio("New user bio");
    }

    @Test
    void createUser_Success() {
        // Given
        given(userRepository.existsByUsername("newuser")).willReturn(Mono.just(false));
        given(userRepository.existsByEmail("newuser@example.com")).willReturn(Mono.just(false));
        given(passwordUtils.encodePassword("Password123!")).willReturn("encodedPassword123");
        given(passwordUtils.isPasswordStrong("Password123!")).willReturn(true);
        given(userRepository.save(any(User.class))).willReturn(Mono.just(testUser));

        // When
        Mono<User> result = userService.createUser(newUserRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals("testuser", user.getUsername());
                    assertEquals("test@example.com", user.getEmail());
                    assertEquals("encodedPassword123", user.getPassword());
                    assertNotNull(user.getCreatedAt());
                    assertNotNull(user.getUpdatedAt());
                }).verifyComplete()
                ;

        // Verify password was encoded
        verify(passwordUtils).encodePassword("Password123!");
    }

    @Test
    void createUser_WhenUsernameExists() {
        // Given
        given(userRepository.existsByUsername("existinguser")).willReturn(Mono.just(true));
        given(userRepository.existsByEmail("existinguser@example.com")).willReturn(Mono.just(false));

        newUserRequest.setUsername("existinguser");
        newUserRequest.setEmail("existinguser@example.com");

        // When
        Mono<User> result = userService.createUser(newUserRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    "用户名或邮箱已存在".equals(throwable.getMessage())
                ).verify()
                ;
    }

    @Test
    void createUser_WhenEmailExists() {
        // Given
        given(userRepository.existsByUsername("newuser")).willReturn(Mono.just(false));
        given(userRepository.existsByEmail("existing@example.com")).willReturn(Mono.just(true));

        newUserRequest.setEmail("existing@example.com");

        // When
        Mono<User> result = userService.createUser(newUserRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    "用户名或邮箱已存在".equals(throwable.getMessage())
                ).verify()
                ;
    }

    @Test
    void createUser_WhenPasswordIsWeak() {
        // Given
        given(userRepository.existsByUsername("newuser")).willReturn(Mono.just(false));
        given(userRepository.existsByEmail("newuser@example.com")).willReturn(Mono.just(false));
        given(passwordUtils.isPasswordStrong("weak")).willReturn(false);

        newUserRequest.setPassword("weak");

        // When
        Mono<User> result = userService.createUser(newUserRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof IllegalArgumentException &&
                    "密码不符合强度要求".equals(throwable.getMessage())
                ).verify()
                ;
    }

    @Test
    void createUser_WhenPasswordEncodingFails() {
        // Given
        given(userRepository.existsByUsername("newuser")).willReturn(Mono.just(false));
        given(userRepository.existsByEmail("newuser@example.com")).willReturn(Mono.just(false));
        given(passwordUtils.isPasswordStrong("Password123!")).willReturn(true);
        given(passwordUtils.encodePassword(anyString()))
                .willThrow(new RuntimeException("Encryption failed"));

        // When
        Mono<User> result = userService.createUser(newUserRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    "系统错误：密码加密失败".equals(throwable.getMessage())
                ).verify()
                ;
    }

    @Test
    void getUserById_Success() {
        // Given
        given(userRepository.findById(1L)).willReturn(Mono.just(testUser));

        // When
        Mono<User> result = userService.getUserById(1L);

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals(1L, user.getId());
                    assertEquals("testuser", user.getUsername());
                    assertEquals("test@example.com", user.getEmail());
                })
                ;
    }

    @Test
    void getUserById_NotFound() {
        // Given
        given(userRepository.findById(999L)).willReturn(Mono.empty());

        // When
        Mono<User> result = userService.getUserById(999L);

        // Then
        StepVerifier.create(result)
                ;
    }

    @Test
    void getAllUsers_Success() {
        // Given
        given(userRepository.findAll()).willReturn(Flux.just(testUser));

        // When
        Flux<User> result = userService.getAllUsers();

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals("testuser", user.getUsername());
                    assertEquals("test@example.com", user.getEmail());
                })
                ;
    }

    @Test
    void getUsersWithPagination_Success() {
        // Given
        given(userRepository.findAllWithPagination(10, 0)).willReturn(Flux.just(testUser));

        // When
        Flux<User> result = userService.getUsersWithPagination(0, 10);

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals("testuser", user.getUsername());
                    assertEquals("test@example.com", user.getEmail());
                })
                ;
    }

    @Test
    void searchUsersByUsername_Success() {
        // Given
        given(userRepository.findByUsernameContainingIgnoreCase("test"))
                .willReturn(Flux.just(testUser));

        // When
        Flux<User> result = userService.searchUsersByUsername("test");

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals("testuser", user.getUsername());
                    assertTrue(user.getUsername().toLowerCase().contains("test"));
                })
                ;
    }

    @Test
    void searchUsersByEmail_Success() {
        // Given
        given(userRepository.findByEmailContainingIgnoreCase("example"))
                .willReturn(Flux.just(testUser));

        // When
        Flux<User> result = userService.searchUsersByEmail("example");

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals("test@example.com", user.getEmail());
                    assertTrue(user.getEmail().toLowerCase().contains("example"));
                })
                ;
    }

    @Test
    void updateUser_Success() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("olduser");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword("oldPassword");
        existingUser.setBio("Old bio");

        User updateRequest = new User();
        updateRequest.setUsername("newuser");
        updateRequest.setEmail("newuser@example.com");
        updateRequest.setBio("Updated bio");

        given(userRepository.findById(1L)).willReturn(Mono.just(existingUser));

        // When
        Mono<User> result = userService.updateUser(1L, updateRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals("newuser", user.getUsername());
                    assertEquals("newuser@example.com", user.getEmail());
                    assertEquals("Updated bio", user.getBio());
                })
                ;
    }

    @Test
    void updateUser_WhenUsernameExists() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("olduser");
        existingUser.setEmail("old@example.com");

        User updateRequest = new User();
        updateRequest.setUsername("existinguser");

        given(userRepository.findById(1L)).willReturn(Mono.just(existingUser));
        given(userRepository.existsByUsername("existinguser")).willReturn(Mono.just(true));

        // When
        Mono<User> result = userService.updateUser(1L, updateRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    "用户名已存在".equals(throwable.getMessage())
                )
                ;
    }

    @Test
    void updateUser_WhenUserNotFound() {
        // Given
        given(userRepository.findById(999L)).willReturn(Mono.empty());

        // When
        Mono<User> result = userService.updateUser(999L, new User());

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    "用户不存在".equals(throwable.getMessage())
                )
                ;
    }

    @Test
    void updateUser_WhenPasswordIsUpdated() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("olduser");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword("oldPassword");

        User updateRequest = new User();
        updateRequest.setUsername("olduser");
        updateRequest.setEmail("old@example.com");
        updateRequest.setPassword("NewPassword123!");

        given(userRepository.findById(1L)).willReturn(Mono.just(existingUser));
        given(passwordUtils.encodePassword("NewPassword123!")).willReturn("encodedNewPassword");
        given(userRepository.save(any(User.class))).willReturn(Mono.just(testUser));

        // When
        Mono<User> result = userService.updateUser(1L, updateRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals("olduser", user.getUsername());
                    assertEquals("old@example.com", user.getEmail());
                    assertEquals("encodedNewPassword", user.getPassword());
                })
                ;
    }

    @Test
    void deleteUser_Success() {
        // Given
        given(userRepository.deleteById(1L)).willReturn(Mono.empty());

        // When
        Mono<Void> result = userService.deleteUser(1L);

        // Then
        StepVerifier.create(result)
                ;
    }

    @Test
    void countAllUsers_Success() {
        // Given
        given(userRepository.countAll()).willReturn(Mono.just(5L));

        // When
        Mono<Long> result = userService.countAllUsers();

        // Then
        StepVerifier.create(result)
                .assertNext(count -> assertEquals(5L, count))
                ;
    }
}