package com.example.webfluxdemo.controller;

import com.example.webfluxdemo.model.User;
import com.example.webfluxdemo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关的CRUD操作API")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "创建用户",
            description = "创建一个新的用户账户，自动加密密码并进行强度验证"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "用户创建成功",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "请求参数错误或用户名/邮箱已存在",
                    content = @Content
            )
    })
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

    @Operation(
            summary = "获取所有用户",
            description = "获取系统中所有用户的列表"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "获取用户列表成功",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
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

    @Operation(
            summary = "分页获取用户",
            description = "分页获取用户列表，包含分页信息"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "获取用户列表成功",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @GetMapping("/page")
    public Mono<ResponseEntity<Map<String, Object>>> getUsersByPage(
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小")
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

    @Operation(
            summary = "根据用户名搜索用户",
            description = "根据用户名关键词搜索用户，支持模糊匹配"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "搜索用户成功",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @GetMapping("/search/username")
    public Mono<ResponseEntity<Map<String, Object>>> searchUsersByUsername(
            @Parameter(description = "搜索关键词")
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

    @Operation(
            summary = "根据邮箱搜索用户",
            description = "根据邮箱关键词搜索用户，支持模糊匹配"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "搜索用户成功",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @GetMapping("/search/email")
    public Mono<ResponseEntity<Map<String, Object>>> searchUsersByEmail(
            @Parameter(description = "搜索关键词")
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

    @Operation(
            summary = "根据ID获取用户",
            description = "根据用户ID获取用户详细信息"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "获取用户成功",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "用户不存在",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> getUserById(
            @Parameter(description = "用户ID")
            @PathVariable Long id) {
        log.info("接收到取用户详情请求: {}", id);

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
                    return Mono.just(ResponseEntity.ok(response));
//                    return Mono.just(ResponseEntity.notFound().build());
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