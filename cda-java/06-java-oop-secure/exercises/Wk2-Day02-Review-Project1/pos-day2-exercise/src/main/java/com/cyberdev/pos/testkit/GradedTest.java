package com.cyberdev.pos.testkit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test method as a graded test tied to a specific TODO tag.
 *
 * tag    - the TODO tag this test verifies, e.g. "POS1-1"
 * points - the automated point value of this test out of the day's 80 automated points
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GradedTest {
    String tag();
    int points() default 1;
    String description() default "";
}
