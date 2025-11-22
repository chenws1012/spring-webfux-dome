# Spring WebFlux 用户管理服务

这是一个基于Spring WebFlux的响应式用户管理服务，实现了完整的用户CRUD操作、分页查询功能、搜索功能和密码安全特性。

## 技术栈

- **Spring Boot 3.3.0** - 应用框架
- **Spring WebFlux** - 响应式Web框架
- **R2DBC** - 响应式数据库访问
- **PostgreSQL** - 主数据库
- **Project Reactor** - 响应式编程
- **Maven** - 构建工具
- **Lombok** - 减少样板代码
- **SpringDoc OpenAPI** - API文档
- **Spring Security Crypto** - 密码加密

## 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Docker (可选，用于快速启动PostgreSQL)

### 2. 数据库设置

#### 使用Docker启动PostgreSQL
```bash
docker run -d --name postgres-webflux \
  -e POSTGRES_DB=webflux_demo \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=123456 \
  -p 5432:5432 \
  postgres:13
```

#### 手动创建数据库
```bash
createdb webflux_demo
psql -d webflux_demo -f src/main/resources/schema.sql
```

### 3. 构建和运行

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run

# 或者直接打包运行
mvn clean package
java -jar target/webflux-demo-1.0.0.jar
```

### 4. 测试API

应用启动后，访问 http://localhost:8080/api/users

#### API 文档
应用启动后，可以通过以下地址查看完整的 API 文档：
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

#### 创建用户
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

**注意**: 密码必须符合强度要求：至少8个字符，包含大小写字母、数字和特殊字符

#### 获取所有用户
```bash
curl http://localhost:8080/api/users
```

#### 分页获取用户
```bash
curl "http://localhost:8080/api/users?page=0&size=5"
```

#### 根据ID获取用户
```bash
curl http://localhost:8080/api/users/1
```

#### 更新用户
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "updated_user",
    "email": "updated@example.com",
    "password": "newpassword123"
  }'
```

#### 删除用户
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

#### 搜索用户
```bash
# 按用户名搜索
curl "http://localhost:8080/api/users/search/username?keyword=john"

# 按邮箱搜索
curl "http://localhost:8080/api/users/search/email?keyword=example"
```

#### 统计用户总数
```bash
curl http://localhost:8080/api/users/count
```

## API 接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/users` | 创建用户 |
| GET | `/api/users` | 获取所有用户 |
| GET | `/api/users/page` | 分页获取用户 |
| GET | `/api/users/{id}` | 根据ID获取用户 |
| PUT | `/api/users/{id}` | 更新用户 |
| DELETE | `/api/users/{id}` | 删除用户 |
| GET | `/api/users/search/username` | 按用户名搜索 |
| GET | `/api/users/search/email` | 按邮箱搜索 |
| GET | `/api/users/count` | 统计用户总数 |

## 安全功能

### 密码加密与验证
- **BCrypt加密**: 使用Spring Security Crypto进行密码加密
- **密码强度验证**: 密码必须包含大小写字母、数字和特殊字符
- **自动加密**: 创建和更新用户时自动加密密码
- **密码验证**: 提供密码验证方法用于用户登录

### 数据安全
- **唯一性约束**: 用户名和邮箱必须唯一
- **输入验证**: 使用JSR-303注解进行输入验证
- **错误处理**: 完善的错误处理机制，不暴露敏感信息
- **事务管理**: 使用Spring事务确保数据一致性

## 项目结构

```
src/main/java/com/example/webfluxdemo/
├── WebfluxDemoApplication.java      # 主启动类
├── model/
│   └── User.java                   # 用户实体类
├── repository/
│   └── UserRepository.java          # 数据访问层
├── service/
│   └── UserService.java            # 业务逻辑层
├── controller/
│   └── UserController.java          # 控制器层
└── security/
    └── PasswordUtils.java           # 密码工具类

src/main/resources/
├── application.yml                  # 应用配置
└── schema.sql                      # 数据库初始化脚本
```

## 核心特性

1. **异步非阻塞**：基于Spring WebFlux和Project Reactor实现异步编程
2. **响应式数据库**：使用R2DBC进行响应式数据库访问
3. **完整CRUD**：支持用户的增删改查操作
4. **分页查询**：支持分页和条件查询
5. **搜索功能**：支持按用户名和邮箱搜索
6. **数据验证**：使用JSR-303注解进行数据验证
7. **密码安全**：BCrypt加密和密码强度验证
8. **异常处理**：完善的异常处理和错误返回
9. **API文档**：集成SpringDoc OpenAPI自动生成API文档
10. **监控支持**：集成Spring Boot Actuator

## 密码强度验证

系统对密码有严格的强度要求：

### 密码规则
- **最小长度**: 8个字符
- **最大长度**: 100个字符
- **必须包含**:
  - 至少1个大写字母 (A-Z)
  - 至少1个小写字母 (a-z)
  - 至少1个数字 (0-9)
  - 至少1个特殊字符 (@$!%*?&)

### 示例密码
- ✅ `Password123!`
- ✅ `MySecurePass@2024`
- ✅ `User#12345`
- ❌ `password123` (缺少大写字母和特殊字符)
- ❌ `PASSWORD123` (缺少小写字母)
- ❌ `Password` (缺少数字和特殊字符)

## 开发说明

1. **响应式编程**：所有的方法都返回`Mono`或`Flux`类型
2. **事务管理**：使用`@Transactional`注解进行事务管理
3. **数据验证**：使用`@Valid`注解进行请求体验证
4. **日志记录**：使用`@Slf4j`注解进行日志记录
5. **API文档**：集成OpenAPI 3.0注解自动生成文档

### 数据库配置更新
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/webflux_demo
    username: postgres
    password: 123456  # 更新为正确的密码
    pool:
      initial-size: 5
      max-size: 20
      min-idle: 5
      max-idle-time: 30m
```

### 管理端点配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

## 监控和管理

应用提供了Spring Boot Actuator端点：

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标

## 开发建议

### 响应式编程最佳实践
- 使用`flatMap`进行链式操作
- 正确处理错误流（`onErrorResume`, `onErrorReturn`）
- 避免阻塞操作在响应式流中
- 合理使用背压机制

### 性能优化
- 数据库连接池配置
- 分页查询避免大数据量处理
- 使用索引提高查询性能
- 合理设置缓存策略

## 测试

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserServiceTest

# 运行测试并生成报告
mvn clean test jacoco:report
```

### 测试数据
应用启动时会自动创建测试数据：
- `john_doe` (john@example.com)
- `jane_smith` (jane@example.com)
- `bob_wilson` (bob@example.com)

## 许可证

MIT License