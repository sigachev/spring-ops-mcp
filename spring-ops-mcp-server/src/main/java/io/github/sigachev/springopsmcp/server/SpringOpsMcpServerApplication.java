package io.github.sigachev.springopsmcp.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Standalone Spring Ops MCP Server.
 * 
 * Run this application to provide MCP access to multiple Spring Boot applications.
 * 
 * Usage:
 *   java -jar spring-ops-mcp-server.jar
 * 
 * Then connect Claude Code:
 *   claude mcp add spring-ops --transport http http://localhost:8090/mcp/sse
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "io.github.sigachev.springopsmcp")
public class SpringOpsMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringOpsMcpServerApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("""
                
                ╔═══════════════════════════════════════════════════════════════╗
                ║                    Spring Ops MCP Server                      ║
                ╠═══════════════════════════════════════════════════════════════╣
                ║  MCP Endpoint: http://localhost:8090/mcp/sse                  ║
                ║                                                               ║
                ║  Connect Claude Code:                                         ║
                ║    claude mcp add spring-ops \\                                ║
                ║      --transport http http://localhost:8090/mcp/sse           ║
                ║                                                               ║
                ║  Then try:                                                    ║
                ║    "List registered apps"                                     ║
                ║    "Register user-service at http://localhost:8081"           ║
                ║    "Check health of user-service"                             ║
                ╚═══════════════════════════════════════════════════════════════╝
                """);
    }
}
