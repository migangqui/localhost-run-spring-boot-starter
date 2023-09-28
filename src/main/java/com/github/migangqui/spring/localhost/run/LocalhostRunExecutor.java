package com.github.migangqui.spring.localhost.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "localhost-run.enabled", havingValue = "true")
public class LocalhostRunExecutor {

	private final Logger log = LoggerFactory.getLogger(LocalhostRunExecutor.class);

	public LocalhostRunExecutor() {
	}

	@Value("${server.port:8080}")
	private String serverPort;

	@Value("${localhost-run.subdomain:lhr.life}")
	private String subDomain;

	@EventListener(ApplicationStartedEvent.class)
	public void execute() throws IOException {
		final Process process = Runtime.getRuntime().exec(String.format("ssh -R 80:localhost:%s nokey@localhost.run", serverPort));

		final Pattern urlPattern = Pattern.compile("https:\\/\\/.*." + subDomain);

		new BufferedReader(new InputStreamReader(process.getInputStream())).lines()
				.filter(line -> urlPattern.matcher(line).find())
				.map(filteredLine -> findText(filteredLine, urlPattern)).findFirst()
				.ifPresentOrElse(
						url -> log.info("Remote access to application with url {}", url),
						() -> log.info("Remote access to application is not available"));
	}

	private static String findText(final String filteredLine, final Pattern urlPattern) {
		final Matcher matcher = urlPattern.matcher(filteredLine);
		matcher.find();
		return matcher.group(0);
	}

}
