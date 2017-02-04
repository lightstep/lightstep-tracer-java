package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.KeyValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal class used primarily for unit testing and debugging. This is not
 * part of the OpenTracing API and is not a supported API.
 */
public class Status {
    private final Map<String, String> tags;
    private final ClientMetrics clientMetrics;

    Status(List<KeyValue> attrs, ClientMetrics clientMetrics) {
        tags = new HashMap<>(attrs.size());
        for (KeyValue pair : attrs) {
            tags.put(pair.getKey(), pair.getStringValue());
        }

        this.clientMetrics = clientMetrics;
    }

    public long getSpansDropped() {
        return clientMetrics.spansDropped;
    }

    public boolean hasTag(String key) {
        return tags.containsKey(key);
    }

    public String getTag(String key) {
        return tags.get(key);
    }
}
