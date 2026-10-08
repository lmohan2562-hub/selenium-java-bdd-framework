package io.github.lehamohan.bdd.support;

import java.util.Locale;
import java.util.Map;

/**
 * Appointment form input. {@link #valid()} returns a known-good booking; validation scenarios
 * override exactly one field with {@link #with(String, String)} so each example isolates one rule.
 */
public record BookingDetails(
        String department,
        String provider,
        String date,
        String time,
        String visitType,
        String phone,
        String reason,
        boolean consent) {

    public static BookingDetails valid() {
        return new BookingDetails("Family Medicine", "Dr. Grace Liu", "NEXT_WEEKDAY", "10:15",
                "In person", "555-010-1234", "Annual wellness check-up", true);
    }

    /** Builds from a two-column (field | value) Gherkin table, starting from valid defaults. */
    public static BookingDetails fromTable(Map<String, String> table) {
        BookingDetails details = valid();
        for (Map.Entry<String, String> e : table.entrySet()) {
            details = details.with(e.getKey(), e.getValue());
        }
        return details;
    }

    public BookingDetails with(String field, String value) {
        String v = value == null ? "" : value;
        return switch (field.trim().toLowerCase(Locale.ROOT)) {
            case "department" -> new BookingDetails(v, provider, date, time, visitType, phone, reason, consent);
            case "provider" -> new BookingDetails(department, v, date, time, visitType, phone, reason, consent);
            case "date" -> new BookingDetails(department, provider, v, time, visitType, phone, reason, consent);
            case "time" -> new BookingDetails(department, provider, date, v, visitType, phone, reason, consent);
            case "visit type" -> new BookingDetails(department, provider, date, time, v, phone, reason, consent);
            case "phone" -> new BookingDetails(department, provider, date, time, visitType, v, reason, consent);
            case "reason" -> new BookingDetails(department, provider, date, time, visitType, phone, v, consent);
            case "consent" -> new BookingDetails(department, provider, date, time, visitType, phone, reason,
                    Boolean.parseBoolean(v) || "checked".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v));
            default -> throw new IllegalArgumentException("Unknown booking field: " + field);
        };
    }

    public String resolvedDate() {
        return DateResolver.resolve(date);
    }
}
