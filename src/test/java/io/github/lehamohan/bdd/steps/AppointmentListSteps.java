package io.github.lehamohan.bdd.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.lehamohan.bdd.context.TestContext;
import io.github.lehamohan.bdd.pages.AppointmentsPage;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AppointmentListSteps {

    private final TestContext context;

    public AppointmentListSteps(TestContext context) {
        this.context = context;
    }

    private AppointmentsPage page() {
        return context.appointmentsPage();
    }

    @Then("the upcoming appointments should include:")
    public void upcomingAppointmentsInclude(List<Map<String, String>> expectedRows) {
        for (Map<String, String> expected : expectedRows) {
            Map<String, String> actual = page().appointment(expected.get("Confirmation"));
            assertThat(actual).containsAllEntriesOf(expected);
        }
    }

    @When("I cancel appointment {string}")
    public void cancelAppointment(String confirmation) {
        page().cancel(confirmation);
    }

    @When("I cancel the appointment I just booked")
    public void cancelJustBooked() {
        page().cancel(context.<String>recall(BookingSteps.LAST_CONFIRMATION));
    }

    @Then("appointment {string} should have status {string}")
    public void appointmentShouldHaveStatus(String confirmation, String status) {
        assertThat(page().appointment(confirmation)).containsEntry("Status", status);
    }

    @Then("the appointment I just booked should have status {string}")
    public void justBookedShouldHaveStatus(String status) {
        appointmentShouldHaveStatus(context.recall(BookingSteps.LAST_CONFIRMATION), status);
    }

    @Then("appointment {string} can no longer be cancelled")
    public void cannotBeCancelled(String confirmation) {
        assertThat(page().canCancel(confirmation)).isFalse();
    }
}
