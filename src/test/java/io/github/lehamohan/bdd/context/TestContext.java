package io.github.lehamohan.bdd.context;

import io.github.lehamohan.bdd.config.TestConfig;
import io.github.lehamohan.bdd.driver.DriverManager;
import io.github.lehamohan.bdd.pages.AppointmentsPage;
import io.github.lehamohan.bdd.pages.LoginPage;
import io.github.lehamohan.bdd.server.AppUnderTest;
import io.github.lehamohan.bdd.support.Waits;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * Scenario-scoped state shared between step definition classes via PicoContainer constructor
 * injection. A fresh instance (and browser) is created for every scenario.
 */
public class TestContext {

    private final DriverManager driverManager;
    private final Map<String, Object> scenarioData = new HashMap<>();
    private Waits waits;
    private LoginPage loginPage;
    private AppointmentsPage appointmentsPage;

    public TestContext(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    public WebDriver driver() {
        return driverManager.driver();
    }

    public DriverManager driverManager() {
        return driverManager;
    }

    public String baseUrl() {
        return AppUnderTest.baseUrl();
    }

    public Waits waits() {
        if (waits == null) {
            waits = new Waits(driver(), TestConfig.explicitWait());
        }
        return waits;
    }

    public LoginPage loginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(driver(), waits());
        }
        return loginPage;
    }

    public AppointmentsPage appointmentsPage() {
        if (appointmentsPage == null) {
            appointmentsPage = new AppointmentsPage(driver(), waits());
        }
        return appointmentsPage;
    }

    public void remember(String key, Object value) {
        scenarioData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T recall(String key) {
        Object value = scenarioData.get(key);
        if (value == null) {
            throw new IllegalStateException("Nothing remembered under '" + key + "' in this scenario");
        }
        return (T) value;
    }
}
