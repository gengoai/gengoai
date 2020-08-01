package com.gengoai.string;

import com.gengoai.Validation;
import com.gengoai.function.SerializablePredicate;
import lombok.NonNull;

import java.util.BitSet;

/**
 * <p>A <code>CharMatcher</code> is a {@link SerializablePredicate} for <code>Character</code>s testing if characters
 * match a specific pattern. Additionally, methods exist to match against all, any, or no part of a
 * <code>CharSequence</code>.</p>
 *
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface CharMatcher extends SerializablePredicate<Character> {

   /**
    * Matches any character
    */
   CharMatcher Any = character -> true;
   /**
    * Matches any ascii character (int value {@code <=}  127)
    */
   CharMatcher Ascii = character -> character <= 127;
   /**
    * Matches digits defined using {@link Character#isDigit(char)}
    */
   CharMatcher Digit = Character::isDigit;
   /**
    * Matches ideographic characters defined using {@link Character#isIdeographic(int)}
    */
   CharMatcher Ideographic = Character::isIdeographic;
   /**
    * Matches letters defined using {@link Character#isLetter(char)}
    */
   CharMatcher Letter = Character::isLetter;
   /**
    * Matches letters or digits using {@link Character#isLetterOrDigit(char)}
    */
   CharMatcher LetterOrDigit = Character::isLetterOrDigit;
   /**
    * Matches lower case using {@link Character#isLowerCase(char)}
    */
   CharMatcher LowerCase = Character::isLowerCase;
   /**
    * Matches nothing
    */
   CharMatcher None = character -> false;
   /**
    * Matches punctuation.
    */
   CharMatcher Punctuation = c -> {
      switch (Character.getType(c)) {
         case Character.CONNECTOR_PUNCTUATION:
         case Character.DASH_PUNCTUATION:
         case Character.END_PUNCTUATION:
         case Character.FINAL_QUOTE_PUNCTUATION:
         case Character.INITIAL_QUOTE_PUNCTUATION:
         case Character.START_PUNCTUATION:
         case Character.OTHER_PUNCTUATION:
         case '=':
            return true;
      }
      return false;
   };
   /**
    * Matches upper case using {@link Character#isUpperCase(char)}
    */
   CharMatcher UpperCase = Character::isUpperCase;
   /**
    * Matches whitespace using {@link Character#isWhitespace(char)}
    */
   CharMatcher WhiteSpace = Character::isWhitespace;

   /**
    * Matches breaking whitespace.
    */
   CharMatcher BreakingWhiteSpace = character -> {
      //Taken from Guava
      switch (character) {
         case '\t':
         case '\n':
         case '\013':
         case '\f':
         case '\r':
         case ' ':
         case '\u0085':
         case '\u1680':
         case '\u2028':
         case '\u2029':
         case '\u205f':
         case '\u3000':
            return true;
         case '\u2007':
            return false;
         default:
            return character >= '\u2000' && character <= '\u200a';
      }
   };


   /**
    * Matches against any character in the given sequence.s
    *
    * @param characters the characters
    * @return the char matcher
    */
   static CharMatcher anyOf(@NonNull CharSequence characters) {
      final BitSet bitSet = characters.chars().collect(BitSet::new, BitSet::set, BitSet::or);
      return bitSet::get;
   }

   @Override
   default CharMatcher and(@NonNull SerializablePredicate<? super Character> other) {
      return character -> (test(character) && other.test(character));
   }

   /**
    * Finds the first character in the given sequence that matches.
    *
    * @param sequence the sequence
    * @return the index of the first match or -1 if none
    */
   default int findIn(@NonNull CharSequence sequence) {
      return findIn(sequence, 0);
   }

   /**
    * Finds the first character in the given sequence that matches.
    *
    * @param sequence the sequence
    * @param offset   the starting position to search in the CharSequence
    * @return the index of the first match or -1 if none
    */
   default int findIn(@NonNull CharSequence sequence, int offset) {
      Validation.checkElementIndex(offset, sequence.length());
      for (int i = offset; i < sequence.length(); i++) {
         if (test(sequence.charAt(i))) {
            return i;
         }
      }
      return -1;
   }

   /**
    * Checks if the entire CharSequence matches.
    *
    * @param sequence the sequence
    * @return True if this matcher matches all of the given CharSequence
    */
   default boolean matchesAllOf(@NonNull CharSequence sequence) {
      for (int i = 0; i < sequence.length(); i++) {
         if (!test(sequence.charAt(i))) {
            return false;
         }
      }
      return true;
   }

   /**
    * Checks if any of the CharSequence matches.
    *
    * @param sequence the sequence
    * @return True if this matcher matches any of the given CharSequence
    */
   default boolean matchesAnyOf(@NonNull CharSequence sequence) {
      for (int i = 0; i < sequence.length(); i++) {
         if (test(sequence.charAt(i))) {
            return true;
         }
      }
      return false;
   }

   /**
    * Checks if none of the CharSequence matches.
    *
    * @param sequence the sequence
    * @return True if this matcher matches none of the given CharSequence
    */
   default boolean matchesNoneOf(@NonNull CharSequence sequence) {
      for (int i = 0; i < sequence.length(); i++) {
         if (test(sequence.charAt(i))) {
            return false;
         }
      }
      return true;
   }

   @Override
   default CharMatcher negate() {
      return character -> !test(character);
   }

   @Override
   default CharMatcher or(@NonNull SerializablePredicate<? super Character> other) {
      return character -> (test(character) || other.test(character));
   }

   /**
    * Removes matches from the beginning and end of the string.
    *
    * @param sequence the sequence
    * @return the trimmed string
    */
   default String trimFrom(@NonNull CharSequence sequence) {
      return trimTrailingFrom(trimLeadingFrom(sequence));
   }

   /**
    * Removes matches from the beginning of the string until a non-match is found.
    *
    * @param sequence the sequence
    * @return the trimmed string
    */
   default String trimLeadingFrom(@NonNull CharSequence sequence) {
      for (int first = 0; first < sequence.length(); first++) {
         if (!test(sequence.charAt(first))) {
            return sequence.subSequence(first, sequence.length()).toString();
         }
      }
      return Strings.EMPTY;
   }

   /**
    * Trims matches from the end of the string until a non-match is found.
    *
    * @param sequence the sequence
    * @return the trimmed string
    */
   default String trimTrailingFrom(@NonNull CharSequence sequence) {
      for (int last = sequence.length() - 1; last >= 0; last--) {
         if (!test(sequence.charAt(last))) {
            return sequence.subSequence(0, last + 1).toString();
         }
      }
      return Strings.EMPTY;
   }


}//END OF CharMatcher
