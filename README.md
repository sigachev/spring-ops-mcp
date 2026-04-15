# Spring Ops MCP

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4+-green.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1+-purple.svg)](https://spring.io/projects/spring-ai)
[![Author](https://img.shields.io/badge/Author-Mikhail%20Sigachev-blue.svg)](https://github.com/sigachev)

**Connect AI coding assistants to your Spring Boot applications.**

Spring Ops MCP exposes Spring Boot Actuator endpoints as [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) tools, allowing Claude Code, Cursor, Windsurf, and other AI assistants to monitor, debug, and interact with your running applications through natural language.

```
"Check the health of my user-service"
"Why is my app slow? Check the metrics and thread dump"
"Enable debug logging for com.myapp.auth"
"What REST endpoints does order-service expose?"
"Compare memory usage across all my services"
```

---

## Why Spring Ops MCP?

**Because debugging Spring Boot apps shouldn't require 10 browser tabs and a PhD in Actuator endpoints.**

### Before: The Old Way 😩

```
1. Something's wrong → open browser → localhost:8081/actuator/health
2. Copy JSON, try to read it
3. Open /actuator/metrics → "what was that metric name again?"
4. Google the metric name
5. /actuator/metrics/http.server.requests?tag=uri:/api/users&tag=method:GET
6. Need debug logs → edit application.properties → restart app
7. Reproduce the issue → finally find the problem after 30 minutes
```

### After: The Spring Ops MCP Way 🚀

```
You: "My /api/users endpoint is slow, investigate"

Claude: Found the issue:
        - Response time: 2.3s (should be <200ms)
        - Root cause: Connection pool exhausted (10/10 connections)
        - Fix: spring.datasource.hikari.maximum-pool-size=20
```

**30 minutes → 30 seconds.**

### Real-World Impact

| Task | Before | After |
|------|--------|-------|
| Morning health check across 5 services | 10 min | 10 sec |
| Debug production performance issue | 30 min | 2 min |
| Onboard new team member to codebase | 2 days | 30 min |
| Change log level for debugging | 5 min + restart | 5 sec, no restart |
| Find why auto-config isn't working | 15 min | 30 sec |

📖 **[See all use cases with examples →](USE_CASES.md)**

---

## Table of Contents

- [Features](#features)
- [Quick Start](#quick-start)
  - [Option A: Embedded Mode](#option-a-embedded-mode-recommended)
  - [Option B: Standalone Mode](#option-b-standalone-mode)
- [Configuration](#configuration)
- [Available MCP Tools](#available-mcp-tools)
- [Usage Examples](#usage-examples)
- [Target App Requirements](#target-app-requirements)
- [Building from Source](#building-from-source)
- [Architecture](#architecture)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

| Category | Capabilities |
|----------|--------------|
| 🏥 **Health & Status** | Application health with all indicators (database, redis, disk, custom), build info, git commit |
| 📊 **Metrics** | JVM memory, CPU, HTTP request stats, connection pools, Micrometer metrics with tag filtering |
| ⚙️ **Configuration** | Environment properties, active profiles, Spring beans, REST endpoint mappings |
| 📝 **Logging** | View log levels, change log levels at runtime (no restart needed!) |
| 🔍 **Diagnostics** | Thread dumps, auto-configuration report, scheduled tasks, HTTP exchange traces |
| 🗄️ **Caches** | List caches, clear specific or all caches |

---

## Quick Start

### Option A: Embedded Mode (Recommended)

Add the MCP server directly to your Spring Boot application. The MCP endpoint runs alongside your app.

#### 1. Add the dependency

**Maven:**
```xml
<dependency>
    <groupId>io.github.sigachev</groupId>
    <artifactId>spring-ops-mcp-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'io.github.sigachev:spring-ops-mcp-starter:0.1.0'
```

#### 2. Configure your application

Add to `application.properties`:
```properties
# Your app's port
server.port=8081

# Expose Actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,env,loggers,beans,mappings,threaddump,scheduledtasks,conditions,caches
management.endpoint.health.show-details=always

# MCP server configuration
spring.ai.mcp.server.name=my-app
spring.ai.mcp.server.transport=sse
spring.ai.mcp.server.sse-path=/mcp/sse

# Auto-register this app for self-monitoring
spring.ops.mcp.register-self=true
spring.ops.mcp.self-name=my-app
spring.ops.mcp.self-port=${server.port}
```

Or `application.yml`:
```yaml
server:
  port: 8081

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,loggers,beans,mappings,threaddump,scheduledtasks,conditions,caches
  endpoint:
    health:
      show-details: always

spring:
  ai:
    mcp:
      server:
        name: my-app
        transport: sse
        sse-path: /mcp/sse
  ops:
    mcp:
      register-self: true
      self-name: my-app
      self-port: 8081
```

#### 3. Start your application

```bash
./mvnw spring-boot:run
```

Your app now has an MCP endpoint at `http://localhost:8081/mcp/sse`

#### 4. Connect Claude Code

```bash
claude mcp add my-app --transport http http://localhost:8081/mcp/sse
```

#### 5. Start chatting!

```
You: Check the health of my-app
Claude: ✓ my-app is UP
        Components:
        - db: UP (PostgreSQL 15.3)
        - redis: UP
        - diskSpace: UP (128 GB free)
```

---

### Option B: Standalone Mode

Run Spring Ops MCP as a separate service that connects to multiple Spring Boot applications.

#### 1. Run the standalone server

**Docker (recommended):**
```bash
docker run -p 8090:8090 sigachev/spring-ops-mcp
```

**Or download and run the JAR:**
```bash
curl -LO https://github.com/sigachev/spring-ops-mcp/releases/latest/download/spring-ops-mcp-server.jar
java -jar spring-ops-mcp-server.jar
```

**Or build from source:**
```bash
git clone https://github.com/sigachev/spring-ops-mcp.git
cd spring-ops-mcp
./mvnw clean package -pl spring-ops-mcp-server -am -DskipTests
java -jar spring-ops-mcp-server/target/spring-ops-mcp-server-0.1.0-SNAPSHOT.jar
```

#### 2. Connect Claude Code

```bash
claude mcp add spring-ops --transport http http://localhost:8090/mcp/sse
```

#### 3. Register your applications

```
You: Register user-service at http://localhost:8081
Claude: ✓ Registered 'user-service' - connection verified!

You: Register order-service at http://localhost:8082
Claude: ✓ Registered 'order-service' - connection verified!

You: List my apps
Claude: Registered applications:
        1. user-service  → http://localhost:8081/actuator
        2. order-service → http://localhost:8082/actuator
```

#### 4. Pre-configure apps (optional)

Instead of registering apps via chat, configure them in `application.yml`:

```yaml
spring:
  ops:
    mcp:
      apps:
        user-service:
          url: http://localhost:8081
        order-service:
          url: http://localhost:8082
        payment-service:
          url: http://localhost:8083
          actuator-path: /management  # custom actuator path
```

---

## Configuration

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `spring.ops.mcp.enabled` | `true` | Enable/disable Spring Ops MCP |
| `spring.ops.mcp.register-self` | `true` | Auto-register this app for self-monitoring |
| `spring.ops.mcp.self-name` | `self` | Name for self-registration |
| `spring.ops.mcp.self-port` | `8080` | Port for self-registration |
| `spring.ops.mcp.apps.<name>.url` | - | Base URL of target application |
| `spring.ops.mcp.apps.<name>.actuator-path` | `/actuator` | Actuator base path |

### Full Configuration Example

```yaml
server:
  port: 8090

spring:
  application:
    name: spring-ops-mcp-server

  ai:
    mcp:
      server:
        name: spring-ops-mcp
        version: 0.1.0
        transport: sse
        sse-path: /mcp/sse

  ops:
    mcp:
      enabled: true
      register-self: false  # Standalone mode
      apps:
        # Development services
        user-service:
          url: http://localhost:8081
        order-service:
          url: http://localhost:8082
        payment-service:
          url: http://localhost:8083
        
        # Production services (with auth - coming soon)
        # prod-api:
        #   url: https://api.mycompany.com
        #   actuator-path: /management
        #   auth-type: basic
        #   username: actuator
        #   password: ${ACTUATOR_PASSWORD}

logging:
  level:
    io.github.sigachev.springopsmcp: DEBUG
```

---

## Available MCP Tools

### App Management

| Tool | Description |
|------|-------------|
| `listApps` | List all registered Spring Boot applications |
| `registerApp(name, url, actuatorPath?)` | Register a new application for monitoring |
| `removeApp(name)` | Remove an application from monitoring |

### Health & Status

| Tool | Description |
|------|-------------|
| `getHealth(appName)` | Get health status with all indicators (db, redis, disk, custom) |
| `getInfo(appName)` | Get build info, git commit, and custom info |

### Metrics

| Tool | Description |
|------|-------------|
| `listMetrics(appName)` | List all available metric names |
| `getMetric(appName, metricName, tags?)` | Get specific metric with optional tag filtering |

**Common metrics:**
- `jvm.memory.used`, `jvm.memory.max` — JVM heap
- `jvm.gc.pause` — Garbage collection
- `http.server.requests` — HTTP request stats (filter by `uri`, `method`, `status`)
- `hikaricp.connections.active` — Database connection pool
- `system.cpu.usage`, `process.cpu.usage` — CPU
- `tomcat.threads.current`, `tomcat.threads.busy` — Thread pool

### Configuration

| Tool | Description |
|------|-------------|
| `getEnv(appName, pattern?)` | Get environment properties (optionally filtered) |
| `getBeans(appName)` | List all Spring beans |
| `getMappings(appName)` | Get all REST endpoint mappings |

### Logging

| Tool | Description |
|------|-------------|
| `getLoggers(appName, loggerName?)` | Get log levels for all or specific logger |
| `setLogLevel(appName, loggerName, level)` | Change log level at runtime |

**Valid log levels:** `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `OFF`

### Diagnostics

| Tool | Description |
|------|-------------|
| `getThreadDump(appName)` | Get thread dump for deadlock analysis |
| `getScheduledTasks(appName)` | List @Scheduled tasks |
| `getConditions(appName)` | Get auto-configuration conditions report |
| `getHttpExchanges(appName)` | Get recent HTTP request traces |

### Caches

| Tool | Description |
|------|-------------|
| `getCaches(appName)` | List all caches |
| `clearCache(appName, cacheName)` | Clear specific cache (or `"all"` for all caches) |

---

## Usage Examples

### 🌅 Morning Health Check

```
You: Good morning! How are my services doing?

Claude: Good morning! All 3 services are healthy:

┌─────────────────┬────────┬─────────────────────────┐
│ Service         │ Status │ Memory                  │
├─────────────────┼────────┼─────────────────────────┤
│ user-service    │ ✓ UP   │ 245 MB / 512 MB (48%)   │
│ order-service   │ ✓ UP   │ 312 MB / 512 MB (61%)   │
│ payment-service │ ✓ UP   │ 198 MB / 512 MB (39%)   │
└─────────────────┴────────┴─────────────────────────┘

No issues detected. All database and Redis connections healthy.
```

### 🐌 Performance Investigation

```
You: The checkout endpoint is slow, investigate

Claude: I'll analyze the checkout endpoint performance.

[Calling getMetric("order-service", "http.server.requests", "uri:/api/checkout")]
[Calling getMetric("order-service", "hikaricp.connections.active")]
[Calling getThreadDump("order-service")]

Found the issue:

📊 /api/checkout performance:
   - Average response time: 4.2s (should be <500ms)
   - 95th percentile: 6.8s
   - Requests/min: 847

🔍 Root cause: Database connection pool exhausted
   - HikariCP: 10/10 connections active (100% utilized)
   - 6 threads blocked waiting for connections
   - Slow query in OrderRepository.findByUserIdWithItems()

💡 Recommendations:
1. Increase pool size:
   spring.datasource.hikari.maximum-pool-size=20

2. Add database index:
   CREATE INDEX idx_orders_user_id ON orders(user_id);

3. Optimize the query — consider pagination or lazy loading for order items
```

### 🔍 Runtime Debugging

```
You: Enable debug logging for the auth package, I need to trace a login issue

Claude: Done! Set com.myapp.auth to DEBUG in user-service.

You: [reproduce the issue]

You: What do the logs show?

Claude: Based on the debug output, the issue is in TokenValidator:
        - JWT signature validation is passing
        - But the token's "aud" claim doesn't match your configured audience
        - Expected: "https://api.myapp.com"
        - Actual: "https://api.myapp.com/" (trailing slash)

You: OK, I see the issue. Set logging back to INFO.

Claude: Done! Set com.myapp.auth back to INFO.
```

### 🗺️ API Discovery

```
You: I'm new to the team. What endpoints does order-service have?

Claude: order-service exposes 12 REST endpoints:

📦 Orders
   POST   /api/orders              Create new order
   GET    /api/orders              List orders (paginated)
   GET    /api/orders/{id}         Get order by ID
   PUT    /api/orders/{id}         Update order
   DELETE /api/orders/{id}         Cancel order

💳 Payments
   POST   /api/orders/{id}/pay     Process payment
   GET    /api/orders/{id}/payment Payment status

👤 User Orders
   GET    /api/users/{userId}/orders    User's order history

📊 Analytics
   GET    /api/orders/stats        Order statistics
   GET    /api/orders/export       Export orders (CSV)

🔧 Internal
   POST   /internal/orders/sync    Sync with warehouse
   GET    /internal/health         Deep health check
```

### ⚖️ Environment Comparison

```
You: Compare the config between local and staging for user-service

Claude: Configuration differences between local and staging:

| Property                        | Local           | Staging              |
|---------------------------------|-----------------|----------------------|
| spring.datasource.url           | localhost:5432  | staging-db.aws:5432  |
| spring.redis.host               | localhost       | staging-redis.aws    |
| spring.kafka.bootstrap-servers  | localhost:9092  | staging-kafka.aws    |
| logging.level.root              | DEBUG           | INFO                 |
| server.tomcat.max-threads       | 200             | 400                  |
| app.feature.new-checkout        | true            | false                |

⚠️ Note: The new-checkout feature flag is enabled locally but disabled in staging.
```

### 🧹 Cache Management

```
You: The product catalog seems stale, clear the product caches

Claude: Found 3 product-related caches in catalog-service:
        - products (1,247 entries)
        - product-categories (89 entries)  
        - product-prices (1,247 entries)

Cleared all 3 caches. The next requests will fetch fresh data from the database.
```

---

## Target App Requirements

Your monitored Spring Boot applications need:

### 1. Actuator Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 2. Exposed Endpoints

```properties
# Minimum for basic monitoring
management.endpoints.web.exposure.include=health,info,metrics

# Full feature set
management.endpoints.web.exposure.include=health,info,metrics,env,loggers,beans,mappings,threaddump,scheduledtasks,conditions,httpexchanges,caches

# Show health details
management.endpoint.health.show-details=always
```

### 3. (Optional) HTTP Exchanges

To enable `getHttpExchanges`, add:

```java
@Bean
public HttpExchangeRepository httpExchangeRepository() {
    return new InMemoryHttpExchangeRepository();
}
```

---

## Building from Source

```bash
# Clone
git clone https://github.com/sigachev/spring-ops-mcp.git
cd spring-ops-mcp

# Build all modules
./mvnw clean install

# Build server JAR only
./mvnw clean package -pl spring-ops-mcp-server -am -DskipTests

# Run standalone server
java -jar spring-ops-mcp-server/target/spring-ops-mcp-server-0.1.0-SNAPSHOT.jar

# Docker build
docker build -t spring-ops-mcp .
```

---

## Architecture

```
spring-ops-mcp/
├── spring-ops-mcp-core/          # Core MCP tools & Actuator client
│   ├── ActuatorMcpTools.java     # 18 @Tool-annotated methods
│   ├── ActuatorClient.java       # HTTP client for Actuator endpoints
│   ├── AppRegistry.java          # Registered apps storage
│   └── RegisteredApp.java        # App model
│
├── spring-ops-mcp-starter/       # Spring Boot Starter (embedded mode)
│   ├── SpringOpsMcpAutoConfiguration.java
│   └── SpringOpsMcpProperties.java
│
└── spring-ops-mcp-server/        # Standalone server application
    └── SpringOpsMcpServerApplication.java
```

### How It Works

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Developer Machine                            │
│                                                                     │
│  ┌─────────────┐         ┌──────────────────────┐                  │
│  │ Claude Code │◄───────►│   spring-ops-mcp     │                  │
│  │   Cursor    │   MCP   │   (Embedded or       │                  │
│  │  Windsurf   │ Protocol│    Standalone)       │                  │
│  └─────────────┘         └──────────┬───────────┘                  │
│                                     │                               │
│                          HTTP calls to /actuator/*                  │
│                                     │                               │
│         ┌───────────────────────────┼───────────────────────┐      │
│         ▼                           ▼                       ▼      │
│  ┌─────────────┐           ┌─────────────┐          ┌─────────────┐│
│  │user-service │           │order-service│          │payment-svc  ││
│  │    :8081    │           │    :8082    │          │    :8083    ││
│  │ /actuator/* │           │ /actuator/* │          │ /actuator/* ││
│  └─────────────┘           └─────────────┘          └─────────────┘│
└─────────────────────────────────────────────────────────────────────┘
```

---

## Roadmap

- [ ] **Authentication support** — Basic auth, Bearer token, mTLS for secured Actuator endpoints
- [ ] **Prometheus metrics** — Query Prometheus for historical data
- [ ] **Log streaming** — Stream application logs via MCP
- [ ] **Kubernetes integration** — Pod discovery, kubectl operations
- [ ] **Flyway/Liquibase** — Migration status and management
- [ ] **Spring Cloud Config** — Configuration management across environments

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the Apache License 2.0 — see the [LICENSE](LICENSE) file for details.

---

## Related Projects

- [Spring AI](https://spring.io/projects/spring-ai) — AI integration for Spring
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) — Official MCP SDK for Java
- [Model Context Protocol](https://modelcontextprotocol.io/) — The MCP specification
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/) — Production-ready features

---

## Support

- 🐛 [Report a bug](https://github.com/sigachev/spring-ops-mcp/issues/new?template=bug_report.md)
- 💡 [Request a feature](https://github.com/sigachev/spring-ops-mcp/issues/new?template=feature_request.md)
- 💬 [Discussions](https://github.com/sigachev/spring-ops-mcp/discussions)

---

## Author

**Mikhail Sigachev**

- GitHub: [@sigachev](https://github.com/sigachev/)
- Project: [spring-ops-mcp](https://github.com/sigachev/spring-ops-mcp)

Built with ❤️ for the Spring Boot community.
