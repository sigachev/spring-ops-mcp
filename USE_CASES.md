# Real-World Use Cases

> **Why Spring Ops MCP?** Because debugging Spring Boot apps shouldn't require 10 browser tabs, 5 terminal windows, and a PhD in Actuator endpoints.

---

## The Problem

Every Spring Boot developer knows this workflow:

1. Something's wrong in your app
2. Open browser → `localhost:8081/actuator/health`
3. Copy the JSON, paste somewhere to read it
4. Open another tab → `/actuator/metrics`
5. "What was that metric name again?" → Google it
6. `/actuator/metrics/http.server.requests?tag=uri:/api/users&tag=method:GET`
7. Copy more JSON, try to make sense of it
8. Open terminal → check logs
9. Realize you need DEBUG logs → edit application.properties → restart app
10. Reproduce the issue again
11. Finally find the problem after 30 minutes

**With Spring Ops MCP:**

```
You: "My /api/users endpoint is slow, investigate"

Claude: Found the issue in 3 seconds:
        - Average response time: 2.3s
        - Database connection pool: 10/10 exhausted  
        - 4 threads blocked waiting for connections
        
        Fix: increase hikari.maximum-pool-size to 20
```

---

## Use Case 1: Morning Health Check

### Before (5-10 minutes)
```
- Open browser tab for each service
- Navigate to /actuator/health for each
- Manually compare memory usage
- Check if any indicators are DOWN
- Write it down somewhere
```

### After (10 seconds)
```
You: How are my services doing this morning?

Claude: All 4 services healthy:

┌──────────────────┬────────┬─────────────┬─────────────────┐
│ Service          │ Status │ Memory      │ Notes           │
├──────────────────┼────────┼─────────────┼─────────────────┤
│ user-service     │ ✓ UP   │ 245/512 MB  │ All good        │
│ order-service    │ ✓ UP   │ 312/512 MB  │ All good        │
│ payment-service  │ ✓ UP   │ 198/512 MB  │ All good        │
│ notification-svc │ ✓ UP   │ 156/512 MB  │ All good        │
└──────────────────┴────────┴─────────────┴─────────────────┘

All database connections healthy. Redis responding normally.
```

---

## Use Case 2: Production Incident Response

### Scenario
It's 2 AM. PagerDuty alert: "High latency on checkout endpoint"

### Before (15-30 minutes)
```
1. SSH into server or open VPN
2. Find the right logs
3. Try to correlate timestamps
4. Check metrics dashboards (if you have them)
5. Guess at what might be wrong
6. Make a change, deploy, hope it works
```

### After (2 minutes)
```
You: Checkout is slow in production. What's happening?

Claude: Investigating order-service...

📊 /api/checkout performance (last 5 min):
   - p50: 450ms ✓
   - p95: 8.2s ⚠️  (should be <1s)
   - p99: 12.4s ❌

🔍 Root cause identified: Database connection pool exhaustion

   HikariCP Status:
   - Active connections: 10/10 (100%)
   - Pending threads: 23 (waiting for connection)
   - Connection timeout: 30s

   Thread dump shows:
   - 23 threads BLOCKED at HikariPool.getConnection()
   - Slow query: OrderRepository.findByUserIdWithItems() avg 4.2s

💡 Immediate fix:
   1. Increase pool: spring.datasource.hikari.maximum-pool-size=25
   
   Long-term fix:
   2. Add index: CREATE INDEX idx_orders_user_id ON orders(user_id)
   3. Paginate the query or use lazy loading for items
```

---

## Use Case 3: Onboarding New Team Members

### Scenario
New developer joins the team. Needs to understand the microservices.

### Before (days)
```
- Read outdated wiki docs
- Ask teammates repeatedly
- Trace through code manually
- Still confused after a week
```

