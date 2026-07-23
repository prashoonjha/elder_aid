package com.elderaid.platform.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class TextNormaliser {

    private TextNormaliser() {
    }

    // "HELSINKI", "helsinki" and " Helsinki " should all be one city. Title-case
    // each word so it still reads correctly in the UI - lower-casing everything
    // would be consistent but look wrong.
    public static String city(String city) {
        if (city == null || city.isBlank()) {
            return city;
        }
        return Arrays.stream(city.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT)
                        + word.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }
}
