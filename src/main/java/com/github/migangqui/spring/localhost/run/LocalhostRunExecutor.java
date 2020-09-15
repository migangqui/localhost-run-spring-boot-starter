package com.github.migangqui.spring.localhost.run;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "localhost-run.enabled", havingValue = "true")
public class LocalhostRunExecutor {

    private final Logger log = LoggerFactory.getLogger(LocalhostRunExecutor.class);

    private final Environment env;

    public LocalhostRunExecutor(final Environment env) {
        this.env = env;
    }

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.application.name:}")
    private String appName;

    @EventListener(ApplicationStartedEvent.class)
    public void run() throws IOException {
        final Process proc = Runtime.getRuntime().exec(String.format("ssh -R 80:localhost:%s ssh.localhost.run", serverPort));

        final BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String s;
        String domain = null;

        while ((s = stdInput.readLine()) != null) {
            if (s.contains("{") && s.contains("domain")) {
                domain = (String) new ObjectMapper().readValue(s, Map.class).get("domain");
                break;
            }
        }

        if (domain != null) {
            log.info("Remote access to application with url {}", "http://" + domain);
        } else {
            log.warn("Remote access to application is not available");
        }
    }
}
