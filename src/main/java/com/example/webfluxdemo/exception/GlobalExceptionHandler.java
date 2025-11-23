package com.example.webfluxdemo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import reactor.core.publisher.Mono;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常 (@Valid 失败)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        // 获取所有默认消息
        String errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "验证失败")
                .collect(Collectors.joining("; "));

        response.put("message", errorMessages);
        response.put("timestamp", System.currentTimeMillis());

        log.warn("参数验证失败: {}", errorMessages);
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * 处理约束验证异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        // 获取所有默认消息
        String errorMessages = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        response.put("message", errorMessages);
        response.put("timestamp", System.currentTimeMillis());

        log.warn("约束验证失败: {}", errorMessages);
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * 处理WebExchangeBindException异常
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleWebExchangeBindException(WebExchangeBindException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        // 获取所有默认消息
        String errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "验证失败")
                .collect(Collectors.joining("; "));

        response.put("message", errorMessages);
        response.put("timestamp", System.currentTimeMillis());

        log.warn("WebExchange绑定验证失败: {}", errorMessages);
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "参数类型不匹配: " + ex.getName());
        response.put("timestamp", System.currentTimeMillis());

        log.warn("参数类型不匹配: {}", ex.getName());
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());

        log.error("业务异常: {}", ex.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "系统错误，请稍后重试");
        response.put("timestamp", System.currentTimeMillis());

        log.error("系统异常: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.internalServerError().body(response));
    }
}