### After (minutes)
```
You: I'm new here. What does order-service do?

Claude: order-service is your Order Management API.

📡 REST Endpoints (12 total):

   Orders CRUD:
   POST   /api/orders           → OrderController.create()
   GET    /api/orders           → OrderController.list()
   GET    /api/orders/{id}      → OrderController.getById()
   PUT    /api/orders/{id}      → OrderController.update()
   DELETE /api/orders/{id}      → OrderController.cancel()

   Payments:
   POST   /api/orders/{id}/pay  → PaymentController.process()
   GET    /api/orders/{id}/payment → PaymentController.status()

   User queries:
   GET    /api/users/{id}/orders → UserOrderController.listByUser()

🗄️ Database: PostgreSQL at jdbc:postgresql://db:5432/orders

🔗 Dependencies:
   - payment-service (for payment processing)
   - user-service (for user validation)
   - Kafka (publishes order events)
   - Redis (caches order lookups)

📦 Key beans: OrderService, PaymentClient, OrderRepository,
              KafkaOrderProducer, OrderCacheManager
```

---

## Use Case 4: Debugging Without Restarting

### Scenario
Authentication is failing for some users. You need DEBUG logs but don't want to restart the app (would lose the user's session).

### Before
```
1. Edit application.properties
2. Add logging.level.com.myapp.auth=DEBUG
3. Restart the app
4. Ask user to try again
5. Hope you can reproduce it
6. Remember to remove DEBUG later
```

### After
```
You: Enable debug logging for the auth package

Claude: ✓ Set com.myapp.auth to DEBUG in user-service

You: [User tries to log in again]

You: What do the auth debug logs show?

Claude: Found the issue in TokenValidator:

   DEBUG TokenValidator - Validating JWT for user: john@example.com
   DEBUG TokenValidator - Token issuer: https://auth.myapp.com ✓
   DEBUG TokenValidator - Token audience: https://api.myapp.com/ 
   DEBUG TokenValidator - Expected audience: https://api.myapp.com
   DEBUG TokenValidator - FAILED: Audience mismatch (trailing slash)

   The OAuth provider is including a trailing slash in the audience 
   claim, but your config expects it without the slash.

   Fix in application.properties:
   spring.security.oauth2.resourceserver.jwt.audiences=https://api.myapp.com/

You: Got it, thanks. Set logging back to INFO.

Claude: ✓ Set com.myapp.auth to INFO
```

---

## Use Case 5: Performance Optimization

### Scenario
App is using too much memory. Need to understand what's happening.

### Before
```
1. Attach a profiler (VisualVM, JProfiler)
2. Generate heap dump
3. Download 2GB file
4. Wait 10 minutes for it to load
5. Try to find the problem in a sea of objects
```

### After
```
You: My user-service is using a lot of memory. What's going on?

Claude: Analyzing user-service memory...

📊 JVM Memory:
   Heap: 892 MB / 1024 MB (87% used) ⚠️
   Non-heap: 124 MB
   
   GC Activity (last hour):
   - Young GC: 847 collections, 12.4s total
   - Old GC: 23 collections, 45.2s total ⚠️

📈 Top memory metrics:
   - jvm.buffer.memory.used: 245 MB
   - jvm.memory.used{area=heap,id=G1 Old Gen}: 678 MB ⚠️
   - hikaricp.connections: holding 10 connections

🔍 Likely issues:
   1. Old Gen is 78% full - objects not being collected
   2. High Old GC frequency suggests memory leak
   3. Buffer memory is elevated (check file I/O or NIO usage)

💡 Recommendations:
   1. Check for unclosed resources (streams, connections)
   2. Review @Cacheable usage - might be caching too much
   3. Add -XX:+HeapDumpOnOutOfMemoryError for next OOM
   4. Consider increasing heap: -Xmx2g
```

---

## Use Case 6: Comparing Environments

### Scenario
"It works on my machine but not in staging"

### Before
```
1. SSH to staging
2. Cat the application.properties
3. Compare manually with local
4. Miss the one property that's different
5. Waste 2 hours
```

