package io.github.sigachev.springopsmcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sigachev.springopsmcp.client.ActuatorClient;
import io.github.sigachev.springopsmcp.model.AppRegistry;
import io.github.sigachev.springopsmcp.model.RegisteredApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActuatorMcpTools {

    private static final Logger log = LoggerFactory.getLogger(ActuatorMcpTools.class);

    private final ActuatorClient actuatorClient;
    private final AppRegistry appRegistry;
    private final ObjectMapper objectMapper;

    public ActuatorMcpTools(ActuatorClient actuatorClient, AppRegistry appRegistry, ObjectMapper objectMapper) {
        this.actuatorClient = actuatorClient;
        this.appRegistry = appRegistry;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "List all registered Spring Boot applications")
    public List<Map<String, Object>> listApps() {
        return appRegistry.getApps().stream()
                .map(app -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("name", app.getName());
                    map.put("url", app.getUrl());
                    map.put("actuatorPath", app.getActuatorPath());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Tool(description = "Register a Spring Boot application for monitoring")
    public String registerApp(
            @ToolParam(description = "App name") String name,
            @ToolParam(description = "Base URL") String url,
            @ToolParam(description = "Actuator path, default /actuator") String actuatorPath) {
        String path = (actuatorPath == null || actuatorPath.isBlank()) ? "/actuator" : actuatorPath;
        appRegistry.register(new RegisteredApp(name, url, path));
        try {
            actuatorClient.getHealth(name);
            return "Registered '" + name + "' at " + url + " - connection verified!";
        } catch (Exception e) {
            return "Registered '" + name + "' but could not verify: " + e.getMessage();
        }
    }

    @Tool(description = "Remove an application from monitoring")
    public String removeApp(@ToolParam(description = "App name") String name) {
        if (appRegistry.remove(name)) {
            return "Removed '" + name + "'";
        }
        return "Application '" + name + "' not found";
    }

    @Tool(description = "Get health status of an application")
    public JsonNode getHealth(@ToolParam(description = "App name") String appName) {
        log.debug("Getting health for app: {}", appName);
        return actuatorClient.getHealth(appName);
    }

    @Tool(description = "Get application info")
    public JsonNode getInfo(@ToolParam(description = "App name") String appName) {
        return actuatorClient.getInfo(appName);
    }

    @Tool(description = "List available metrics")
    public JsonNode listMetrics(@ToolParam(description = "App name") String appName) {
        return actuatorClient.getMetrics(appName);
    }

    @Tool(description = "Get a specific metric")
    public JsonNode getMetric(
            @ToolParam(description = "App name") String appName,
            @ToolParam(description = "Metric name") String metricName,
            @ToolParam(description = "Tags filter") String tags) {
        return actuatorClient.getMetric(appName, metricName, tags);
    }

    @Tool(description = "Get environment properties")
    public JsonNode getEnv(
            @ToolParam(description = "App name") String appName,
            @ToolParam(description = "Property pattern") String pattern) {
        if (pattern != null && !pattern.isBlank()) {
            return actuatorClient.getEnvProperty(appName, pattern);
        }
        return actuatorClient.getEnv(appName);
    }

    @Tool(description = "List Spring beans")
    public JsonNode getBeans(@ToolParam(description = "App name") String appName) {
        return actuatorClient.getBeans(appName);
    }

    @Tool(description = "Get REST endpoint mappings")
    public JsonNode getMappings(@ToolParam(description = "App name") String appName) {
        return actuatorClient.getMappings(appName);
    }

    @Tool(description = "Get log levels")
    public JsonNode getLoggers(
            @ToolParam(description = "App name") String appName,
            @ToolParam(description = "Logger name") String loggerName) {
        if (loggerName != null && !loggerName.isBlank()) {
            return actuatorClient.getLogger(appName, loggerName);
        }
        return actuatorClient.getLoggers(appName);
    }

    @Tool(description = "Set log level at runtime")
    public String setLogLevel(
            @ToolParam(description = "App name") String appName,
            @ToolParam(description = "Logger name") String loggerName,
            @ToolParam(description = "Level: TRACE,DEBUG,INFO,WARN,ERROR,OFF") String level) {
        try {
            actuatorClient.setLogLevel(appName, loggerName, level.toUpperCase());
            return "Set " + loggerName + " to " + level.toUpperCase();
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    @Tool(description = "Get thread dump")
    public JsonNode getThreadDump(@ToolParam(description = "App name") String appName) {
        return actuatorClient.getThreadDump(appName);
    }

    @Tool(description = "Get scheduled tasks")
    public JsonNode getScheduledTasks(@ToolParam(description = "App name") String appName) {
        return actuatorClient.getScheduledTasks(appName);
    }

    @Tool(description = "Get auto-config conditions")
    public JsonNode getConditions(@ToolParam(description = "App name") String appName) {
        return actuatorClient.getConditions(appName);
    }

    @Tool(description = "Get HTTP exchanges")
    public JsonNode getHttpExchanges(@ToolParam(description = "App name") String appName) {
        return actuatorClient.getHttpExchanges(appName);
    }

    @Tool(description = "List caches")
    public JsonNode getCaches(@ToolParam(description = "App name") String appName) {
        return actuatorClient.getCaches(appName);
    }

    @Tool(description = "Clear a cache")
    public String clearCache(
            @ToolParam(description = "App name") String appName,
            @ToolParam(description = "Cache name or 'all'") String cacheName) {
        try {
            if ("all".equalsIgnoreCase(cacheName)) {
                actuatorClient.clearAllCaches(appName);
                return "Cleared all caches";
            } else {
                actuatorClient.clearCache(appName, cacheName);
                return "Cleared cache '" + cacheName + "'";
            }
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }
}