package io.github.lehamohan.bdd.pages;

import io.github.lehamohan.bdd.support.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By USERNAME = testId("username");
    private static final By PASSWORD = testId("password");
    private static final By SIGN_IN = testId("sign-in");
    private static final By BANNER = testId("login-error");

    public LoginPage(WebDriver driver, Waits waits) {
        super(driver, waits);
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "index.html");
        waits.visible(USERNAME);
        return this;
    }

    public void signIn(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(SIGN_IN);
    }

    public boolean isLoaded() {
        return isDisplayed(SIGN_IN);
    }

    /** Waits for the asynchronous sign-in check to finish and the banner to appear. */
    public String bannerMessage() {
        waits.visible(BANNER);
        return text(BANNER);
    }

    public String fieldError(String field) {
        By locator = testId("error-" + field);
        waits.textNotBlank(locator);
        return text(locator);
    }
}
