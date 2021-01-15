package com.gengoai.string;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class CharMatcherTest {

   @Test
   public void ideographic() {
      assertFalse(CharMatcher.Ideographic.matchesAnyOf("ABC"));
      assertFalse(CharMatcher.Ideographic.matchesAllOf("ABC"));
      assertTrue(CharMatcher.Ideographic.matchesNoneOf("ABC"));
      assertTrue(CharMatcher.Ideographic.matchesAnyOf("日本語"));
      assertTrue(CharMatcher.Ideographic.matchesAllOf("日本語"));
      assertEquals(0, CharMatcher.Ideographic.findIn("日本語"));
      assertEquals(-1, CharMatcher.Ideographic.findIn("ABC"));
   }

   @Test
   public void ascii() {
      assertTrue(CharMatcher.Ascii.matchesAnyOf("ABC"));
      assertTrue(CharMatcher.Ascii.matchesAllOf("ABC"));
      assertFalse(CharMatcher.Ascii.matchesAnyOf("日本語"));
      assertFalse(CharMatcher.Ascii.matchesAllOf("日本語"));
   }

   @Test
   public void digit() {
      assertTrue(CharMatcher.Digit.matchesAnyOf("123"));
      assertTrue(CharMatcher.Digit.matchesAllOf("123"));
      assertTrue(CharMatcher.Digit.matchesAllOf("１２３"));
      assertFalse(CharMatcher.Digit.matchesAllOf("一二三"));
      assertFalse(CharMatcher.Digit.matchesAnyOf("日本語"));
      assertFalse(CharMatcher.Digit.matchesAllOf("日本語"));
   }

   @Test
   public void none() {
      assertFalse(CharMatcher.None.matchesAnyOf("日本語"));
      assertFalse(CharMatcher.None.matchesAllOf("ABC "));
   }

}