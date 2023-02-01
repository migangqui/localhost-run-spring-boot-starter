package com.github.migangqui.spring.localhost.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ConditionalOnProperty(name = "localhost-run.enabled", havingValue = "true")
public class LocalhostRunExecutor {

    private final Logger log = LoggerFactory.getLogger(LocalhostRunExecutor.class);

    public LocalhostRunExecutor() {}

    @Value("${server.port:8080}")
    private String serverPort;

    @EventListener(ApplicationStartedEvent.class)
    public String run() throws IOException {
        final Process process = Runtime.getRuntime().exec(String.format("ssh -R 80:localhost:%s localhost.run", serverPort));

        final BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        final AtomicReference<String> domain = new AtomicReference<>();

        String line;
        
        while ((line = stdInput.readLine()) != null) {
            if (line.contains("tunneled with tls termination")) {
                domain.set(line.split(",")[1].trim());
                break;
            }
        }

        if (domain.get() != null) {
            log.info("Remote access to application with url {}", domain.get());
        } else {
            log.info("Remote access to application is not available");
        }

        return domain.get();
    }
}
