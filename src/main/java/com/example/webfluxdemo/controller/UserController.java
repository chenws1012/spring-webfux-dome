package com.example.webfluxdemo.controller;

import com.example.webfluxdemo.model.User;
import com.example.webfluxdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createUser(@Valid @RequestBody User user) {
        log.info("接收到创建用户请求: {}", user.getUsername());

        return userService.createUser(user)
                .map(createdUser -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "用户创建成功");
                    response.put("data", createdUser);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("创建用户失败: {}", e.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(response));
                });
    }

    /**
     * 获取所有用户
     */
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getAllUsers() {
        log.info("接收到获取所有用户请求");

        return userService.getAllUsers()
                .collectList()
                .map(users -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "获取用户列表成功");
                    response.put("data", users);
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * 分页获取用户
     */
    @GetMapping("/page")
    public Mono<ResponseEntity<Map<String, Object>>> getUsersByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("接收到分页获取用户请求 - 页码: {}, 每页大小: {}", page, size);

        return userService.getUsersWithPagination(page, size)
                .collectList()
                .flatMap(users -> userService.countAllUsers()
                        .map(total -> {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("message", "获取用户列表成功");
                            response.put("data", users);
                            response.put("pagination", Map.of(
                                    "page", page,
                                    "size", size,
                                    "total", total,
                                    "totalPages", (int) Math.ceil((double) total / size)
                            ));
                            return ResponseEntity.ok(response);
                        }));
    }

    /**
     * 根据用户名搜索用户
     */
    @GetMapping("/search/username")
    public Mono<ResponseEntity<Map<String, Object>>> searchUsersByUsername(
            @RequestParam String keyword) {
        log.info("接收到用户名搜索请求: {}", keyword);

        return userService.searchUsersByUsername(keyword)
                .collectList()
                .map(users -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "搜索用户成功");
                    response.put("data", users);
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * 根据邮箱搜索用户
     */
    @GetMapping("/search/email")
    public Mono<ResponseEntity<Map<String, Object>>> searchUsersByEmail(
            @RequestParam String keyword) {
        log.info("接收到邮箱搜索请求: {}", keyword);

        return userService.searchUsersByEmail(keyword)
                .collectList()
                .map(users -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "搜索用户成功");
                    response.put("data", users);
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> getUserById(@PathVariable Long id) {
        log.info("接��取用户详情请求: {}", id);

        return userService.getUserById(id)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "获取用户成功");
                    response.put("data", user);
                    return ResponseEntity.ok(response);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "用户不存在");
                    return Mono.just(ResponseEntity.notFound().build());
                }));
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user) {
        log.info("接收到更新用户请求: {}", id);

        return userService.updateUser(id, user)
                .map(updatedUser -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "用户更新成功");
                    response.put("data", updatedUser);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("更新用户失败: {}", e.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(response));
                });
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteUser(@PathVariable Long id) {
        log.info("接收到删除用户请求: {}", id);

        return userService.deleteUser(id)
                .then(Mono.fromCallable(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "用户删除成功");
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(e -> {
                    log.error("删除用户失败: {}", e.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(response));
                });
    }

    /**
     * 统计用户总数
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Map<String, Object>>> countUsers() {
        log.info("接收到统计用户总数请求");

        return userService.countAllUsers()
                .map(count -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "统计用户总数成功");
                    response.put("data", Map.of("count", count));
                    return ResponseEntity.ok(response);
                });
    }
}