package io.github.lehamohan.bdd.pages;

import io.github.lehamohan.bdd.support.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/** Shared page helpers. Locators use {@code data-testid} attributes, which survive styling changes. */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final Waits waits;

    protected BasePage(WebDriver driver, Waits waits) {
        this.driver = driver;
        this.waits = waits;
    }

    protected static By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    protected void type(By locator, String text) {
        WebElement el = waits.visible(locator);
        el.clear();
        if (text != null && !text.isEmpty()) {
            el.sendKeys(text);
        }
    }

    protected void click(By locator) {
        waits.clickable(locator).click();
    }

    protected void selectByText(By locator, String visibleText) {
        Select select = new Select(waits.visible(locator));
        if (visibleText == null || visibleText.isEmpty()) {
            select.selectByIndex(0);
        } else {
            select.selectByVisibleText(visibleText);
        }
    }

    protected String text(By locator) {
        return driver.findElement(locator).getText().trim();
    }

    protected boolean isDisplayed(By locator) {
        return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}
