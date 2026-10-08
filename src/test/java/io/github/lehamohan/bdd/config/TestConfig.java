package io.github.lehamohan.bdd.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;

/**
 * Typed access to run configuration. Resolution order (highest first):
 * {@code -Dkey} system property, {@code KEY} environment variable (dots to underscores),
 * then {@code test.properties} on the classpath.
 */
public final class TestConfig {

    private static final Properties DEFAULTS = load("test.properties");

    private TestConfig() {
    }

    public static String browser() {
        return get("browser", "chrome").toLowerCase(Locale.ROOT);
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }

    /** Selenium Grid / remote endpoint, e.g. http://localhost:4444. Blank means local browser. */
    public static String remoteUrl() {
        return get("remote.url", "");
    }

    /** Optional explicit Chrome/Chromium binary (useful when Chrome is not on the default path). */
    public static String chromeBinary() {
        return get("chrome.binary", "");
    }

    /** Set to test an already-deployed copy of the demo app instead of the embedded server. */
    public static String baseUrlOverride() {
        return get("base.url", "");
    }

    public static String appBindAddress() {
        return get("app.bind", "127.0.0.1");
    }

    /** Hostname the browser uses to reach the embedded app (e.g. host.docker.internal for Grid). */
    public static String appHost() {
        return get("app.host", "localhost");
    }

    public static int appPort() {
        return Integer.parseInt(get("app.port", "0"));
    }

    public static Duration explicitWait() {
        return Duration.ofSeconds(Long.parseLong(get("wait.explicit.seconds", "10")));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(Long.parseLong(get("wait.pageload.seconds", "30")));
    }

    public static int windowWidth() {
        return Integer.parseInt(get("window.width", "1366"));
    }

    public static int windowHeight() {
        return Integer.parseInt(get("window.height", "900"));
    }

    static String get(String key, String fallback) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) {
            return sys.trim();
        }
        String env = System.getenv(key.toUpperCase(Locale.ROOT).replace('.', '_'));
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return DEFAULTS.getProperty(key, fallback).trim();
    }

    private static Properties load(String resource) {
        Properties props = new Properties();
        try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return props;
    }
}
