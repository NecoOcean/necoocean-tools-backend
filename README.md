# necoocean-tools-backend

NecoOcean 个人工具展示站的后端。只提供 JSON API，不渲染页面。

架构、接口和字段以文档仓库为准：[necoocean-vault-docs](https://github.com/NecoOcean/necoocean-vault-docs)。

《系统设计文档》开发顺序已完成前四步：日志骨架、统一契约、数据层（Flyway + JPA）、认证会话（Cookie 会话、CSRF、登录锁定）。下一步是公开读接口 C-01～C-06、C-08。

## 构建

需要 JDK 17 和 Maven 3.8 以上。本机 Maven 默认会走到 JDK 8，构建前先把 `JAVA_HOME` 指到 JDK 17。

```powershell
$env:JAVA_HOME = "C:\Users\NecoOcean\.jdks\ms-17.0.17"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
mvn -B verify
```

`mvn -B verify` 是合并前的门禁：编译、单元测试、Checkstyle、阿里 p3c（PMD）、JaCoCo 行覆盖率不低于 70%。没有 Docker 时，MySQL 8.4 的 Flyway 集成测试会跳过。

本地启动：

```powershell
mvn -B spring-boot:run
```

进程只监听 `127.0.0.1:8080`。存活探测：`GET /api/v1/public/health`。

库需要事先建好，脚本是 `src/main/resources/db/create_database.sql`。表和预置分类、站点配置由 Flyway 在启动时写入。连接用环境变量 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`，本机可以把它们放在已忽略的 `config/local.yml`。

管理员不进版本库。库里还没有账号时，用 `ADMIN_USERNAME`（默认 `admin`）和 `ADMIN_PASSWORD` 创建一次；已有账号不会被覆盖。这两个变量同样放在 `config/local.yml`。

## 目录与分层

包名是 `com.necoocean.tools`。业务规则只允许写在 `service`。

| 层 | 包 | 职责 |
|---|---|---|
| 接入层 | `web.publicapi`、`web.admin`、`web` | 参数绑定、调用 service、装配响应 |
| 业务层 | `service` | 审核、分类保护、限流判定等规则的唯一落点 |
| 领域层 | `domain.entity`、`domain.repository` | 实体和 Spring Data JPA。手册里的 DAO 层落在这里 |
| 横切层 | `common`、`logging`、`config`、`security` | 信封、异常、日志、序列化、CSRF、登录锁定 |
| 调度层 | `scheduler` | 以后的残留对象清理 |

不设手册中的 Manager 层。审核策略要能靠改一个默认值切换，规则再拆一层就会散掉。

领域对象放在 `domain.entity`，不加 `DO` 后缀，和架构说明保持一致。对外传输对象放在 `dto`，类名以 `Dto` 结尾。`ApiResponse`、`PageResult` 是契约对象，放在 `common`。

`logback-spring.xml` 在 `src/main/resources`。架构图把它画在 `logging` 包旁边，Spring Boot 只会从 classpath 根加载它。

## 命名

遵循《阿里巴巴 Java 开发手册》华山版：

- 类名 `UpperCamelCase`，方法、变量 `lowerCamelCase`，常量全大写下划线。
- 包名全小写。`public` 是关键字，所以公开接口包名是 `publicapi`。
- 异常类以 `Exception` 结尾。测试类以被测类名开头、以 `Test` 结尾。
- 日志记录器命名为 `logger`。这是手册示例和 p3c 的豁免，不改成全大写。
- POJO 的布尔字段不要以 `is` 开头。
- Service / Repository 的方法使用 `get`、`list`、`count`、`save`、`remove`、`update`。

接口 JSON 使用蛇形字段，和附录 C 一致，不使用 Java 的驼峰对外。

## 注释

类、公有方法和公有常量使用 Javadoc。类注释包含 `@author` 和 `@date`。公有方法写清参数、返回值和抛出的检查异常。注释写在语句上方，不写行尾注释，不保留被注释掉的代码。

## 异常

业务失败使用 `BizException`（运行时异常），错误码和文案来自 `ErrorCode`，调用方不能改写文案。不要用异常做正常流程控制，不要空 catch，不要 `printStackTrace`。

`GlobalExceptionHandler` 的分流：

- 业务异常：WARN，保留完整堆栈。
- 参数、校验、读体失败：WARN，只记字段名，不记字段值。
- 未匹配路径：WARN，返回 `40400`。这是框架兜底码，不在附录 C 的 30 个业务码里，用来避免返回 HTML。
- 未知异常：ERROR，日志保留完整堆栈，响应只有「服务繁忙，请稍后再试。」

事务方法以后抛出 `BizException` 时，Spring 会回滚。不要把业务失败做成受检异常。

## 日志

使用 SLF4J，占位符输出，不拼接字符串，不用 `System.out`。

- `RequestLogFilter` 生成 `traceId`，写入 MDC 和响应头 `X-Trace-Id`。外部传入的值只接受 8 到 64 位十六进制。
- 访问日志记录方法、路径、状态、耗时和脱敏后的 IP。不记录查询串和请求体。
- `LogMasker` 处理邮箱和 IP。邮箱保留首字符和域名，IPv4 保留前两段。
- `ClientIpResolver` 取 `X-Forwarded-For` 第一段。进程只监听回环地址，这个头只能由本机 Nginx 写入。
- 文件日志 UTF-8，按天加 50MB 滚动，保留 15 天，总量上限 1GB。错误日志单独一份。

## 单元测试

测试放在 `src/test/java`，与主代码同包结构。遵守 AIR：自动、独立、可重复。用例覆盖正常值、边界和错误输入。

断言使用 AssertJ。测试方法保持包可见，避免为每个用例再写一套 Javadoc。

JaCoCo 在 `verify` 阶段检查整包行覆盖率不低于 70%。`ToolsApplication.main` 排除在外。

## 依赖

父 POM 是 Spring Boot `3.5.16`。Java 字节码目标为 17。

| 依赖 | 用途 |
|---|---|
| `spring-boot-starter-web` | JSON API |
| `spring-boot-starter-validation` | 参数校验 |
| `spring-boot-starter-security` | Cookie 会话与 CSRF |
| `spring-boot-starter-data-jpa` | 实体与仓储 |
| `flyway-core`、`flyway-mysql` | 启动时迁移 |
| `mysql-connector-j` | MySQL 8 驱动 |
| `spring-boot-starter-test` | JUnit 5、MockMvc、AssertJ |

不引入 Thymeleaf、Freemarker、Lombok、COS SDK。模板引擎被 Enforcer 直接禁止。不用 Lombok，是因为 p3c 只看源码，看不到生成的 `toString` 和 getter。

## 规范如何进构建

IDE 安装「Alibaba Java Coding Guidelines」插件，提交前仍以 Maven 为准。

| 插件 | 作用 |
|---|---|
| `maven-enforcer-plugin` | JDK 17–21、Maven 3.8+、禁止模板引擎 |
| `maven-checkstyle-plugin` 3.6.0 + Checkstyle 10.21.4 | 行宽 120、命名、括号、导入、Javadoc、禁止 `System.out` 和 `printStackTrace` |
| `maven-pmd-plugin` 3.21.2 + `p3c-pmd` 2.1.1 | 华山版语义规则：注释、常量、异常、并发、集合、OOP |
| `jacoco-maven-plugin` 0.8.15 | 行覆盖率不低于 70% |

`p3c-pmd` 2.1.1 只兼容 PMD 6，所以 PMD 插件锁定在 3.21.2（PMD 6.55.0）。不要升到已切换 PMD 7 的 3.22 及以后。

Checkstyle 规则在 `config/checkstyle/checkstyle.xml`。`logger` 允许小写，与手册示例一致。
