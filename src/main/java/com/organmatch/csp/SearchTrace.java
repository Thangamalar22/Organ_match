package com.organmatch.csp;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Records step-by-step search traces up to a maximum entry limit.
 */
@Getter
public class SearchTrace {
    private static final int MAX_ENTRIES = 500;
    private final List<String> entries = new ArrayList<>();

    public void add(String message) {
        if (entries.size() < MAX_ENTRIES) {
            entries.add(message);
        } else if (entries.size() == MAX_ENTRIES) {
            entries.add("... search trace limit reached (" + MAX_ENTRIES + " entries) ...");
        }
    }

    public void clear() {
        entries.clear();
    }
}
