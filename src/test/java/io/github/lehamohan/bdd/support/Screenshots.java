package io.github.lehamohan.bdd.support;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** Captures PNG screenshots, stores them under target/screenshots and returns the bytes for report attachment. */
public final class Screenshots {

    private static final Logger LOG = LoggerFactory.getLogger(Screenshots.class);
    private static final Path DIR = Path.of("target", "screenshots");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private Screenshots() {
    }

    public static Optional<byte[]> capture(WebDriver driver, String name) {
        if (!(driver instanceof TakesScreenshot camera)) {
            return Optional.empty();
        }
        try {
            byte[] png = camera.getScreenshotAs(OutputType.BYTES);
            Files.createDirectories(DIR);
            Path file = DIR.resolve(sanitize(name) + "_" + LocalDateTime.now().format(TS) + ".png");
            Files.write(file, png);
            LOG.info("event=screenshot_saved path={}", file);
            return Optional.of(png);
        } catch (IOException | RuntimeException e) {
            // Never let evidence capture mask the real failure.
            LOG.warn("event=screenshot_failed reason={}", e.toString());
            return Optional.empty();
        }
    }

    static String sanitize(String name) {
        String cleaned = name.replaceAll("[^A-Za-z0-9-_]+", "_");
        return cleaned.length() > 80 ? cleaned.substring(0, 80) : cleaned;
    }
}
