package io.github.lehamohan.bdd.driver;

import org.openqa.selenium.WebDriver;

/**
 * Scenario-scoped driver holder. PicoContainer creates one instance per scenario, so parallel
 * scenarios each get their own browser with no ThreadLocal bookkeeping.
 */
public class DriverManager {

    private WebDriver driver;

    /** Lazily starts the browser on first use, so scenarios that never touch the UI stay fast. */
    public WebDriver driver() {
        if (driver == null) {
            driver = DriverFactory.create();
        }
        return driver;
    }

    public boolean isStarted() {
        return driver != null;
    }

    public void quit() {
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driver = null;
            }
        }
    }
}
