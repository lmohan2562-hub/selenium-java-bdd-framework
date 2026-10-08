package io.github.lehamohan.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.lehamohan.bdd.context.TestContext;
import io.github.lehamohan.bdd.pages.AppointmentsPage;
import io.github.lehamohan.bdd.support.BookingDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingSteps {

    static final String LAST_CONFIRMATION = "lastConfirmation";
    private static final String LAST_BOOKING = "lastBooking";

    private final TestContext context;

    public BookingSteps(TestContext context) {
        this.context = context;
    }

    private AppointmentsPage page() {
        return context.appointmentsPage();
    }

    @When("I book an appointment with:")
    public void bookAppointmentWith(Map<String, String> fields) {
        book(BookingDetails.fromTable(fields));
    }

    @Given("I have booked a valid appointment")
    public void haveBookedValidAppointment() {
        book(BookingDetails.valid());
        bookingConfirmed();
    }

    @When("I book the same provider, date and time again")
    public void bookSameSlotAgain() {
        BookingDetails previous = context.recall(LAST_BOOKING);
        page().fill(previous.with("reason", "Second request for the same slot"));
        page().submit();
    }

    @When("I submit a valid booking but with {string} set to {string}")
    public void submitValidBookingWithOverride(String field, String value) {
        page().fill(BookingDetails.valid().with(field, value));
        page().submit();
    }

    @When("I submit the booking form without entering anything")
    public void submitEmptyForm() {
        page().submit();
    }

    @When("I select the {string} department")
    public void selectDepartment(String department) {
        page().selectDepartment(department);
    }

    @Then("the booking should be confirmed with a confirmation number")
    public void bookingConfirmed() {
        String number = page().waitForConfirmation();
        assertThat(number).matches("APT-\\d{6}");
        context.remember(LAST_CONFIRMATION, number);
    }

    @Then("my new appointment should be listed as:")
    public void newAppointmentListedAs(Map<String, String> expected) {
        String number = context.recall(LAST_CONFIRMATION);
        Map<String, String> row = page().appointment(number);
        assertThat(row).containsAllEntriesOf(expected);
        BookingDetails booked = context.recall(LAST_BOOKING);
        assertThat(row.get("Date")).isEqualTo(booked.resolvedDate());
    }

    @Then("the {string} booking error should read {string}")
    public void bookingErrorShouldRead(String field, String expected) {
        assertThat(page().fieldError(field)).isEqualTo(expected);
    }

    @Then("no booking confirmation should be shown")
    public void noConfirmation() {
        assertThat(page().confirmationShown()).isFalse();
    }

    @Then("the following booking errors should be shown:")
    public void followingErrorsShown(List<Map<String, String>> expected) {
        Map<String, String> actual = page().visibleFieldErrors();
        assertThat(actual).hasSize(expected.size());
        for (Map<String, String> row : expected) {
            assertThat(actual).containsEntry(row.get("field"), row.get("message"));
        }
    }

    @Then("the available providers should be {string}")
    public void availableProvidersShouldBe(String commaSeparated) {
        List<String> expected = Arrays.stream(commaSeparated.split(",")).map(String::trim).toList();
        assertThat(page().providerOptions()).containsExactlyElementsOf(expected);
    }

    private void book(BookingDetails details) {
        page().fill(details);
        page().submit();
        context.remember(LAST_BOOKING, details);
    }
}
