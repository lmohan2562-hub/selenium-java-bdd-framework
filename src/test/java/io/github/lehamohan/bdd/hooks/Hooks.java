package io.github.lehamohan.bdd.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.github.lehamohan.bdd.context.TestContext;
import io.github.lehamohan.bdd.server.AppUnderTest;
import io.github.lehamohan.bdd.support.Screenshots;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {

    private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    @BeforeAll
    public static void startApplication() {
        AppUnderTest.start();
    }

    @AfterAll
    public static void stopApplication() {
        AppUnderTest.stop();
    }

    /** Runs before the driver is quit (higher order runs first for @After). */
    @After(order = 100)
    public void captureEvidenceOnFailure(Scenario scenario) {
        if (scenario.isFailed() && context.driverManager().isStarted()) {
            Screenshots.capture(context.driver(), scenario.getName())
                    .ifPresent(png -> scenario.attach(png, "image/png", "failure-screenshot"));
            scenario.log("URL at failure: " + context.driver().getCurrentUrl());
        }
    }

    @After(order = 0)
    public void quitBrowser(Scenario scenario) {
        context.driverManager().quit();
        LOG.info("event=scenario_finished status={} name=\"{}\"", scenario.getStatus(), scenario.getName());
    }
}
