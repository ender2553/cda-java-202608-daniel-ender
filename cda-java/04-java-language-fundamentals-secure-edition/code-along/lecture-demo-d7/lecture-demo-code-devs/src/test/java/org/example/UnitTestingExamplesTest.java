package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 *
 * 1. Run all tests with "mvn test" or the green arrow beside this class.
 * 2. Read each test using Arrange, Act, Assert.
 * 3. Temporarily change an expected value to make a test fail.
 * 4. Read the failure message: expected value, actual value, and failing line.
 * 5. Restore the expected value and rerun the test.
 *
 * A unit test should be small, repeatable, and independent.
 * One test should not depend on another test running first.
 */
class UnitTestingExamplesTest {

    @Test
    @DisplayName("add returns the sum of two positive numbers")
    void add_twoPositiveNumbers_returnsSum() {
        // ARRANGE: Set up the input and the result we expect.
        int firstNumber = 2;
        int secondNumber = 3;
        int expected = 5;

        // ACT: Call the one method (unit) we want to test.
        int actual = UnitTestingExamples.add(firstNumber, secondNumber);

        // ASSERT: Compare the expected result with the actual result.
        // CALLOUT: The optional message appears only when this assertion fails.
        assertEquals(expected, actual, "2 + 3 should equal 5");
    }

    @Test
    @DisplayName("add also works with negative numbers")
    void add_negativeNumber_returnsCorrectSum() {
        // This second example shows that one method often needs multiple test cases.
        assertEquals(-2, UnitTestingExamples.add(-5, 3));
    }

    @Test
    @DisplayName("isEven identifies even and odd numbers")
    void isEven_evenAndOddNumbers_returnsExpectedBooleans() {
        // assertTrue is best when the result should be true.
        assertTrue(UnitTestingExamples.isEven(8), "8 should be even");

        // assertFalse is best when the result should be false.
        assertFalse(UnitTestingExamples.isEven(7), "7 should be odd");
    }

    @Test
    @DisplayName("greet builds a personalized greeting")
    void greet_validName_returnsGreeting() {
        assertEquals("Hello, Ada!", UnitTestingExamples.greet("Ada"));
    }

    @Test
    @DisplayName("greet rejects a blank name")
    void greet_blankName_throwsException() {
        // Testing an exception proves that invalid input is handled.
        // Pass the method call as a lambda so JUnit can run and observe it.
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UnitTestingExamples.greet(" ")
        );

        // We can also verify that the exception explains the problem clearly.
        assertEquals("Name must not be blank", exception.getMessage());
    }

    @ParameterizedTest(name = "grade {0} should have passing result: {1}")
    @CsvSource({
            "0, false",
            "59, false",
            "60, true",
            "100, true"
    })
    @DisplayName("isPassingGrade handles boundary values")
    void isPassingGrade_boundaryValues_returnsExpectedResult(int grade, boolean expected) {
        // PARAMETERIZED-TEST:
        // JUnit runs this method once per @CsvSource row. This avoids copying the
        // same Arrange/Act/Assert structure for several related inputs.
        assertEquals(expected, UnitTestingExamples.isPassingGrade(grade));
    }

    @Test
    @DisplayName("isPassingGrade rejects grades outside 0 through 100")
    void isPassingGrade_gradeAbove100_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> UnitTestingExamples.isPassingGrade(101)
        );
    }
}
