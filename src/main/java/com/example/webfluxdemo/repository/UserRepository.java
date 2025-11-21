package com.example.webfluxdemo.repository;

import com.example.webfluxdemo.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Mono<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Mono<User> findByEmail(String email);

    /**
     * 检查用户名是否已存在
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     */
    Mono<Boolean> existsByEmail(String email);

    /**
     * 根据用户名模糊查询用户列表
     */
    @Query("SELECT * FROM users WHERE username LIKE :keyword ORDER BY created_at DESC")
    Flux<User> findByUsernameContainingIgnoreCase(String keyword);

    /**
     * 根据邮箱模糊查询用户列表
     */
    @Query("SELECT * FROM users WHERE email LIKE :keyword ORDER BY created_at DESC")
    Flux<User> findByEmailContainingIgnoreCase(String keyword);

    /**
     * 分页查询用户列表
     */
    @Query("SELECT * FROM users ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<User> findAllWithPagination(int limit, int offset);

    /**
     * 统计用户总数
     */
    @Query("SELECT COUNT(*) FROM users")
    Mono<Long> countAll();
}