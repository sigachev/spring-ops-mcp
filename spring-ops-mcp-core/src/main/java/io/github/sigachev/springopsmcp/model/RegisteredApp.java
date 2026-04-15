package io.github.sigachev.springopsmcp.model;

public class RegisteredApp {

    private String name;
    private String url;
    private String actuatorPath = "/actuator";

    public RegisteredApp() {
    }

    public RegisteredApp(String name, String url) {
        this.name = name;
        this.url = url;
        this.actuatorPath = "/actuator";
    }

    public RegisteredApp(String name, String url, String actuatorPath) {
        this.name = name;
        this.url = url;
        this.actuatorPath = actuatorPath != null ? actuatorPath : "/actuator";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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