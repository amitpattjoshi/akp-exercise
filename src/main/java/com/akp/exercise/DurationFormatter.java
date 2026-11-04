package com.akp.exercise;

import java.util.ArrayList;
import java.util.List;

public class DurationFormatter {

    private static final String COMMA_OPERATOR = ", ";
    private static final String LAST_SEPARATOR = " and ";
    private static final int SECONDS_IN_HOUR = 60 * 60;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final String NOW = "now";
    private static final String HOUR_UNIT = "hour";
    private static final String MINUTE_UNIT = "minute";
    private static final String SECOND_UNIT = "second";

    public String format(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds must not be negative but was " + seconds);
        }
        if (seconds == 0) {
            return NOW;
        }
        final List<String> parts = new ArrayList<>();

        int hours = seconds / SECONDS_IN_HOUR;
        int minutes = seconds % SECONDS_IN_HOUR / SECONDS_IN_MINUTE;
        int remainingSeconds = seconds % SECONDS_IN_MINUTE;

        if (hours > 0) {
            parts.add(formatUnit(hours, HOUR_UNIT));
        }
        if (minutes > 0) {
            parts.add(formatUnit(minutes, MINUTE_UNIT));
        }
        if (remainingSeconds > 0) {
            parts.add(formatUnit(remainingSeconds, SECOND_UNIT));
        }

        return join(parts);

    }

    private String join(final List<String> parts) {
        if (parts.size() == 1) {
            return parts.get(0);
        }
        String allButLast = String.join(COMMA_OPERATOR, parts.subList(0, parts.size() - 1));
        String last = LAST_SEPARATOR + parts.get(parts.size() - 1);
        return allButLast + last;
    }

    private String formatUnit(int value, String unit) {
        return value + " " + unit + (value == 1 ? "" : "s");
    }
}