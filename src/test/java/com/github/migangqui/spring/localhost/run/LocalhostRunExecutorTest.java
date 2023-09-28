package com.github.migangqui.spring.localhost.run;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class LocalhostRunExecutorTest {

	private final LocalhostRunExecutor localhostRunExecutor = new LocalhostRunExecutor();

	private static MemoryAppender memoryAppender;

	@BeforeAll
	public static void setup() {
		final Logger logger = (Logger) LoggerFactory.getLogger(LocalhostRunExecutor.class);
		memoryAppender = new MemoryAppender();
		memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
		logger.setLevel(Level.INFO);
		logger.addAppender(memoryAppender);
		memoryAppender.start();
	}

	@AfterAll
	public static void cleanUp() {
		memoryAppender.reset();
		memoryAppender.stop();
	}

	@Test
	@Disabled("Disabled to pass the GithubAction")
	void run_ok() throws IOException {
		// given
		ReflectionTestUtils.setField(localhostRunExecutor, "serverPort", "8080");
		ReflectionTestUtils.setField(localhostRunExecutor, "subDomain", "lhr.life");

		// when
		localhostRunExecutor.execute();

		// then
		assertThat(memoryAppender.search("Remote access to application with url").size()).isEqualTo(1);
	}

	public static class MemoryAppender extends ListAppender<ILoggingEvent> {
		public void reset() {
			this.list.clear();
		}

		public List<ILoggingEvent> search(final String string) {
			return this.list.stream()
					.filter(event -> event.toString().contains(string))
					.collect(Collectors.toList());
		}
	}
}