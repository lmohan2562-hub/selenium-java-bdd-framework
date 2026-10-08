package io.github.lehamohan.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.lehamohan.bdd.context.TestContext;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    /** Synthetic demo account; documented in the README. Not a real credential. */
    static final String DEMO_USER = "patient.demo";
    static final String DEMO_SECRET = "Demo!2026"; // pragma: allowlist secret (synthetic demo account)

    private final TestContext context;

    public LoginSteps(TestContext context) {
        this.context = context;
    }

    @Given("the patient portal sign-in page is open")
    public void signInPageIsOpen() {
        context.loginPage().open(context.baseUrl());
    }

    @Given("I am signed in as a patient")
    public void signedInAsPatient() {
        context.loginPage().open(context.baseUrl()).signIn(DEMO_USER, DEMO_SECRET);
        context.appointmentsPage().waitUntilLoaded();
    }

    @When("I sign in as {string} with password {string}")
    public void signInAs(String username, String password) {
        context.loginPage().signIn(username, password);
    }

    @Then("I should see the appointments dashboard")
    public void shouldSeeDashboard() {
        context.appointmentsPage().waitUntilLoaded();
        assertThat(context.appointmentsPage().currentUrl()).contains("appointments.html");
    }

    @Then("the welcome message should read {string}")
    public void welcomeMessageShouldRead(String expected) {
        assertThat(context.appointmentsPage().welcomeMessage()).isEqualTo(expected);
    }

    @Then("I should see the sign-in error {string}")
    public void shouldSeeSignInError(String expected) {
        assertThat(context.loginPage().bannerMessage()).isEqualTo(expected);
    }

    @Then("I should remain on the sign-in page")
    public void shouldRemainOnSignInPage() {
        assertThat(context.loginPage().isLoaded()).as("sign-in button visible").isTrue();
        assertThat(context.loginPage().currentUrl()).contains("index.html");
    }

    @Then("the {string} field error should read {string}")
    public void fieldErrorShouldRead(String field, String expected) {
        assertThat(context.loginPage().fieldError(field)).isEqualTo(expected);
    }

    @When("I sign out")
    public void signOut() {
        context.appointmentsPage().signOut();
    }

    @When("I open the appointments page without signing in")
    public void openAppointmentsAnonymously() {
        context.driver().get(context.baseUrl() + "appointments.html");
        context.waits().urlContains("index.html");
    }
}
