package com.organmatch.csp;

import com.organmatch.model.Recipient;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Represents the mutable domain of candidate Recipient values for a CSP variable.
 * Supports state push/pop history for backtracking and forward checking.
 */
public class Domain {
    private final List<Recipient> values;
    private final Stack<List<Recipient>> history = new Stack<>();

    public Domain(List<Recipient> initialValues) {
        this.values = new ArrayList<>(initialValues != null ? initialValues : List.of());
    }

    public List<Recipient> getValues() {
        return new ArrayList<>(values);
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean remove(Recipient recipient) {
        return values.remove(recipient);
    }

    public void add(Recipient recipient) {
        if (!values.contains(recipient)) {
            values.add(recipient);
        }
    }

    /**
     * Pushes current domain state to history stack before pruning.
     */
    public void pushState() {
        history.push(new ArrayList<>(values));
    }

    /**
     * Pops domain state from history stack during backtracking.
     */
    public void popState() {
        if (!history.isEmpty()) {
            values.clear();
            values.addAll(history.pop());
        }
    }
}
