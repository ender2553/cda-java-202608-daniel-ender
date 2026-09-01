package org.example;
/**
 * Thrown when a finding with a CVE ID already present in the register
 * is added again.
 *
 * Unchecked (extends RuntimeException): the caller can prevent this
 * entirely by checking getUniqueCveIds() before calling addFinding(),
 * the same checked-vs-unchecked reasoning used throughout this course.
 */
public class DuplicateFindingException extends RuntimeException {

    public DuplicateFindingException(String message) {
        super(message);
    }
}
