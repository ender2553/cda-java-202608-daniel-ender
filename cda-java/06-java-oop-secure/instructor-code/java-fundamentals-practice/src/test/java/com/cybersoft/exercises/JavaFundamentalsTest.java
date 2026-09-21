package com.cybersoft.exercises;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaFundamentalsTest {
 @Test
 void exercise1AddNumbers() {
  assertEquals(10, JavaFundamentals.addNumbers(4, 6));
 }

 @Test
 void exercise2DoubleNumber() {
  assertEquals(-6, JavaFundamentals.doubleNumber(-3));
 }

 @Test
 void exercise3RectangleArea() {
  assertEquals(20, JavaFundamentals.rectangleArea(5, 4));
 }

 @Test
 void exercise4MinutesToSeconds() {
  assertEquals(300, JavaFundamentals.minutesToSeconds(5));
 }

 @Test
 void exercise5IsEven() {
  assertTrue(JavaFundamentals.isEven(8));
 }

 @Test
 void exercise6LargerNumber() {
  assertEquals(9, JavaFundamentals.larger(3, 9));
 }

 @Test
 void exercise7CanVote() {
  assertTrue(JavaFundamentals.canVote(18));
 }

 @Test
 void exercise8NumberSign() {
  assertEquals("negative", JavaFundamentals.numberSign(-1));
 }

 @Test
 void exercise9InclusiveRange() {
  assertTrue(JavaFundamentals.inRange(20));
 }

 @Test
 void exercise10LeapYear() {
  assertTrue(JavaFundamentals.isLeapYear(2000));
  assertFalse(JavaFundamentals.isLeapYear(1900));
 }

 @Test
 void exercise11SumToN() {
  assertEquals(15, JavaFundamentals.sumToN(5));
 }

 @Test
 void exercise12Factorial() {
  assertEquals(120, JavaFundamentals.factorial(5));
 }

 @Test
 void exercise13CountEvens() {
  assertEquals(5, JavaFundamentals.countEvens(10));
 }

 @Test
 void exercise14FizzBuzz() {
  assertEquals("FizzBuzz", JavaFundamentals.fizzBuzz(15));
 }

 @Test
 void exercise15CountDigits() {
  assertEquals(10, JavaFundamentals.countDigits(Integer.MIN_VALUE));
 }

 @Test
 void exercise16FirstCharacter() {
  assertEquals('J', JavaFundamentals.firstCharacter("Java"));
 }

 @Test
 void exercise17CountLetterA() {
  assertEquals(4, JavaFundamentals.countA("Java Application"));
 }

 @Test
 void exercise18ReverseString() {
  assertEquals("avaJ", JavaFundamentals.reverse("Java"));
 }

 @Test
 void exercise19Palindrome() {
  assertTrue(JavaFundamentals.isPalindrome("racecar"));
 }

 @Test
 void exercise20MaskEmail() {
  assertEquals("m****@example.com", JavaFundamentals.maskEmail("myron@example.com"));
 }

 @Test
 void exercise21ArraySum() {
  assertEquals(12, JavaFundamentals.arraySum(new int[]{2, 4, 6}));
 }

 @Test
 void exercise22FindMaximum() {
  assertEquals(6, JavaFundamentals.findMax(new int[]{2, 4, 6}));
 }

 @Test
 void exercise23CountPositives() {
  assertEquals(2, JavaFundamentals.countPositive(new int[]{1, -1, 2}));
 }

 @Test
 void exercise24ContainsNumber() {
  assertTrue(JavaFundamentals.contains(new int[]{2, 4, 6}, 4));
 }

 @Test
 void exercise25Average() {
  assertEquals(4.0, JavaFundamentals.average(new int[]{2, 4, 6}));
 }

 @Test
 void exercise26ReverseArray() {
  assertArrayEquals(new int[]{3, 2, 1}, JavaFundamentals.reverseArray(new int[]{1, 2, 3}));
 }

 @Test
 void exercise27SumList() {
  assertEquals(6, JavaFundamentals.sumList(List.of(1, 2, 3)));
 }

 @Test
 void exercise28RemoveNegatives() {
  assertEquals(List.of(4, 8, 3), JavaFundamentals.removeNegatives(List.of(4, -2, 8, -5, 3)));
 }

 @Test
 void exercise29UniqueCount() {
  assertEquals(3, JavaFundamentals.uniqueCount(List.of("r", "b", "r", "g")));
 }

 @Test
 void exercise30FindDuplicates() {
  assertEquals(
          Set.of("Java", "Python"),
          JavaFundamentals.findDuplicates(List.of("Java", "Python", "Java", "C#", "Python"))
  );
 }

 @Test
 void exercise31WordCount() {
  assertEquals(3, JavaFundamentals.wordCount(List.of("java", "sql", "java", "java")).get("java"));
 }

 @Test
 void exercise32LookupScore() {
  assertEquals(-1, JavaFundamentals.getScore(Map.of("Ana", 90), "Sam"));
 }

 @Test
 void exercise33ServeCustomer() {
  Queue<String> queue = new ArrayDeque<>(List.of("Ana", "Bo"));

  assertEquals("Ana", JavaFundamentals.serveNext(queue));
  assertEquals("Bo", JavaFundamentals.serveNext(queue));
  assertEquals("No customers", JavaFundamentals.serveNext(queue));
 }

 @Test
 void exercise34BankAccount() {
  var account = new JavaFundamentals.BankAccount("A1", new BigDecimal("100.00"));

  account.deposit(new BigDecimal("25.00"));

  assertTrue(account.withdraw(new BigDecimal("20.00")));
  assertEquals(new BigDecimal("105.00"), account.getBalance());
 }

 @Test
 void exercise35Employee() {
  var employee = new JavaFundamentals.Employee("Ana", new BigDecimal("5000"));

  assertEquals(new BigDecimal("60000"), employee.calculateAnnualSalary());
 }

 @Test
 void exercise36ShapePolymorphism() {
  JavaFundamentals.Shape rectangle = new JavaFundamentals.Rectangle(3, 4);

  assertEquals(12, rectangle.calculateArea());
  assertEquals(Math.PI * 4, new JavaFundamentals.Circle(2).calculateArea(), 0.0001);
 }

 @Test
 void exercise37TwoSum() {
  assertArrayEquals(new int[]{0, 1}, JavaFundamentals.twoSum(new int[]{2, 7, 11, 15}, 9));
 }

 @Test
 void exercise38SecondLargest() {
  assertEquals(9, JavaFundamentals.secondLargest(new int[]{8, 3, 12, 5, 9}));
 }

 @Test
 void exercise39PasswordValidator() {
  assertTrue(JavaFundamentals.isValidPassword("SecurePass1!"));
 }

 @Test
 void exercise40FailedLoginDetection() {
  assertTrue(JavaFundamentals.suspiciousLoginActivity(
          List.of("SUCCESS", "FAILED", "FAILED", "FAILED", "SUCCESS")
  ));
 }
}
