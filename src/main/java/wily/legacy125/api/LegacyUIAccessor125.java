package wily.legacy125.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small 1.2.5 counterpart to FactoryAPI's modern UIAccessor.
 *
 * <p>The backport deliberately keeps upstream UI-definition keys at call
 * sites.  Version-specific definitions can therefore change measurements
 * without rewriting screen composition logic.</p>
 */
public final class LegacyUIAccessor125 {
    private final Map<String, Integer> integers = new LinkedHashMap<String, Integer>();

    public LegacyUIAccessor125 putInteger(String name, int value) {
        if (name == null) throw new IllegalArgumentException("name");
        integers.put(name, Integer.valueOf(value));
        return this;
    }

    public int getInteger(String name) {
        Integer value = integers.get(name);
        if (value == null) throw new IllegalArgumentException("Undefined UI value: " + name);
        return value.intValue();
    }

    public int getInteger(String name, int fallback) {
        Integer value = integers.get(name);
        return value == null ? fallback : value.intValue();
    }

    public Map<String, Integer> integers() {
        return Collections.unmodifiableMap(integers);
    }
}
