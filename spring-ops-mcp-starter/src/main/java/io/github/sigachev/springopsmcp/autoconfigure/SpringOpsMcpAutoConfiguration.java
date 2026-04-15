package io.github.sigachev.springopsmcp.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sigachev.springopsmcp.client.ActuatorClient;
import io.github.sigachev.springopsmcp.model.AppRegistry;
import io.github.sigachev.springopsmcp.tools.ActuatorMcpTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Auto-configuration for Spring Ops MCP.
 * 
 * Enables embedding an MCP server directly in your Spring Boot application,
 * allowing AI assistants to interact with your app's Actuator endpoints.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(ActuatorMcpTools.class)
@ConditionalOnProperty(prefix = "spring.ops.mcp", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpringOpsMcpProperties.class)
public class SpringOpsMcpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppRegistry appRegistry(SpringOpsMcpProperties properties) {
        AppRegistry registry = new AppRegistry();
        
        // Register apps from configuration
        if (properties.getApps() != null) {
            properties.getApps().forEach((name, config) -> {
                log.info("Auto-registering app from config: {} -> {}", name, config.getUrl());
                registry.register(new io.github.sigachev.springopsmcp.model.RegisteredApp(
                        name,
                        config.getUrl(),
                        config.getActuatorPath() != null ? config.getActuatorPath() : "/actuator"
                ));
            });
        }
        
        // Auto-register self if enabled
        if (properties.isRegisterSelf()) {
            String selfUrl = "http://localhost:" + properties.getSelfPort();
            log.info("Auto-registering self as '{}' at {}", properties.getSelfName(), selfUrl);
            registry.register(new io.github.sigachev.springopsmcp.model.RegisteredApp(
                    properties.getSelfName(),
                    selfUrl,
                    "/actuator"
            ));
        }
        
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public ActuatorClient actuatorClient(RestClient.Builder restClientBuilder, 
                                         AppRegistry appRegistry,
                                         ObjectMapper objectMapper) {
        return new ActuatorClient(restClientBuilder, appRegistry, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ActuatorMcpTools actuatorMcpTools(ActuatorClient actuatorClient,
                                              AppRegistry appRegistry,
                                              ObjectMapper objectMapper) {
        log.info("Registering Actuator MCP tools");
        return new ActuatorMcpTools(actuatorClient, appRegistry, objectMapper);
    }
}
