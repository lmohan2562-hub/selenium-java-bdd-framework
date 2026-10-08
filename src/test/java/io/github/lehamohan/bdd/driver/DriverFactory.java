package io.github.lehamohan.bdd.driver;

import io.github.lehamohan.bdd.config.TestConfig;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Creates a configured {@link WebDriver} for the requested browser, locally or on a Selenium Grid.
 *
 * <p>Driver binaries are resolved by Selenium Manager by default. In restricted environments, point
 * Selenium at pre-installed binaries with {@code -Dwebdriver.chrome.driver=/path/to/chromedriver}
 * and {@code -Dchrome.binary=/path/to/chrome}.
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver create() {
        Browser browser = Browser.from(TestConfig.browser());
        Capabilities options = optionsFor(browser, TestConfig.headless());
        String remote = TestConfig.remoteUrl();

        WebDriver driver;
        if (!remote.isBlank()) {
            driver = remote(remote, options);
        } else {
            driver = switch (browser) {
                case CHROME -> new ChromeDriver((ChromeOptions) options);
                case FIREFOX -> new FirefoxDriver((FirefoxOptions) options);
                case EDGE -> new EdgeDriver((EdgeOptions) options);
            };
        }
        driver.manage().timeouts().implicitlyWait(Duration.ZERO); // explicit waits only
        driver.manage().timeouts().pageLoadTimeout(TestConfig.pageLoadTimeout());
        if (!TestConfig.headless()) {
            driver.manage().window().setSize(new Dimension(TestConfig.windowWidth(), TestConfig.windowHeight()));
        }
        LOG.info("event=driver_started browser={} headless={} remote={}", browser, TestConfig.headless(),
                remote.isBlank() ? "-" : remote);
        return driver;
    }

    static Capabilities optionsFor(Browser browser, boolean headless) {
        String windowSize = "--window-size=" + TestConfig.windowWidth() + "," + TestConfig.windowHeight();
        return switch (browser) {
            case CHROME -> {
                ChromeOptions o = new ChromeOptions();
                if (!TestConfig.chromeBinary().isBlank()) {
                    o.setBinary(TestConfig.chromeBinary());
                }
                if (headless) {
                    o.addArguments("--headless=new");
                }
                o.addArguments(windowSize, "--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
                        "--disable-extensions", "--no-first-run", "--no-default-browser-check");
                o.setExperimentalOption("prefs", Map.of(
                        "credentials_enable_service", false,
                        "profile.password_manager_enabled", false,
                        "profile.password_manager_leak_detection", false));
                o.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
                yield o;
            }
            case FIREFOX -> {
                FirefoxOptions o = new FirefoxOptions();
                if (headless) {
                    o.addArguments("-headless");
                }
                o.addArguments("-width", String.valueOf(TestConfig.windowWidth()),
                        "-height", String.valueOf(TestConfig.windowHeight()));
                yield o;
            }
            case EDGE -> {
                EdgeOptions o = new EdgeOptions();
                if (headless) {
                    o.addArguments("--headless=new");
                }
                o.addArguments(windowSize, "--no-sandbox", "--disable-dev-shm-usage");
                yield o;
            }
        };
    }

    private static WebDriver remote(String url, Capabilities options) {
        try {
            return new RemoteWebDriver(URI.create(url).toURL(), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid remote.url: " + url, e);
        }
    }
}
