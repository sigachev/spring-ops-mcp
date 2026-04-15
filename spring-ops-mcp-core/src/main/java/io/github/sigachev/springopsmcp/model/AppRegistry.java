package io.github.sigachev.springopsmcp.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AppRegistry {

    private static final Logger log = LoggerFactory.getLogger(AppRegistry.class);
    private final Map<String, RegisteredApp> apps = new ConcurrentHashMap<>();

    public void register(RegisteredApp app) {
        log.info("Registering app: {} at {}", app.getName(), app.getUrl());
        apps.put(app.getName().toLowerCase(), app);
    }

    public Optional<RegisteredApp> get(String name) {
        return Optional.ofNullable(apps.get(name.toLowerCase()));
    }

    public boolean remove(String name) {
        RegisteredApp removed = apps.remove(name.toLowerCase());
        if (removed != null) {
            log.info("Removed app: {}", name);
            return true;
        }
        return false;
    }

    public Collection<RegisteredApp> getApps() {
        return apps.values();
    }

    public boolean exists(String name) {
        return apps.containsKey(name.toLowerCase());
    }

    public void clear() {
        apps.clear();
        log.info("Cleared all registered apps");
    }

    public int size() {
        return apps.size();
    }
}