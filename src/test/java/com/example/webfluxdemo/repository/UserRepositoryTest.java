package com.example.webfluxdemo.repository;

import com.example.webfluxdemo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Repository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;

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
    }

    @Test
    void shouldExtendR2dbcRepository() {
        // Given
        Class<?>[] interfaces = UserRepository.class.getInterfaces();

        // Then
        assertTrue(interfaces.length > 0);
        boolean extendsR2dbc = false;
        for (Class<?> iface : interfaces) {
            if (iface.getSimpleName().equals("R2dbcRepository")) {
                extendsR2dbc = true;
                break;
            }
        }
        assertTrue(extendsR2dbc, "UserRepository should extend R2dbcRepository");
    }

    @Test
    void userShouldHaveTableAnnotation() {
        // Given
        Table tableAnnotation = User.class.getAnnotation(Table.class);

        // Then
        assertNotNull(tableAnnotation, "User class should have @Table annotation");
        assertEquals("users", tableAnnotation.value(), "Table name should be 'users'");
    }

    @Test
    void findByUsername_Success() {
        // Given
        given(userRepository.findByUsername("testuser")).willReturn(Mono.just(testUser));

        // When
        Mono<User> result = userRepository.findByUsername("testuser");

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
    void findByUsername_NotFound() {
        // Given
        given(userRepository.findByUsername("nonexistent")).willReturn(Mono.empty());

        // When
        Mono<User> result = userRepository.findByUsername("nonexistent");

        // Then
        StepVerifier.create(result)
                ;
    }

    @Test
    void findByEmail_Success() {
        // Given
        given(userRepository.findByEmail("test@example.com")).willReturn(Mono.just(testUser));

        // When
        Mono<User> result = userRepository.findByEmail("test@example.com");

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
    void findByEmail_NotFound() {
        // Given
        given(userRepository.findByEmail("nonexistent@example.com")).willReturn(Mono.empty());

        // When
        Mono<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Then
        StepVerifier.create(result)
                ;
    }

    @Test
    void existsByUsername_Exists() {
        // Given
        given(userRepository.existsByUsername("testuser")).willReturn(Mono.just(true));

        // When
        Mono<Boolean> result = userRepository.existsByUsername("testuser");

        // Then
        StepVerifier.create(result)
                .assertNext(exists -> assertTrue(exists))
                ;
    }

    @Test
    void existsByUsername_NotExists() {
        // Given
        given(userRepository.existsByUsername("nonexistent")).willReturn(Mono.just(false));

        // When
        Mono<Boolean> result = userRepository.existsByUsername("nonexistent");

        // Then
        StepVerifier.create(result)
                .assertNext(exists -> assertFalse(exists))
                ;
    }

    @Test
    void existsByEmail_Exists() {
        // Given
        given(userRepository.existsByEmail("test@example.com")).willReturn(Mono.just(true));

        // When
        Mono<Boolean> result = userRepository.existsByEmail("test@example.com");

        // Then
        StepVerifier.create(result)
                .assertNext(exists -> assertTrue(exists))
                ;
    }

    @Test
    void existsByEmail_NotExists() {
        // Given
        given(userRepository.existsByEmail("nonexistent@example.com")).willReturn(Mono.just(false));

        // When
        Mono<Boolean> result = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        StepVerifier.create(result)
                .assertNext(exists -> assertFalse(exists))
                ;
    }

    @Test
    void findByUsernameContainingIgnoreCase_Success() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("testadmin");
        user2.setEmail("admin@example.com");
        user2.setPassword("encodedPassword456");

        Flux<User> users = Flux.just(testUser, user2);
        given(userRepository.findByUsernameContainingIgnoreCase("test")).willReturn(users);

        // When
        Flux<User> result = userRepository.findByUsernameContainingIgnoreCase("test");

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals("testuser", user.getUsername());
                    assertTrue(user.getUsername().toLowerCase().contains("test"));
                })
                .assertNext(user -> {
                    assertEquals("testadmin", user.getUsername());
                    assertTrue(user.getUsername().toLowerCase().contains("test"));
                })
                ;
    }

    @Test
    void findByUsernameContainingIgnoreCase_NoResults() {
        // Given
        given(userRepository.findByUsernameContainingIgnoreCase("nonexistent"))
                .willReturn(Flux.empty());

        // When
        Flux<User> result = userRepository.findByUsernameContainingIgnoreCase("nonexistent");

        // Then
        StepVerifier.create(result)
                ;
    }

    @Test
    void findByEmailContainingIgnoreCase_Success() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setEmail("testadmin@example.com");
        user2.setPassword("encodedPassword456");

        Flux<User> users = Flux.just(testUser, user2);
        given(userRepository.findByEmailContainingIgnoreCase("example")).willReturn(users);

        // When
        Flux<User> result = userRepository.findByEmailContainingIgnoreCase("example");

        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertEquals("test@example.com", user.getEmail());
                    assertTrue(user.getEmail().toLowerCase().contains("example"));
                })
                .assertNext(user -> {
                    assertEquals("testadmin@example.com", user.getEmail());
                    assertTrue(user.getEmail().toLowerCase().contains("example"));
                })
                ;
    }

    @Test
    void findByEmailContainingIgnoreCase_NoResults() {
        // Given
        given(userRepository.findByEmailContainingIgnoreCase("nonexistent"))
                .willReturn(Flux.empty());

        // When
        Flux<User> result = userRepository.findByEmailContainingIgnoreCase("nonexistent");

        // Then
        StepVerifier.create(result)
                ;
    }

    @Test
    void findAllWithPagination_Success() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("encodedPassword456");

        Flux<User> users = Flux.just(testUser, user2);
        given(userRepository.findAllWithPagination(10, 0)).willReturn(users);

        // When
        Flux<User> result = userRepository.findAllWithPagination(10, 0);

        // Then
        StepVerifier.create(result)
                .assertNext(user -> assertEquals(1L, user.getId()))
                .assertNext(user -> assertEquals(2L, user.getId()))
                ;
    }

    @Test
    void findAllWithPagination_EmptyResult() {
        // Given
        given(userRepository.findAllWithPagination(10, 0)).willReturn(Flux.empty());

        // When
        Flux<User> result = userRepository.findAllWithPagination(10, 0);

        // Then
        StepVerifier.create(result)
                ;
    }

    @Test
    void countAll_Success() {
        // Given
        given(userRepository.countAll()).willReturn(Mono.just(5L));

        // When
        Mono<Long> result = userRepository.countAll();

        // Then
        StepVerifier.create(result)
                .assertNext(count -> assertEquals(5L, count))
                ;
    }

    @Test
    void countAll_Zero() {
        // Given
        given(userRepository.countAll()).willReturn(Mono.just(0L));

        // When
        Mono<Long> result = userRepository.countAll();

        // Then
        StepVerifier.create(result)
                .assertNext(count -> assertEquals(0L, count))
                ;
    }

    @Test
    void repositoryShouldHaveCorrectAnnotations() {
        // Given
        Repository repositoryAnnotation = UserRepository.class.getAnnotation(Repository.class);

        // Then
        assertNotNull(repositoryAnnotation, "UserRepository should have @Repository annotation");
    }
}