### After
```
You: Compare user-service config between local and staging

Claude: Configuration differences:

| Property                      | Local              | Staging              |
|-------------------------------|--------------------|--------------------- |
| spring.datasource.url         | localhost:5432     | staging-db.aws:5432  |
| spring.redis.host             | localhost          | staging-redis.aws    |
| logging.level.root            | DEBUG              | INFO                 |
| app.feature.new-login         | true               | false ⚠️             |
| app.cache.ttl-seconds         | 60                 | 3600                 |
| server.tomcat.max-threads     | 200                | 50 ⚠️                |

⚠️ Found 2 significant differences:
1. new-login feature is ON locally but OFF in staging
2. Staging has only 50 Tomcat threads (vs 200 local) - could cause queuing
```

---

## Use Case 7: Cache Management

### Scenario
Data was updated in the database but the app is showing stale data.

### Before
```
1. Try to remember where caches are defined
2. Find the cache names in code
3. Figure out how to clear them
4. Write a one-off endpoint or restart the app
```

### After
```
You: Users are seeing old product prices. Clear the pricing caches.

Claude: Found 3 caches in catalog-service:

   products:        1,247 entries
   product-prices:  1,247 entries  ← pricing data
   categories:      89 entries

You: Clear product-prices

Claude: ✓ Cleared product-prices cache (1,247 entries removed)

   Next request will fetch fresh prices from database.
```

---

## Use Case 8: Understanding Auto-Configuration

### Scenario
Spring Boot auto-configured something you didn't expect.

### Before
```
1. Add debug=true to application.properties
2. Restart app
3. Scroll through 500 lines of startup logs
4. Try to find the relevant condition
5. Still confused
```

### After
```
You: Why is Redis auto-configured? I didn't add it.

Claude: Redis auto-configuration report for user-service:

✓ MATCHED (why Redis IS configured):
   RedisAutoConfiguration:
      - @ConditionalOnClass found: RedisOperations, Lettuce
      - Reason: spring-boot-starter-data-redis is on classpath
   
   RedisRepositoriesAutoConfiguration:
      - @ConditionalOnBean found: RedisConnectionFactory
      - @ConditionalOnProperty: spring.data.redis.repositories.enabled=true

📦 Redis came from this dependency chain:
   spring-boot-starter-cache
   └── spring-boot-starter-data-redis (optional, but present)
       └── lettuce-core

💡 To disable Redis auto-configuration:
   spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
   
   Or remove spring-boot-starter-data-redis if you don't need it.
```

---

## Summary: Time Saved

| Task | Before | After | Time Saved |
|------|--------|-------|------------|
| Morning health check | 10 min | 10 sec | 98% |
| Debug production issue | 30 min | 2 min | 93% |
| Onboard new developer | 2 days | 30 min | 96% |
| Change log levels | 5 min + restart | 5 sec | 99% |
| Memory investigation | 30 min | 1 min | 97% |
| Compare environments | 20 min | 10 sec | 99% |
| Clear caches | 10 min | 5 sec | 99% |
| Debug auto-config | 15 min | 30 sec | 97% |

---

## Who Is This For?

- **Solo developers** tired of context-switching between browser, terminal, and IDE
- **Backend teams** who want faster debugging and less "works on my machine"
- **DevOps engineers** who need quick insights without full observability stack
- **On-call engineers** who need answers at 2 AM, not dashboards
- **New team members** who want to understand the codebase fast
- **Anyone** who thinks "there must be a better way" when debugging Spring Boot

---

## Get Started

```bash
# Add to your Spring Boot app
<dependency>
    <groupId>io.github.sigachev</groupId>
    <artifactId>spring-ops-mcp-starter</artifactId>
    <version>0.1.0</version>
</dependency>

# Connect Claude Code
claude mcp add my-app --transport http http://localhost:8081/mcp/sse

# Start asking questions
"Check health of my-app"
"Why is my app slow?"
"What endpoints do I have?"
```

**Stop copy-pasting JSON. Start asking questions.**

---

## Author

**Mikhail Sigachev** — [@sigachev](https://github.com/sigachev/)

[Back to main README →](README.md)
