package io.github.sigachev.springopsmcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a registered Spring Boot application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredApp {

    /**
     * Unique name for this application (e.g., "user-service", "order-api")
     */
    private String name;

    /**
     * Base URL of the application (e.g., "http://localhost:8081")
     */
    private String url;

    /**
     * Actuator base path (default: "/actuator")
     */
    private String actuatorPath = "/actuator";

    public RegisteredApp(String name, String url) {
        this.name = name;
        this.url = url;
        this.actuatorPath = "/actuator";
    }
}
