package io.github.sigachev.springopsmcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sigachev.springopsmcp.client.ActuatorClient;
import io.github.sigachev.springopsmcp.model.AppRegistry;
import io.github.sigachev.springopsmcp.model.RegisteredApp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP Tools for Spring Boot Actuator endpoints.
 * These tools allow AI assistants to interact with running Spring Boot applications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActuatorMcpTools {

    private final ActuatorClient actuatorClient;
    private final AppRegistry appRegistry;
    private final ObjectMapper objectMapper;

    // ==================== App Management ====================

    @Tool(description = "List all registered Spring Boot applications that can be monitored")
    public List<Map<String, Object>> listApps() {
        return appRegistry.getApps().stream()
                .map(app -> Map.<String, Object>of(
                        "name", app.getName(),
                        "url", app.getUrl(),
                        "actuatorPath", app.getActuatorPath()
                ))
                .collect(Collectors.toList());
    }

    @Tool(description = "Register a new Spring Boot application for monitoring. The app must have Actuator endpoints exposed.")
    public String registerApp(
            @ToolParam(description = "Unique name for the application (e.g., 'user-service', 'order-api')") String name,
            @ToolParam(description = "Base URL of the application (e.g., 'http://localhost:8081')") String url,
            @ToolParam(description = "Actuator base path, defaults to '/actuator'") String actuatorPath
    ) {
        String path = (actuatorPath == null || actuatorPath.isBlank()) ? "/actuator" : actuatorPath;
        appRegistry.register(new RegisteredApp(name, url, path));
        
        // Verify connection
        try {
            actuatorClient.getHealth(name);
            return "Successfully registered '" + name + "' at " + url + " - connection verified!";
        } catch (Exception e) {
            return "Registered '" + name + "' but could not verify connection: " + e.getMessage() + 
                   ". Make sure the app is running and Actuator endpoints are exposed.";
        }
    }

    @Tool(description = "Remove a registered application from monitoring")
    public String removeApp(
            @ToolParam(description = "Name of the application to remove") String name
    ) {
        if (appRegistry.remove(name)) {
            return "Removed '" + name + "' from monitoring";
        }
        return "Application '" + name + "' not found";
    }

    // ==================== Health & Status ====================

    @Tool(description = "Get the health status of a Spring Boot application including all health indicators (database, redis, disk, custom indicators)")
    public JsonNode getHealth(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        log.debug("Getting health for app: {}", appName);
        return actuatorClient.getHealth(appName);
    }

    @Tool(description = "Get application info including build details, git commit, and custom info contributors")
    public JsonNode getInfo(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        return actuatorClient.getInfo(appName);
    }

    // ==================== Metrics ====================

    @Tool(description = "List all available metric names for an application. Use this to discover what metrics are available before querying specific ones.")
    public JsonNode listMetrics(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        return actuatorClient.getMetrics(appName);
    }

    @Tool(description = "Get a specific metric value with optional tag filtering. Common metrics: " +
            "jvm.memory.used, jvm.memory.max, jvm.gc.pause, " +
            "http.server.requests (with tags uri, method, status), " +
            "hikaricp.connections.active, hikaricp.connections.pending, " +
            "system.cpu.usage, process.cpu.usage, " +
            "tomcat.threads.current, tomcat.threads.busy")
    public JsonNode getMetric(
            @ToolParam(description = "Name of the registered application") String appName,
            @ToolParam(description = "Metric name (e.g., 'jvm.memory.used', 'http.server.requests')") String metricName,
            @ToolParam(description = "Optional comma-separated tags to filter (e.g., 'uri:/api/users,method:GET')") String tags
    ) {
        return actuatorClient.getMetric(appName, metricName, tags);
    }

    // ==================== Configuration ====================

    @Tool(description = "Get environment properties of an application. Can filter by property name pattern. " +
            "Useful for checking database URLs, feature flags, Spring profiles, and configuration values.")
    public JsonNode getEnv(
            @ToolParam(description = "Name of the registered application") String appName,
            @ToolParam(description = "Optional property name or pattern to filter (e.g., 'spring.datasource', 'server.port')") String pattern
    ) {
        JsonNode env = actuatorClient.getEnv(appName);
        
        if (pattern != null && !pattern.isBlank()) {
            // Filter will be done client-side for flexibility
            return actuatorClient.getEnvProperty(appName, pattern);
        }
        return env;
    }

    @Tool(description = "List all Spring beans in the application. Useful for understanding application structure and dependencies.")
    public JsonNode getBeans(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        return actuatorClient.getBeans(appName);
    }

    @Tool(description = "Get all REST endpoint mappings (URL routes) in the application. " +
            "Shows HTTP methods, paths, and handler methods. Essential for understanding the API surface.")
    public JsonNode getMappings(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        return actuatorClient.getMappings(appName);
    }

    // ==================== Logging ====================

    @Tool(description = "Get current log levels for all loggers or a specific logger. " +
            "Shows configured and effective log levels.")
    public JsonNode getLoggers(
            @ToolParam(description = "Name of the registered application") String appName,
            @ToolParam(description = "Optional specific logger name (e.g., 'com.myapp.service', 'org.springframework')") String loggerName
    ) {
        if (loggerName != null && !loggerName.isBlank()) {
            return actuatorClient.getLogger(appName, loggerName);
        }
        return actuatorClient.getLoggers(appName);
    }

    @Tool(description = "Change the log level for a specific logger at runtime. " +
            "Useful for debugging issues without restarting the application. " +
            "Valid levels: TRACE, DEBUG, INFO, WARN, ERROR, OFF")
    public String setLogLevel(
            @ToolParam(description = "Name of the registered application") String appName,
            @ToolParam(description = "Logger name (e.g., 'com.myapp.service', 'org.hibernate.SQL')") String loggerName,
            @ToolParam(description = "New log level: TRACE, DEBUG, INFO, WARN, ERROR, or OFF") String level
    ) {
        try {
            actuatorClient.setLogLevel(appName, loggerName, level.toUpperCase());
            return "Successfully set " + loggerName + " to " + level.toUpperCase() + " in " + appName;
        } catch (Exception e) {
            return "Failed to set log level: " + e.getMessage();
        }
    }

    // ==================== Diagnostics ====================

    @Tool(description = "Get a thread dump from the application. " +
            "Useful for diagnosing deadlocks, high CPU usage, and thread pool exhaustion.")
    public JsonNode getThreadDump(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        return actuatorClient.getThreadDump(appName);
    }

    @Tool(description = "Get scheduled tasks configured in the application. " +
            "Shows @Scheduled methods, cron expressions, and fixed-rate/delay configurations.")
    public JsonNode getScheduledTasks(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        return actuatorClient.getScheduledTasks(appName);
    }

    @Tool(description = "Get auto-configuration conditions report. " +
            "Shows which auto-configurations were applied or skipped and why. " +
            "Useful for debugging configuration issues.")
    public JsonNode getConditions(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        return actuatorClient.getConditions(appName);
    }

    @Tool(description = "Get HTTP request statistics and traces if available. " +
            "Shows recent HTTP exchanges including request/response details.")
    public JsonNode getHttpExchanges(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        return actuatorClient.getHttpExchanges(appName);
    }

    // ==================== Caches ====================

    @Tool(description = "List all caches in the application and their statistics if available.")
    public JsonNode getCaches(
            @ToolParam(description = "Name of the registered application") String appName
    ) {
        return actuatorClient.getCaches(appName);
    }

    @Tool(description = "Clear a specific cache or all caches in the application.")
    public String clearCache(
            @ToolParam(description = "Name of the registered application") String appName,
            @ToolParam(description = "Cache name to clear, or 'all' to clear all caches") String cacheName
    ) {
        try {
            if ("all".equalsIgnoreCase(cacheName)) {
                actuatorClient.clearAllCaches(appName);
                return "Cleared all caches in " + appName;
            } else {
                actuatorClient.clearCache(appName, cacheName);
                return "Cleared cache '" + cacheName + "' in " + appName;
            }
        } catch (Exception e) {
            return "Failed to clear cache: " + e.getMessage();
        }
    }
}
