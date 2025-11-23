package com.example.webfluxdemo.controller;

import com.example.webfluxdemo.model.User;
import com.example.webfluxdemo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private Map<String, Object> expectedResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setBio("Test bio");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        expectedResponse = new HashMap<>();
        expectedResponse.put("success", true);
        expectedResponse.put("message", "用户创建成功");
        expectedResponse.put("data", testUser);
    }

    @Test
    void createUser_Success() {
        // Given
        User userRequest = new User();
        userRequest.setUsername("newuser");
        userRequest.setEmail("newuser@example.com");
        userRequest.setPassword("Password123!");

        given(userService.createUser(any(User.class))).willReturn(Mono.just(testUser));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.createUser(userRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue(response.getBody().containsKey("success"));
                    assertTrue((Boolean) response.getBody().get("success"));
                    assertEquals("用户创建成功", response.getBody().get("message"));
                    assertEquals(testUser, response.getBody().get("data"));
                })
                ;
    }

    @Test
    void createUser_WhenUsernameExists() {
        // Given
        User userRequest = new User();
        userRequest.setUsername("existinguser");
        userRequest.setEmail("existing@example.com");
        userRequest.setPassword("Password123!");

        given(userService.createUser(any(User.class)))
                .willReturn(Mono.error(new RuntimeException("用户名或邮箱已存在")));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.createUser(userRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(400, response.getStatusCode().value());
                    assertFalse((Boolean) response.getBody().get("success"));
                    assertEquals("用户名或邮箱已存在", response.getBody().get("message"));
                })
                ;
    }

    @Test
    void getUserById_Success() {
        // Given
        given(userService.getUserById(1L)).willReturn(Mono.just(testUser));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.getUserById(1L);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue((Boolean) response.getBody().get("success"));
                    assertEquals("获取用户成功", response.getBody().get("message"));
                    assertEquals(testUser, response.getBody().get("data"));
                })
                ;
    }

    @Test
    void getUserById_NotFound() {
        // Given
        given(userService.getUserById(1L)).willReturn(Mono.empty());

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.getUserById(1L);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertFalse((Boolean) response.getBody().get("success"));
                    assertEquals("用户不存在", response.getBody().get("message"));
                })
                ;
    }

    @Test
    void getAllUsers_Success() {
        // Given
        given(userService.getAllUsers()).willReturn(Flux.just(testUser));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.getAllUsers();

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue((Boolean) response.getBody().get("success"));
                    assertEquals("获取用户列表成功", response.getBody().get("message"));
                    assertNotNull(response.getBody().get("data"));
                })
                ;
    }

    @Test
    void getUsersByPage_Success() {
        // Given
        given(userService.getUsersWithPagination(0, 10)).willReturn(Flux.just(testUser));
        given(userService.countAllUsers()).willReturn(Mono.just(1L));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.getUsersByPage(0, 10);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue((Boolean) response.getBody().get("success"));
                    assertEquals("获取用户列表成功", response.getBody().get("message"));
                    assertNotNull(response.getBody().get("data"));
                    assertNotNull(response.getBody().get("pagination"));
                    assertEquals(0, ((Map<?, ?>) response.getBody().get("pagination")).get("page"));
                    assertEquals(10, ((Map<?, ?>) response.getBody().get("pagination")).get("size"));
                    assertEquals(1L, ((Map<?, ?>) response.getBody().get("pagination")).get("total"));
                })
                ;
    }

    @Test
    void searchUsersByUsername_Success() {
        // Given
        given(userService.searchUsersByUsername("test")).willReturn(Flux.just(testUser));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.searchUsersByUsername("test");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue((Boolean) response.getBody().get("success"));
                    assertEquals("搜索用户成功", response.getBody().get("message"));
                    assertEquals(testUser, response.getBody().get("data"));
                })
                ;
    }

    @Test
    void searchUsersByEmail_Success() {
        // Given
        given(userService.searchUsersByEmail("test@example.com")).willReturn(Flux.just(testUser));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.searchUsersByEmail("test@example.com");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue((Boolean) response.getBody().get("success"));
                    assertEquals("搜索用户成功", response.getBody().get("message"));
                    assertEquals(testUser, response.getBody().get("data"));
                })
                ;
    }

    @Test
    void updateUser_Success() {
        // Given
        User updateRequest = new User();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setBio("Updated bio");

        given(userService.updateUser(1L, updateRequest)).willReturn(Mono.just(testUser));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.updateUser(1L, updateRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue((Boolean) response.getBody().get("success"));
                    assertEquals("用户更新成功", response.getBody().get("message"));
                    assertEquals(testUser, response.getBody().get("data"));
                })
                ;
    }

    @Test
    void updateUser_WhenUsernameExists() {
        // Given
        User updateRequest = new User();
        updateRequest.setUsername("existinguser");
        updateRequest.setEmail("updated@example.com");

        given(userService.updateUser(1L, updateRequest))
                .willReturn(Mono.error(new RuntimeException("用户名已存在")));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.updateUser(1L, updateRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(400, response.getStatusCode().value());
                    assertFalse((Boolean) response.getBody().get("success"));
                    assertEquals("用户名已存在", response.getBody().get("message"));
                })
                ;
    }

    @Test
    void deleteUser_Success() {
        // Given
        given(userService.deleteUser(1L)).willReturn(Mono.empty());

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.deleteUser(1L);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue((Boolean) response.getBody().get("success"));
                    assertEquals("用户删除成功", response.getBody().get("message"));
                })
                ;
    }

    @Test
    void deleteUser_WithError() {
        // Given
        given(userService.deleteUser(1L))
                .willReturn(Mono.error(new RuntimeException("删除失败")));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.deleteUser(1L);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(400, response.getStatusCode().value());
                    assertFalse((Boolean) response.getBody().get("success"));
                    assertEquals("删除失败", response.getBody().get("message"));
                })
                ;
    }

    @Test
    void countUsers_Success() {
        // Given
        given(userService.countAllUsers()).willReturn(Mono.just(5L));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = userController.countUsers();

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue((Boolean) response.getBody().get("success"));
                    assertEquals("统计用户总数成功", response.getBody().get("message"));
                    assertEquals(5L, ((Map<?, ?>) response.getBody().get("data")).get("count"));
                })
                ;
    }
}