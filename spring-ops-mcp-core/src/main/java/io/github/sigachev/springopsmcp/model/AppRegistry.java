package io.github.sigachev.springopsmcp.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of Spring Boot applications to monitor.
 * Thread-safe for concurrent access.
 */
@Slf4j
@Component
public class AppRegistry {

    private final Map<String, RegisteredApp> apps = new ConcurrentHashMap<>();

    /**
     * Register a new application.
     */
    public void register(RegisteredApp app) {
        log.info("Registering app: {} at {}", app.getName(), app.getUrl());
        apps.put(app.getName().toLowerCase(), app);
    }

    /**
     * Get an application by name (case-insensitive).
     */
    public Optional<RegisteredApp> get(String name) {
        return Optional.ofNullable(apps.get(name.toLowerCase()));
    }

    /**
     * Remove an application by name.
     */
    public boolean remove(String name) {
        RegisteredApp removed = apps.remove(name.toLowerCase());
        if (removed != null) {
            log.info("Removed app: {}", name);
            return true;
        }
        return false;
    }

    /**
     * Get all registered applications.
     */
    public Collection<RegisteredApp> getApps() {
        return apps.values();
    }

    /**
     * Check if an application is registered.
     */
    public boolean exists(String name) {
        return apps.containsKey(name.toLowerCase());
    }

    /**
     * Clear all registered applications.
     */
    public void clear() {
        apps.clear();
        log.info("Cleared all registered apps");
    }

    /**
     * Get the number of registered applications.
     */
    public int size() {
        return apps.size();
    }
}
