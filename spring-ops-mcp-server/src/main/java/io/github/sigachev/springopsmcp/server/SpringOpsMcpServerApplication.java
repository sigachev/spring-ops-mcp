package io.github.sigachev.springopsmcp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication(scanBasePackages = "io.github.sigachev.springopsmcp")
public class SpringOpsMcpServerApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringOpsMcpServerApplication.class);

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