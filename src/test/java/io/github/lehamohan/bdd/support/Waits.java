package io.github.lehamohan.bdd.support;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.function.Function;

/**
 * Explicit-wait wrapper. Pages never call {@code Thread.sleep} or rely on implicit waits; every
 * synchronisation point is named here, which keeps timeouts consistent and failure messages
 * descriptive ("waiting for: provider options to load").
 */
public final class Waits {

    private static final Duration POLL = Duration.ofMillis(100);

    private final WebDriver driver;
    private final Duration timeout;

    public Waits(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.timeout = timeout;
    }

    public <T> T until(String description, Function<WebDriver, T> condition) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(POLL)
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .withMessage("waiting for: " + description);
        return wait.until(condition);
    }

    public WebElement visible(By locator) {
        return until(locator + " to be visible", ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return until(locator + " to be clickable", ExpectedConditions.elementToBeClickable(locator));
    }

    public void invisible(By locator) {
        until(locator + " to be hidden", ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public void textNotBlank(By locator) {
        until(locator + " to have text", d -> !d.findElement(locator).getText().isBlank());
    }

    public void urlContains(String fragment) {
        until("URL to contain '" + fragment + "'", ExpectedConditions.urlContains(fragment));
    }

    /** Waits until a {@code <select>} is enabled and has more than the placeholder option. */
    public void optionsLoaded(By select, int minRealOptions) {
        until("options to load in " + select, d -> {
            WebElement el = d.findElement(select);
            return el.isEnabled() && el.findElements(By.tagName("option")).size() > minRealOptions;
        });
    }
}
