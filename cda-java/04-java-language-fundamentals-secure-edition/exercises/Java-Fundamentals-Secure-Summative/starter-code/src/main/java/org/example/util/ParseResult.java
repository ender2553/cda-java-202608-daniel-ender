package org.example.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the outcome of parsing a batch of untrusted records: the records
 * that passed validation, plus a count and human-readable reasons for
 * every record that was rejected (fail-closed per-record, rather than
 * aborting the entire feed because of one bad entry).
 *
 * PROVIDED FOR YOU -- do not modify.
 */
public final class ParseResult<T> {

    private final List<T> accepted = new ArrayList<>();
    private final List<String> rejectionReasons = new ArrayList<>();

    public void addAccepted(T value) {
        accepted.add(value);
    }

    public void addRejected(int recordIndex, String reason) {
        rejectionReasons.add("record[" + recordIndex + "]: " + reason);
    }

    public List<T> getAccepted() {
        return Collections.unmodifiableList(accepted);
    }

    public List<String> getRejectionReasons() {
        return Collections.unmodifiableList(rejectionReasons);
    }

    public int getAcceptedCount() {
        return accepted.size();
    }

    public int getRejectedCount() {
        return rejectionReasons.size();
    }

    @Override
    public String toString() {
        return "ParseResult{accepted=" + accepted.size() + ", rejected=" + rejectionReasons.size() + "}";
    }
}
