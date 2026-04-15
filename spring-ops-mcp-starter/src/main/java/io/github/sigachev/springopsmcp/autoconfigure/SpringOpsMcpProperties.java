package io.github.sigachev.springopsmcp.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Configuration properties for Spring Ops MCP.
 */
@Data
@ConfigurationProperties(prefix = "spring.ops.mcp")
public class SpringOpsMcpProperties {

    /**
     * Enable/disable Spring Ops MCP (default: true)
     */
    private boolean enabled = true;

    /**
     * Automatically register this application for self-monitoring (default: true)
     */
    private boolean registerSelf = true;

    /**
     * Name to use when registering self (default: "self")
     */
    private String selfName = "self";

    /**
     * Port of this application for self-registration (default: 8080)
     */
    private int selfPort = 8080;

    /**
     * Pre-configured applications to register on startup.
     * 
     * Example:
     * spring.ops.mcp.apps.user-service.url=http://localhost:8081
     * spring.ops.mcp.apps.order-service.url=http://localhost:8082
     */
    private Map<String, AppConfig> apps;

    @Data
    public static class AppConfig {
        /**
         * Base URL of the application
         */
        private String url;

        /**
         * Actuator base path (default: /actuator)
         */
        private String actuatorPath = "/actuator";

        /**
         * Authentication type: none, basic, bearer
         */
        private String authType = "none";

        /**
         * Username for basic auth
         */
        private String username;

        /**
         * Password for basic auth
         */
        private String password;

        /**
         * Bearer token for token auth
         */
        private String token;
    }
}
