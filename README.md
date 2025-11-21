# Spring WebFlux 用户管理服务

这是一个基于Spring WebFlux的异步用户管理服务demo，实现了完整的用户CRUD操作和分页查询功能。

## 技术栈

- **Spring Boot 3.2.0**
- **Spring WebFlux** - 响应式Web框架
- **R2DBC** - 响应式数据库访问
- **PostgreSQL** - 数据库
- **Project Reactor** - 响应式编程
- **Maven** - 构建工具
- **Lombok** - 减少样板代码

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
  -e POSTGRES_PASSWORD=password \
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

#### 创建用户
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "password": "password123"
  }'
```

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
└── controller/
    └── UserController.java          # 控制器层

src/main/resources/
├── application.yml                  # 应用配置
└── schema.sql                      # 数据库初始化脚本
```

## 核心特性

1. **异步非阻塞**：基于Spring WebFlux和Project Reactor实现异步编程
2. **响应式数据库**：使用R2DBC进行响应式数据库访问
3. **完整CRUD**：支持用户的增删改查操作
4. **分页查询**：支持分页和条件查询
5. **数据验证**：使用JSR-303注解进行数据验证
6. **异常处理**：完善的异常处理和错误返回
7. **日志记录**：详细的日志记录用于调试

## 配置说明

### 数据库配置
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/webflux_demo
    username: postgres
    password: password
    pool:
      initial-size: 5
      max-size: 20
```

### 应用配置
```yaml
app:
  user:
    default-page-size: 10
    max-page-size: 100
```

## 开发说明

1. **响应式编程**：所有的方法都返回`Mono`或`Flux`类型
2. **事务管理**：使用`@Transactional`注解进行事务管理
3. **数据验证**：使用`@Valid`注解进行请求体验证
4. **日志记录**：使用`@Slf4j`注解进行日志记录

## 监控和管理

应用提供了Spring Boot Actuator端点：

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标

## 许可证

MIT License