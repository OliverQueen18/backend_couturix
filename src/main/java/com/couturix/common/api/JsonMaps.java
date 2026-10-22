package com.couturix.common.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public final class JsonMaps {

    private JsonMaps() {}

    public static String str(Map<String, ?> body, String... keys) {
        if (body == null) {
            return null;
        }
        for (String key : keys) {
            Object value = body.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }

    public static Long lng(Map<String, ?> body, String... keys) {
        String raw = str(body, keys);
        return raw == null ? null : Long.valueOf(raw);
    }

    public static BigDecimal decimal(Map<String, ?> body, String... keys) {
        String raw = str(body, keys);
        return raw == null ? null : new BigDecimal(raw);
    }

    public static LocalDate date(Map<String, ?> body, String... keys) {
        String raw = str(body, keys);
        return raw == null ? null : LocalDate.parse(raw);
    }
}
