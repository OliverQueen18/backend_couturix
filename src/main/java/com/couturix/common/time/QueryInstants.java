package com.couturix.common.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class QueryInstants {

    private static final ZoneId ZONE = ZoneId.of("Africa/Brazzaville");

    private QueryInstants() {}

    public record Range(Instant start, Instant endExclusive) {
        public Instant endInclusive() {
            return endExclusive.minusMillis(1);
        }
    }

    public static Range parse(String from, String to) {
        Instant start = from == null || from.isBlank()
                ? LocalDate.now(ZONE).minusDays(30).atStartOfDay(ZONE).toInstant()
                : startOf(from);
        Instant endExclusive = to == null || to.isBlank()
                ? LocalDate.now(ZONE).plusDays(1).atStartOfDay(ZONE).toInstant()
                : endExclusiveOf(to);
        if (!endExclusive.isAfter(start)) {
            endExclusive = start.plusSeconds(86_400);
        }
        return new Range(start, endExclusive);
    }

    private static Instant startOf(String raw) {
        String value = raw.trim();
        if (value.length() <= 10) {
            return LocalDate.parse(value).atStartOfDay(ZONE).toInstant();
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            return LocalDate.parse(value.substring(0, 10)).atStartOfDay(ZONE).toInstant();
        }
    }

    private static Instant endExclusiveOf(String raw) {
        String value = raw.trim();
        if (value.length() <= 10) {
            return LocalDate.parse(value).plusDays(1).atStartOfDay(ZONE).toInstant();
        }
        try {
            Instant instant = Instant.parse(value);
            return instant;
        } catch (DateTimeParseException ex) {
            return LocalDate.parse(value.substring(0, 10)).plusDays(1).atStartOfDay(ZONE).toInstant();
        }
    }
}
