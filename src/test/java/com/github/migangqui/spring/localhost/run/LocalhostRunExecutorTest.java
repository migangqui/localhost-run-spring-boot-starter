package com.github.migangqui.spring.localhost.run;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class LocalhostRunExecutorTest {

    private final LocalhostRunExecutor localhostRunExecutor = new LocalhostRunExecutor();

    @Test
    void run_ok() throws IOException {
        // given
        ReflectionTestUtils.setField(localhostRunExecutor, "serverPort", "8080");

        // when
        final var domain = localhostRunExecutor.run();

        // then
        assertThat(domain)
                .isNotNull()
                .contains("https://")
                .contains(".link");
    }

}