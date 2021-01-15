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

import com.gengoai.Validation;
import com.gengoai.collection.tree.Span;
import com.gengoai.io.CSV;
import com.gengoai.io.CSVReader;
import com.gengoai.stream.Streams;
import com.gengoai.tuple.IntPair;
import lombok.NonNull;

import java.io.StringReader;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gengoai.Validation.notNullOrBlank;

/**
 * <p>Convenience methods for manipulating strings.</p>
 *
 * @author David B. Bracewell
 */
public final class Strings {
   /**
    * Single blank string
    */
   public static final String BLANK = " ";
   /**
    * Empty String
    */
   public static final String EMPTY = "";

   private Strings() {
      throw new IllegalAccessError();
   }

   public static String lineBreak(@NonNull String str, int lineWidth) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < str.length(); ) {
         int e = i + 1;
         while (e < str.length() && e <= i + lineWidth) {
            if (str.charAt(e) == '\n') {
               break;
            }
            e++;
         }
         while (e < str.length() && e > i && !Character.isWhitespace(str.charAt(e))) {
            e--;
         }
         builder.append(str.substring(i, e)).append("\n");
         i = e + 1;
      }
      return builder.toString();
   }

   /**
    * <p>Abbreviates a string to a desired length and adds "..." at the end.</p>
    *
    * @param input  The input string
    * @param length The length of the abbreviation
    * @return The abbreviated string
    */
   public static String abbreviate(CharSequence input, int length) {
      if (input == null) {
         return null;
      }
      if (input.length() <= length) {
         return input.toString();
      }
      return input.subSequence(0, length) + "...";
   }

   /**
    * Append if not present string.
    *
    * @param string the string
    * @param suffix the suffix
    * @return the string
    */
   public static String appendIfNotPresent(@NonNull String string, @NonNull String suffix) {
      return string.endsWith(suffix)
            ? string
            : (string + suffix);
   }

   /**
    * Centers an input string inside a string of given length
    *
    * @param s      the string to center
    * @param length the length of the new string
    * @return the centered string
    */
   public static String center(String s, int length) {
      Validation.checkArgument(length > 0, "Length must be > 0");
      if (s == null) {
         return null;
      }
      int start = (int) Math.floor(Math.max(0, (length - s.length()) / 2d));
      return padEnd(repeat(' ', start) + s, length, ' ');
   }

   /**
    * Counts the number target strings in the given string
    *
    * @param str    the str to calculate the count on
    * @param target the target which we want to count
    * @return the number of times target occurs in str
    */
   public static int count(String str, String target) {
      notNullOrBlank(target, "Target string must not be null or blank,");
      if (Strings.isNullOrBlank(str)) {
         return 0;
      }
      int index = -target.length();
      int count = 0;
      while ((index = str.indexOf(target, index + target.length())) != -1) {
         count++;
      }
      return count;
   }

   /**
    * Converts empty/bank strings to null
    *
    * @param input the input string
    * @return the output string (null if input null or blank, input otherwise)
    */
   public static String emptyToNull(String input) {
      return Strings.isNotNullOrBlank(input)
            ? input
            : null;
   }

   /**
    * Escapes a string by placing an escape character in front of reserved characters.
    *
    * @param input              the input
    * @param escapeCharacter    the escape character
    * @param reservedCharacters the characters needing to be escaped
    * @return the string
    */
   public static String escape(String input, char escapeCharacter, @NonNull String reservedCharacters) {
      return escape(input,
                    Character.toString(escapeCharacter),
                    i -> reservedCharacters.contains(Character.toString(i)),
                    Character::toString);
   }

   /**
    * Escapes and converts characters matched using the given <code>escapeChecker</code> predicate by prepending the
    * transformed value, determines using the given <code>conversionFunction</code>, with the
    * <code>escapeMarker</code>.
    * <p>
    * <pre>
    * {@code
    *       escape("MY @ time is great.",
    *              "\\x",
    *              c -> !Character.isLetter(c) && !Character.isWhitespace(c),
    *              c -> Integer::toHexString);
    * }
    * </pre>
    * Generates <code>MY \x40 time is great\x2e</code>
    * </p>
    *
    * @param input              the input string to escape
    * @param escapeMarker       the escape string to use
    * @param escapeChecker      the predicate to check if a character needs to be escaped
    * @param conversionFunction the function to convert the character needing escaped to a string.
    * @return the escaped string
    */
   public static String escape(String input,
                               @NonNull String escapeMarker,
                               @NonNull IntPredicate escapeChecker,
                               @NonNull IntFunction<String> conversionFunction) {
      if (input == null) {
         return null;
      }
      StringBuilder builder = new StringBuilder();
      input.chars()
           .forEach(c -> {
              if (escapeChecker.test(c)) {
                 builder.append(escapeMarker)
                        .append(conversionFunction.apply(c));
              } else {
                 builder.append((char) c);
              }
           });
      return builder.toString();
   }

   /**
    * Escapes the unicode in the given string using the Java specification
    *
    * @param string The string to escape
    * @return The escaped string
    */
   public static String escapeUnicode(String string) {
      return escape(string,
                    "\\u",
                    i -> i > 128,
                    c -> String.format("%04X", (int) c));
   }

   /**
    * Expand int pair.
    *
    * @param txt   the txt
    * @param start the start
    * @param end   the end
    * @return the int pair
    */
   public static Span expand(String txt, int start, int end) {

      while (start > 0 &&
            !Character.isWhitespace(txt.charAt(start - 1)) &&
            !CharMatcher.Punctuation.test(txt.charAt(start - 1))) {
         start--;
      }

      while (start < end &&
            Character.isWhitespace(txt.charAt(start))) {
         start++;
      }

      while (end < txt.length() &&
            !Character.isWhitespace(txt.charAt(end)) &&
            !CharMatcher.Punctuation.test(txt.charAt(end))) {
         end++;
      }

      while (end > start &&
            Character.isWhitespace(txt.charAt(end - 1))) {
         end--;
      }

      return Span.of(start, end);
   }

   public static Iterator<IntPair> findIterator(@NonNull String input, @NonNull String target) {
      return new FindIterator(input, target);
   }

   public static String firstMatch(@NonNull Pattern pattern, @NonNull String input, int group) {
      Matcher m = pattern.matcher(input);
      if (m.find()) {
         return m.group(group);
      }
      return Strings.EMPTY;
   }

   public static String firstMatch(@NonNull Pattern pattern, @NonNull String input, @NonNull String group) {
      Matcher m = pattern.matcher(input);
      if (m.find()) {
         return m.group(group);
      }
      return Strings.EMPTY;
   }

   public static String firstMatch(@NonNull Pattern pattern, @NonNull String input) {
      Matcher m = pattern.matcher(input);
      if (m.find()) {
         return m.group();
      }
      return Strings.EMPTY;
   }

   /**
    * Determines if a string has at least one digit
    *
    * @param string the string to check
    * @return True if the string has at least one digit
    */
   public static boolean hasDigit(CharSequence string) {
      return StringMatcher.HasDigit.test(string);
   }

   /**
    * Determines if a string has at least one letter
    *
    * @param string the string to check
    * @return True if the string has at least one letter
    */
   public static boolean hasLetter(CharSequence string) {
      return StringMatcher.HasLetter.test(string);
   }

   /**
    * Determines if a given string has one or more punctuation characters.
    *
    * @param string the string to check
    * @return True if the string has one or more punctuation characters
    */
   public static boolean hasPunctuation(CharSequence string) {
      return StringMatcher.HasPunctuation.test(string);
   }

   /**
    * Determines if a string is only made up of letters or digits
    *
    * @param string the string to check
    * @return True if the string is only made up of letter or digits
    */
   public static boolean isAlphaNumeric(CharSequence string) {
      return StringMatcher.LetterOrDigit.test(string);
   }

   /**
    * Determines if a string is only made up of numbers.
    *
    * @param string the string to check
    * @return True if the string is only made up of numbers.
    */
   public static boolean isDigit(CharSequence string) {
      return StringMatcher.Digit.test(string);
   }

   /**
    * Determines if a string is only made up of letters.
    *
    * @param string the string to check
    * @return True if the string is only made up of letters.
    */
   public static boolean isLetter(CharSequence string) {
      return StringMatcher.Letter.test(string);
   }

   /**
    * Determines if an entire string is lower case or not
    *
    * @param input The input string
    * @return True if the string is lower case, False if not
    */
   public static boolean isLowerCase(CharSequence input) {
      return StringMatcher.LowerCase.test(input);
   }

   /**
    * Determines if a string is only made up of non letters and digits
    *
    * @param string the string to check
    * @return True if the string is only made up of non letters and digits
    */
   public static boolean isNonAlphaNumeric(CharSequence string) {
      return !StringMatcher.LetterOrDigit.test(string);
   }

   /**
    * Determines if a string is not null or blank (trimmed string is empty).
    *
    * @param input The input string
    * @return True when the input string is not null and the trimmed version of the string is not empty.
    */
   public static boolean isNotNullOrBlank(CharSequence input) {
      return StringMatcher.NotNullOrBlank.test(input);
   }

   /**
    * Determines if a string is null or blank (trimmed string is empty).
    *
    * @param input The input string
    * @return True when the input string is null or the trimmed version of the string is empty.
    */
   public static boolean isNullOrBlank(CharSequence input) {
      return StringMatcher.NullOrBlank.test(input);
   }

   /**
    * Determines if a given string is only made up of punctuation characters.
    *
    * @param string the string to check
    * @return True if the string is all punctuation
    */
   public static boolean isPunctuation(CharSequence string) {
      return StringMatcher.Punctuation.test(string);
   }

   /**
    * Determines if an entire string is title case or not
    *
    * @param input The input string
    * @return True if the string is title case, False if not
    */
   public static boolean isTitleCase(@NonNull CharSequence input) {
      if (input.length() == 0) {
         return false;
      }
      if (input.length() == 1) {
         return Character.isUpperCase(input.charAt(0));
      }
      return Character.isUpperCase(input.charAt(0)) && StringMatcher.LowerCase.test(input.subSequence(1,
                                                                                                      input.length()));
   }

   /**
    * Determines if an entire string is upper case or not
    *
    * @param input The input string
    * @return True if the string is upper case, False if not
    */
   public static boolean isUpperCase(CharSequence input) {
      return StringMatcher.UpperCase.test(input);
   }

   /**
    * Joins the items in the given iterable into a string separated using the given delimiter, with the given prefix at
    * the beginning, and the given suffix at the end of the string.
    *
    * @param iterable  the iterable
    * @param delimiter the delimiter
    * @param prefix    the prefix
    * @param suffix    the suffix
    * @return the string
    */
   public static String join(@NonNull Iterable<?> iterable,
                             @NonNull CharSequence delimiter,
                             @NonNull CharSequence prefix,
                             @NonNull CharSequence suffix) {
      return Streams.asStream(iterable)
                    .map(Object::toString)
                    .collect(Collectors.joining(delimiter, prefix, suffix));
   }

   /**
    * Joins the items in the given iterable into a string separated using the given delimiter.
    *
    * @param iterable  the iterable
    * @param delimiter the delimiter
    * @return the string
    */
   public static String join(@NonNull Iterable<?> iterable, @NonNull CharSequence delimiter) {
      return Streams.asStream(iterable)
                    .map(Object::toString)
                    .collect(Collectors.joining(delimiter));
   }

   /**
    * Joins the items in the given array into a string separated using the given delimiter
    *
    * @param <T>       the type parameter
    * @param values    the values
    * @param delimiter the delimiter
    * @return the string
    */
   public static <T> String join(@NonNull T[] values, @NonNull CharSequence delimiter) {
      return Streams.asStream(values)
                    .map(Object::toString)
                    .collect(Collectors.joining(delimiter));
   }

   /**
    * Joins the items in the given array into a string separated using the given delimiter, with the given prefix at the
    * beginning, and the given suffix at the end of the string.
    *
    * @param <T>       the type parameter
    * @param values    the values
    * @param delimiter the delimiter
    * @param prefix    the prefix
    * @param suffix    the suffix
    * @return the string
    */
   public static <T> String join(@NonNull T[] values,
                                 @NonNull CharSequence delimiter,
                                 @NonNull CharSequence prefix,
                                 @NonNull CharSequence suffix) {
      return Streams.asStream(values)
                    .map(Object::toString)
                    .collect(Collectors.joining(delimiter, prefix, suffix));
   }

   public static void main(String[] args) {
      System.out.println(escape("MY @ time is great.",
                                "\\x",
                                c -> !Character.isLetter(c) && !Character.isWhitespace(c),
                                Integer::toHexString));
   }

   public static Iterator<IntPair> matchIterator(@NonNull String input, @NonNull Pattern pattern) {
      return new MatcherIterator(pattern.matcher(input));
   }

   /**
    * Converts null values into an empty string
    *
    * @param input the input string
    * @return the output string (empty if input null, input otherwise)
    */
   public static String nullToEmpty(String input) {
      return input == null
            ? EMPTY
            : input;
   }

   /**
    * Pads the end of the string to make the string into the desired length using the given padding character to make up
    * the additional length.
    *
    * @param sequence         the sequence
    * @param desiredLength    the desired length
    * @param paddingCharacter the padding character
    * @return the string
    */
   public static String padEnd(CharSequence sequence, int desiredLength, char paddingCharacter) {
      if (sequence == null) {
         return repeat(paddingCharacter, desiredLength);
      } else if (sequence.length() == desiredLength) {
         return sequence.toString();
      }
      return sequence.toString() + repeat(paddingCharacter, desiredLength - sequence.length());
   }

   /**
    * Pads the beginning of the string to make the string into the desired length using the given padding character to
    * make up the additional length.
    *
    * @param sequence         the sequence
    * @param desiredLength    the desired length
    * @param paddingCharacter the padding character
    * @return the string
    */
   public static String padStart(CharSequence sequence, int desiredLength, char paddingCharacter) {
      if (sequence == null) {
         return repeat(paddingCharacter, desiredLength);
      } else if (sequence.length() == desiredLength) {
         return sequence.toString();
      }
      return repeat(paddingCharacter, desiredLength - sequence.length()) + sequence.toString();
   }

   /**
    * Prepend if not present string.
    *
    * @param string the string
    * @param prefix the prefix
    * @return the string
    */
   public static String prependIfNotPresent(@NonNull String string, @NonNull String prefix) {
      return string.startsWith(prefix)
            ? string
            : (prefix + string);
   }

   /**
    * Generates a random string of given length made up of valid hexadecimal characters.
    *
    * @param length the length of the string
    * @return the random string
    */
   public static String randomHexString(int length) {
      return randomString(length, CharMatcher.anyOf("ABCDEF1234567890"));
   }

   /**
    * Generates a random string of a given length
    *
    * @param length The length of the string
    * @param min    The min character in the string
    * @param max    The max character in the string
    * @return A string of random characters
    */
   public static String randomString(int length, int min, int max) {
      return randomString(length, min, max, CharMatcher.Any);
   }

   /**
    * Generates a random string of a given length
    *
    * @param length    The length of the string
    * @param validChar CharPredicate that must match for a character to be returned in the string
    * @return A string of random characters
    */
   public static String randomString(int length, CharMatcher validChar) {
      return randomString(length, 0, Integer.MAX_VALUE, validChar);
   }

   /**
    * Generates a random string of a given length
    *
    * @param length    The length of the string
    * @param min       The min character in the string
    * @param max       The max character in the string
    * @param validChar CharPredicate that must match for a character to be returned in the string
    * @return A string of random characters
    */
   public static String randomString(int length, int min, int max, CharMatcher validChar) {
      if (length <= 0) {
         return EMPTY;
      }
      Random random = new Random();
      int maxRandom = max - min;
      char[] array = new char[length];
      for (int i = 0; i < array.length; i++) {
         char c;
         do {
            c = (char) (random.nextInt(maxRandom) + min);
         } while (Character.isLowSurrogate(c) ||
               Character.isHighSurrogate(c) ||
               !validChar.test(c));
         array[i] = c;
      }
      return new String(array);
   }

   /**
    * Normalizes a string by removing the diacritics.
    *
    * @param input the input string
    * @return Resulting string without diacritic marks
    */
   public static String removeDiacritics(CharSequence input) {
      if (input == null) {
         return null;
      }
      return StringFunctions.DIACRITICS_NORMALIZATION.apply(input.toString());
   }

   /**
    * <p>Replaces repeated characters with a single instance. e.g. <code>Gooooood</code> would become
    * <code>God</code>.</p>
    *
    * @param sequence The character sequence
    * @return The compacted string
    * @throws NullPointerException when the sequence is null
    */
   public static String removeRepeatedChars(CharSequence sequence) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < sequence.length(); i++) {
         char c = sequence.charAt(i);
         if (builder.length() == 0 || builder.charAt(builder.length() - 1) != c) {
            builder.append(c);
         }
      }
      return builder.toString();
   }

   /**
    * Repeats the given string count times
    *
    * @param s     the string to repeat
    * @param count the count
    * @return the string
    */
   public static String repeat(String s, int count) {
      return IntStream.range(0, count)
                      .mapToObj(i -> s)
                      .collect(Collectors.joining());
   }

   /**
    * Repeats the given character count times
    *
    * @param c     the character to repeat
    * @param count the count
    * @return the string
    */
   public static String repeat(char c, int count) {
      return repeat(Character.toString(c), count);
   }

   /**
    * Safe equals of two strings taking null into consideration.
    *
    * @param s1            the first string
    * @param s2            the second string
    * @param caseSensitive True if equals is case sensitive, False if case insensitive.
    * @return the boolean
    */
   public static boolean safeEquals(String s1, String s2, boolean caseSensitive) {
      if (s1 == s2) {
         return true;
      } else if (s1 == null || s2 == null) {
         return false;
      } else if (caseSensitive) {
         return s1.equals(s2);
      }
      return s1.equalsIgnoreCase(s2);
   }

   /**
    * Properly splits a delimited separated string using {@link CSV} assuming default values for the CSV object except
    * for the delimiter.
    *
    * @param input     The input string
    * @param separator The separator
    * @return A list of all the cells in the input
    */
   public static List<String> split(CharSequence input, char separator) {
      if (input == null) {
         return new ArrayList<>();
      }
      Validation.checkArgument(separator != '"', "Separator cannot be a quote");
      try (CSVReader reader = CSV.builder().delimiter(separator).reader(new StringReader(input.toString()))) {
         List<String> all = new ArrayList<>();
         List<String> row;
         while ((row = reader.nextRow()) != null) {
            all.addAll(row);
         }
         return all;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Normalize to canonical form.
    *
    * @param input the input string
    * @return the normalized string
    */
   public static String toCanonicalForm(CharSequence input) {
      if (input == null) {
         return null;
      }
      return StringFunctions.CANONICAL_NORMALIZATION.apply(input.toString());
   }

   /**
    * Converts an input string to title case
    *
    * @param input The input string
    * @return The title cased version of the input
    */
   public static String toTitleCase(CharSequence input) {
      if (input == null) {
         return null;
      }
      return StringFunctions.TITLE_CASE.apply(input.toString());
   }

   /**
    * Unescapes a string which is escaped with the given escaped character.
    *
    * @param input           the input
    * @param escapeCharacter the escape character
    * @return the string
    */
   public static String unescape(String input, char escapeCharacter) {
      if (input == null) {
         return null;
      }
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < input.length(); ) {
         if (input.charAt(i) == escapeCharacter) {
            builder.append(input.charAt(i + 1));
            i = i + 2;
         } else {
            builder.append(input.charAt(i));
            i++;
         }
      }
      return builder.toString();
   }

   private static class FindIterator implements Iterator<IntPair> {
      private final String input;
      private final String target;
      private int start = 0;

      private FindIterator(String input, String target) {
         this.input = input;
         this.target = target;
      }

      protected boolean advance() {
         if (input.length() == 0 || start >= input.length()) {
            return false;
         }
         start = input.indexOf(target);
         return start >= 0;
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public IntPair next() {
         if (!advance()) {
            throw new NoSuchElementException();
         }
         var retValue = IntPair.of(start, start + target.length());
         start += target.length();
         return retValue;
      }
   }

   private static class MatcherIterator implements Iterator<IntPair> {
      private final Matcher matcher;
      private IntPair next = null;

      private MatcherIterator(Matcher matcher) {
         this.matcher = matcher;
      }

      protected boolean advance() {
         if (next != null) {
            return true;
         }
         if (matcher.find()) {
            next = IntPair.of(matcher.start(), matcher.end());
            return true;
         }
         return false;
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public IntPair next() {
         if (!advance()) {
            throw new NoSuchElementException();
         }
         var retValue = IntPair.of(next.v1, next.v2);
         next = null;
         return retValue;
      }
   }

}// END OF StringUtils

