package com.example.webfluxdemo.service;

import com.example.webfluxdemo.model.User;
import com.example.webfluxdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * 创建用户
     */
    public Mono<User> createUser(User user) {
        log.info("创建用户: {}", user.getUsername());

        return userRepository.existsByUsername(user.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("用户名已存在"));
                    }
                    return userRepository.existsByEmail(user.getEmail())
                            .flatMap(existsEmail -> {
                                if (existsEmail) {
                                    return Mono.error(new RuntimeException("邮箱已存在"));
                                }
                                User newUser = new User();
                                newUser.setUsername(user.getUsername());
                                newUser.setEmail(user.getEmail());
                                newUser.setPassword(user.getPassword());
                                newUser.setCreatedAt(LocalDateTime.now());
                                newUser.setUpdatedAt(LocalDateTime.now());

                                return userRepository.save(newUser);
                            });
                });
    }

    /**
     * 根据ID获取用户
     */
    public Mono<User> getUserById(Long id) {
        log.info("根据ID获取用户: {}", id);
        return userRepository.findById(id);
    }

    /**
     * 获取所有用户
     */
    public Flux<User> getAllUsers() {
        log.info("获取所有用户");
        return userRepository.findAll();
    }

    /**
     * 分页获取用户
     */
    public Flux<User> getUsersWithPagination(int page, int size) {
        log.info("分页获取用户 - 页码: {}, 每页大小: {}", page, size);
        int offset = page * size;
        return userRepository.findAllWithPagination(size, offset);
    }

    /**
     * 根据用户名搜索用户
     */
    public Flux<User> searchUsersByUsername(String keyword) {
        log.info("根据用户名搜索用户: {}", keyword);
        return userRepository.findByUsernameContainingIgnoreCase(keyword);
    }

    /**
     * 根据邮箱搜索用户
     */
    public Flux<User> searchUsersByEmail(String keyword) {
        log.info("根据邮箱搜索用户: {}", keyword);
        return userRepository.findByEmailContainingIgnoreCase(keyword);
    }

    /**
     * 更新用户
     */
    public Mono<User> updateUser(Long id, User user) {
        log.info("更新用户: {}", id);

        return userRepository.findById(id)
                .flatMap(existingUser -> {
                    // 检查用户名是否被其他用户使用
                    if (!existingUser.getUsername().equals(user.getUsername())) {
                        return userRepository.existsByUsername(user.getUsername())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new RuntimeException("用户名已存在"));
                                    }
                                    return updateUserFields(existingUser, user);
                                });
                    } else {
                        return updateUserFields(existingUser, user);
                    }
                })
                .switchIfEmpty(Mono.error(new RuntimeException("用户不存在")));
    }

    private Mono<User> updateUserFields(User existingUser, User user) {
        // 检查邮箱是否被其他用户使用
        if (!existingUser.getEmail().equals(user.getEmail())) {
            return userRepository.existsByEmail(user.getEmail())
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.error(new RuntimeException("邮箱已存在"));
                        }
                        existingUser.setUsername(user.getUsername());
                        existingUser.setEmail(user.getEmail());
                        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                            existingUser.setPassword(user.getPassword());
                        }
                        existingUser.setUpdatedAt(LocalDateTime.now());
                        return userRepository.save(existingUser);
                    });
        } else {
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }
            existingUser.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(existingUser);
        }
    }

    /**
     * 删除用户
     */
    public Mono<Void> deleteUser(Long id) {
        log.info("删除用户: {}", id);
        return userRepository.deleteById(id);
    }

    /**
     * 统计用户总数
     */
    public Mono<Long> countAllUsers() {
        log.info("统计用户总数");
        return userRepository.countAll();
    }
}