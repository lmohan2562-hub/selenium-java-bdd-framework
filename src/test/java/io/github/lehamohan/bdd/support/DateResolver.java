package io.github.lehamohan.bdd.support;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Turns readable date tokens used in feature files into concrete ISO dates relative to today,
 * so scenarios never go stale. Unknown values are passed through unchanged (e.g. "2026/01/01"
 * for format-validation examples).
 */
public final class DateResolver {

    private DateResolver() {
    }

    public static String resolve(String token) {
        return resolve(token, LocalDate.now());
    }

    static String resolve(String token, LocalDate today) {
        return switch (token.trim().toUpperCase()) {
            case "NEXT_WEEKDAY" -> nextWeekday(today.plusDays(1)).toString();
            case "WEEKDAY_IN_2_WEEKS" -> nextWeekday(today.plusDays(14)).toString();
            case "TODAY" -> today.toString();
            case "YESTERDAY" -> today.minusDays(1).toString();
            case "NEXT_SATURDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).toString();
            case "BEYOND_90_DAYS" -> nextWeekday(today.plusDays(91)).toString();
            default -> token;
        };
    }

    private static LocalDate nextWeekday(LocalDate from) {
        LocalDate d = from;
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d;
    }
}
