package com.cyberdev.secsuite.testkit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Marks a test method as a graded test tied to a specific TODO tag (copied from the QuickPay
 * POS series' testkit.GradedTest).
 *
 * tag    - the TODO tag this test verifies, e.g. "SEC-10"
 * points - the automated point value of this test out of the capstone's 80 automated points
 *          (the remaining 20 are the manual code-quality review -- see REQUIREMENTS.md)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GradedTest {
    String tag();
    int points() default 1;
    String description() default "";
}
