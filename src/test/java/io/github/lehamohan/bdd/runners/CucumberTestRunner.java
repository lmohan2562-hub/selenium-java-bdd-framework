package io.github.lehamohan.bdd.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * TestNG entry point. Each scenario is a DataProvider row; {@code parallel = true} plus
 * Surefire's {@code dataproviderthreadcount} (Maven property {@code threads}) controls concurrency.
 *
 * <p>Filter at runtime with {@code -Dcucumber.filter.tags="@smoke"}.
 */
@CucumberOptions(
        features = "classpath:features",
        glue = {"io.github.lehamohan.bdd.steps", "io.github.lehamohan.bdd.hooks"},
        tags = "not @wip",
        plugin = {
                "pretty",
                "summary",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "junit:target/cucumber-reports/cucumber-junit.xml",
                "rerun:target/cucumber-reports/rerun.txt"
        },
        monochrome = true)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
