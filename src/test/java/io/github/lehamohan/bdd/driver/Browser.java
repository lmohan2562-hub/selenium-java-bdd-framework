package io.github.lehamohan.bdd.driver;

import java.util.Arrays;
import java.util.Locale;

public enum Browser {
    CHROME, FIREFOX, EDGE;

    public static Browser from(String name) {
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported browser '" + name + "'. Use one of "
                    + Arrays.toString(values()), e);
        }
    }
}
