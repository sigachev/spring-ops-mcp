package io.github.sigachev.springopsmcp.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "spring.ops.mcp")
public class SpringOpsMcpProperties {

    private boolean enabled = true;
    private boolean registerSelf = true;
    private String selfName = "self";
    private int selfPort = 8080;
    private Map<String, AppConfig> apps;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRegisterSelf() {
        return registerSelf;
    }

    public void setRegisterSelf(boolean registerSelf) {
        this.registerSelf = registerSelf;
    }

    public String getSelfName() {
        return selfName;
    }

    public void setSelfName(String selfName) {
        this.selfName = selfName;
    }

    public int getSelfPort() {
        return selfPort;
    }

    public void setSelfPort(int selfPort) {
        this.selfPort = selfPort;
    }

    public Map<String, AppConfig> getApps() {
        return apps;
    }

    public void setApps(Map<String, AppConfig> apps) {
        this.apps = apps;
    }

    public static class AppConfig {
        private String url;
        private String actuatorPath = "/actuator";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getActuatorPath() {
            return actuatorPath;
        }

        public void setActuatorPath(String actuatorPath) {
            this.actuatorPath = actuatorPath;
        }
    }
}