package io.github.lehamohan.bdd.server;

import io.github.lehamohan.bdd.config.TestConfig;

import java.io.IOException;
import java.io.UncheckedIOException;

/** Process-wide handle on the application under test (embedded server or an external base URL). */
public final class AppUnderTest {

    private static DemoAppServer server;
    private static String baseUrl;

    private AppUnderTest() {
    }

    public static synchronized void start() {
        if (baseUrl != null) {
            return;
        }
        String override = TestConfig.baseUrlOverride();
        if (!override.isBlank()) {
            baseUrl = override.endsWith("/") ? override : override + "/";
            return;
        }
        try {
            server = new DemoAppServer(TestConfig.appBindAddress(), TestConfig.appPort(), TestConfig.appHost()).start();
            baseUrl = server.baseUrl();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not start the demo app server", e);
        }
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
        baseUrl = null;
    }

    public static synchronized String baseUrl() {
        if (baseUrl == null) {
            throw new IllegalStateException("Application not started; the @BeforeAll hook should have run");
        }
        return baseUrl;
    }
}
