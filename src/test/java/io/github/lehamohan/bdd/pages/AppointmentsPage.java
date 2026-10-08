package io.github.lehamohan.bdd.pages;

import io.github.lehamohan.bdd.support.BookingDetails;
import io.github.lehamohan.bdd.support.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dashboard: booking form plus the upcoming-appointments table. */
public class AppointmentsPage extends BasePage {

    public static final List<String> FORM_FIELDS =
            List.of("department", "provider", "date", "time", "phone", "reason", "consent");
    private static final List<String> COLUMNS =
            List.of("Confirmation", "Department", "Provider", "Date", "Time", "Visit type", "Status");

    private static final By WELCOME = testId("welcome");
    private static final By LOGOUT = testId("logout");
    private static final By DEPARTMENT = testId("department");
    private static final By PROVIDER = testId("provider");
    private static final By DATE = testId("date");
    private static final By TIME = testId("time");
    private static final By IN_PERSON = testId("visit-in-person");
    private static final By TELEHEALTH = testId("visit-telehealth");
    private static final By PHONE = testId("phone");
    private static final By REASON = testId("reason");
    private static final By CONSENT = testId("consent");
    private static final By BOOK = testId("book");
    private static final By SPINNER = testId("booking-spinner");
    private static final By CONFIRMATION = testId("confirmation");
    private static final By CONFIRMATION_NUMBER = testId("confirmation-number");
    private static final By TABLE_ROWS = By.cssSelector("[data-testid='appointments-table'] tbody tr");

    public AppointmentsPage(WebDriver driver, Waits waits) {
        super(driver, waits);
    }

    public AppointmentsPage waitUntilLoaded() {
        waits.urlContains("appointments.html");
        waits.textNotBlank(WELCOME);
        return this;
    }

    public String welcomeMessage() {
        return text(WELCOME);
    }

    public void signOut() {
        click(LOGOUT);
    }

    public void selectDepartment(String department) {
        selectByText(DEPARTMENT, department);
        if (department != null && !department.isEmpty()) {
            waits.optionsLoaded(PROVIDER, 1);
        }
    }

    public List<String> providerOptions() {
        List<String> names = new ArrayList<>();
        for (WebElement option : new Select(driver.findElement(PROVIDER)).getOptions()) {
            String value = option.getDomAttribute("value");
            if (value != null && !value.isEmpty()) {
                names.add(option.getText().trim());
            }
        }
        return names;
    }

    public void fill(BookingDetails details) {
        selectDepartment(details.department());
        if (details.department() != null && !details.department().isEmpty()) {
            selectByText(PROVIDER, details.provider());
        }
        type(DATE, details.resolvedDate());
        selectByText(TIME, details.time());
        click("Telehealth".equalsIgnoreCase(details.visitType()) ? TELEHEALTH : IN_PERSON);
        type(PHONE, details.phone());
        type(REASON, details.reason());
        setConsent(details.consent());
    }

    public void submit() {
        click(BOOK);
    }

    /** Waits through the async booking (spinner) and returns the confirmation number. */
    public String waitForConfirmation() {
        waits.visible(CONFIRMATION);
        waits.invisible(SPINNER);
        waits.textNotBlank(CONFIRMATION_NUMBER);
        return text(CONFIRMATION_NUMBER);
    }

    public String fieldError(String field) {
        By locator = testId("error-" + field);
        waits.textNotBlank(locator);
        return text(locator);
    }

    /** All non-empty field errors currently shown, keyed by field name. */
    public Map<String, String> visibleFieldErrors() {
        waits.until("at least one field error", d -> FORM_FIELDS.stream()
                .anyMatch(f -> !d.findElement(testId("error-" + f)).getText().isBlank()));
        Map<String, String> errors = new LinkedHashMap<>();
        for (String field : FORM_FIELDS) {
            String message = text(testId("error-" + field));
            if (!message.isEmpty()) {
                errors.put(field, message);
            }
        }
        return errors;
    }

    public boolean confirmationShown() {
        return isDisplayed(CONFIRMATION);
    }

    /** Table rows as column-name to cell-text maps (action column excluded). */
    public List<Map<String, String>> appointments() {
        List<Map<String, String>> rows = new ArrayList<>();
        for (WebElement tr : driver.findElements(TABLE_ROWS)) {
            List<WebElement> cells = tr.findElements(By.tagName("td"));
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < COLUMNS.size(); i++) {
                row.put(COLUMNS.get(i), cells.get(i).getText().trim());
            }
            rows.add(row);
        }
        return rows;
    }

    public Map<String, String> appointment(String confirmationNumber) {
        return appointments().stream()
                .filter(r -> confirmationNumber.equals(r.get("Confirmation")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No appointment row for " + confirmationNumber));
    }

    public void cancel(String confirmationNumber) {
        click(testId("cancel-" + confirmationNumber));
        waits.until("status of " + confirmationNumber + " to be Cancelled",
                d -> "Cancelled".equals(appointment(confirmationNumber).get("Status")));
    }

    public boolean canCancel(String confirmationNumber) {
        return !driver.findElements(testId("cancel-" + confirmationNumber)).isEmpty();
    }

    private void setConsent(boolean wanted) {
        WebElement box = waits.visible(CONSENT);
        if (box.isSelected() != wanted) {
            box.click();
        }
    }
}
