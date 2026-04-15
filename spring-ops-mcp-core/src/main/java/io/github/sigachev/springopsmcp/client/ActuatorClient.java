package io.github.sigachev.springopsmcp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sigachev.springopsmcp.model.AppRegistry;
import io.github.sigachev.springopsmcp.model.RegisteredApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class ActuatorClient {

    private static final Logger log = LoggerFactory.getLogger(ActuatorClient.class);

    private final RestClient restClient;
    private final AppRegistry appRegistry;
    private final ObjectMapper objectMapper;

    public ActuatorClient(RestClient.Builder restClientBuilder, AppRegistry appRegistry, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.appRegistry = appRegistry;
        this.objectMapper = objectMapper;
    }

    public JsonNode getHealth(String appName) {
        return get(appName, "/health");
    }

    public JsonNode getInfo(String appName) {
        return get(appName, "/info");
    }

    public JsonNode getMetrics(String appName) {
        return get(appName, "/metrics");
    }

    public JsonNode getMetric(String appName, String metricName, String tags) {
        String path = "/metrics/" + metricName;
        if (tags != null && !tags.isBlank()) {
            String[] tagPairs = tags.split(",");
            StringBuilder queryParams = new StringBuilder("?");
            for (int i = 0; i < tagPairs.length; i++) {
                if (i > 0) queryParams.append("&");
                queryParams.append("tag=").append(tagPairs[i].trim());
            }
            path += queryParams.toString();
        }
        return get(appName, path);
    }

    public JsonNode getEnv(String appName) {
        return get(appName, "/env");
    }

    public JsonNode getEnvProperty(String appName, String property) {
        return get(appName, "/env/" + property);
    }

    public JsonNode getBeans(String appName) {
        return get(appName, "/beans");
    }

    public JsonNode getMappings(String appName) {
        return get(appName, "/mappings");
    }

    public JsonNode getLoggers(String appName) {
        return get(appName, "/loggers");
    }

    public JsonNode getLogger(String appName, String loggerName) {
        return get(appName, "/loggers/" + loggerName);
    }

    public void setLogLevel(String appName, String loggerName, String level) {
        RegisteredApp app = getApp(appName);
        String url = buildUrl(app, "/loggers/" + loggerName);
        log.debug("Setting log level: {} -> {} for {}", loggerName, level, appName);
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("configuredLevel", level))
                .retrieve()
                .toBodilessEntity();
    }

    public JsonNode getThreadDump(String appName) {
        return get(appName, "/threaddump");
    }

    public JsonNode getScheduledTasks(String appName) {
        return get(appName, "/scheduledtasks");
    }

    public JsonNode getConditions(String appName) {
        return get(appName, "/conditions");
    }

    public JsonNode getHttpExchanges(String appName) {
        return get(appName, "/httpexchanges");
    }

    public JsonNode getCaches(String appName) {
        return get(appName, "/caches");
    }

    public void clearCache(String appName, String cacheName) {
        RegisteredApp app = getApp(appName);
        String url = buildUrl(app, "/caches/" + cacheName);
        restClient.delete().uri(url).retrieve().toBodilessEntity();
    }

    public void clearAllCaches(String appName) {
        RegisteredApp app = getApp(appName);
        String url = buildUrl(app, "/caches");
        restClient.delete().uri(url).retrieve().toBodilessEntity();
    }

    private JsonNode get(String appName, String endpoint) {
        RegisteredApp app = getApp(appName);
        String url = buildUrl(app, endpoint);
        log.debug("GET {}", url);
        try {
            String response = restClient.get().uri(url).retrieve().body(String.class);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            log.error("Failed to call {} for {}: {}", endpoint, appName, e.getMessage());
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", true);
            error.put("message", e.getMessage());
            error.put("endpoint", endpoint);
            error.put("app", appName);
            return error;
        }
    }

    private RegisteredApp getApp(String appName) {
        return appRegistry.get(appName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Application '" + appName + "' not found. Use listApps() to see registered apps."));
    }

    private String buildUrl(RegisteredApp app, String endpoint) {
        String baseUrl = app.getUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String actuatorPath = app.getActuatorPath();
        if (!actuatorPath.startsWith("/")) {
            actuatorPath = "/" + actuatorPath;
        }
        if (actuatorPath.endsWith("/")) {
            actuatorPath = actuatorPath.substring(0, actuatorPath.length() - 1);
        }
        return baseUrl + actuatorPath + endpoint;
    }
}