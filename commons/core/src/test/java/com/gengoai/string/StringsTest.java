/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.string;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringsTest {

   @Test
   public void testCase() throws Exception {
      assertEquals("Title Case", Strings.toTitleCase("title case"));
      assertEquals("Title Case", Strings.toTitleCase("TITLE CASE"));
      assertEquals(" ", Strings.toTitleCase(" "));
      assertNull(Strings.toTitleCase(null));

      assertEquals("TITLE CASE", StringFunctions.UPPER_CASE.apply("title case"));
      assertEquals("TITLE CASE", StringFunctions.UPPER_CASE.apply("TITLE CASE"));
      assertEquals(" ", StringFunctions.UPPER_CASE.apply(" "));
      assertNull(StringFunctions.UPPER_CASE.apply(null));

      assertEquals("title case", StringFunctions.LOWER_CASE.apply("title case"));
      assertEquals("title case", StringFunctions.LOWER_CASE.apply("TITLE CASE"));
      assertEquals(" ", StringFunctions.LOWER_CASE.apply(" "));
      assertNull(StringFunctions.LOWER_CASE.apply(null));
   }

   @Test
   public void testAbbreviate() throws Exception {
      assertEquals("abc...", Strings.abbreviate("abcdefgh", 3));
      assertEquals("abc", Strings.abbreviate("abc", 3));
      assertEquals("", Strings.abbreviate("", 3));
      assertNull(Strings.abbreviate(null, 3));
   }


   @Test
   public void testNormalization() throws Exception {
      assertEquals("democrocia", Strings.removeDiacritics("democrocìa"));
      assertEquals("123", Strings.toCanonicalForm("１２３"));
   }

   @Test
   public void testIsMethods() throws Exception {
      assertTrue(Strings.isLowerCase("lower"));
      assertFalse(Strings.isLowerCase("あえおいう"));
      assertFalse(Strings.isLowerCase("UPPER"));
      assertFalse(Strings.isLowerCase(""));
      assertFalse(Strings.isLowerCase(null));

      assertFalse(Strings.isUpperCase("lower"));
      assertFalse(Strings.isUpperCase("あえおいう"));
      assertTrue(Strings.isUpperCase("UPPER"));
      assertFalse(Strings.isUpperCase(""));
      assertFalse(Strings.isUpperCase(null));

      assertTrue(Strings.isAlphaNumeric("lower"));
      assertTrue(Strings.isAlphaNumeric("UPPER"));
      assertTrue(Strings.isAlphaNumeric("lower123"));
      assertTrue(Strings.isAlphaNumeric("UP589PER"));
      assertTrue(Strings.isAlphaNumeric("lower123"));
      assertTrue(Strings.isAlphaNumeric("あえおいう１２３"));
      assertFalse(Strings.isAlphaNumeric(""));
      assertFalse(Strings.isAlphaNumeric(null));

      assertFalse(Strings.isNonAlphaNumeric("lower"));
      assertFalse(Strings.isNonAlphaNumeric("UPPER"));
      assertFalse(Strings.isNonAlphaNumeric("lower123"));
      assertFalse(Strings.isNonAlphaNumeric("UP589PER"));
      assertFalse(Strings.isNonAlphaNumeric("lower123"));
      assertFalse(Strings.isNonAlphaNumeric("あえおいう１２３"));
      assertTrue(Strings.isNonAlphaNumeric(""));
      assertTrue(Strings.isNonAlphaNumeric(null));

      assertTrue(Strings.isLetter("lower"));
      assertFalse(Strings.isLetter("lower123"));
      assertFalse(Strings.isLetter(""));
      assertFalse(Strings.isLetter(null));

      assertFalse(Strings.isDigit("lower123"));
      assertFalse(Strings.isDigit(""));
      assertFalse(Strings.isDigit(null));
      assertTrue(Strings.isDigit("１２３"));

      assertTrue(Strings.isPunctuation("、"));
      assertTrue(Strings.isPunctuation(","));
      assertFalse(Strings.isPunctuation("abc"));
      assertFalse(Strings.isPunctuation(""));
      assertFalse(Strings.isPunctuation(null));
   }


   @Test
   public void center() throws Exception {
      assertEquals("  One  ", Strings.center("One", 7));
      assertEquals("One", Strings.center("One", 1));
      assertNull(Strings.center(null, 1));
   }

   @Test(expected = IllegalArgumentException.class)
   public void centerLengthError() throws Exception {
      assertEquals("One", Strings.center("One", -1));
   }


   @Test
   public void repeat() throws Exception {
      assertEquals("++++", Strings.repeat('+', 4));
      assertEquals("++++", Strings.repeat("+", 4));
      assertEquals("+=+=+=+=", Strings.repeat("+=", 4));
      assertEquals("null", Strings.repeat(null, 1));
   }

   @Test
   public void hasPunctuation() throws Exception {
      assertTrue(Strings.hasPunctuation("A.C."));
      assertTrue(Strings.hasPunctuation(";-"));
      assertFalse(Strings.hasPunctuation("abc"));
      assertFalse(Strings.hasPunctuation(null));
   }

   @Test
   public void isAlphanumeric() throws Exception {
      assertFalse(Strings.isAlphaNumeric(null));
      assertFalse(Strings.isAlphaNumeric("A.C."));
      assertFalse(Strings.isAlphaNumeric(";-"));
      assertTrue(Strings.isAlphaNumeric("abc"));
      assertTrue(Strings.isAlphaNumeric("abc123"));
   }

   @Test
   public void isDigit() throws Exception {
      assertFalse(Strings.isDigit(null));
      assertFalse(Strings.isDigit("A.C."));
      assertFalse(Strings.isDigit(";-"));
      assertFalse(Strings.isDigit("abc"));
      assertFalse(Strings.isDigit("abc123"));
      assertTrue(Strings.isDigit("123"));
      assertFalse(Strings.isDigit("123,000.45"));
   }

   @Test
   public void hasDigit() throws Exception {
      assertFalse(Strings.hasDigit(null));
      assertFalse(Strings.hasDigit("A.C."));
      assertFalse(Strings.hasDigit(";-"));
      assertFalse(Strings.hasDigit("abc"));
      assertTrue(Strings.hasDigit("abc123"));
      assertTrue(Strings.hasDigit("123"));
      assertTrue(Strings.hasDigit("123,000.45"));
   }

   @Test
   public void hasLetter() throws Exception {
      assertFalse(Strings.hasLetter(null));
      assertTrue(Strings.hasLetter("A.C."));
      assertFalse(Strings.hasLetter(";-"));
      assertTrue(Strings.hasLetter("abc"));
      assertTrue(Strings.hasLetter("abc123"));
      assertFalse(Strings.hasLetter("123"));
      assertFalse(Strings.hasLetter("123,000.45"));
   }

   @Test
   public void safeEquals() throws Exception {
      assertTrue(Strings.safeEquals(null, null, false));
      assertTrue(Strings.safeEquals("A", "A", false));
      assertFalse(Strings.safeEquals("A", null, false));
      assertFalse(Strings.safeEquals(null, "A", false));
      assertTrue(Strings.safeEquals("a", "A", false));

      assertTrue(Strings.safeEquals(null, null, true));
      assertTrue(Strings.safeEquals("A", "A", true));
      assertFalse(Strings.safeEquals("a", "A", true));
      assertFalse(Strings.safeEquals("A", null, true));
      assertFalse(Strings.safeEquals(null, "A", true));
   }

   @Test
   public void unescape() throws Exception {
      assertNull(Strings.unescape(null, '\\'));
      assertEquals("A&C", Strings.unescape("A\\&C", '\\'));
      assertEquals("A&\\C", Strings.unescape("A\\&\\\\C", '\\'));
   }
}//END OF StringUtilsTest