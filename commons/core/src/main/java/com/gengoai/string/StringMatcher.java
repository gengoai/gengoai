package com.gengoai.string;

import com.gengoai.function.SerializablePredicate;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * <p>A <code>StringMatcher</code> is a {@link SerializablePredicate} for <code>CharSequence</code>s testing if
 * CharSequences match a specific pattern.</p>
 *
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface StringMatcher extends SerializablePredicate<CharSequence> {
   /**
    * Matches CharSequences that are not null.
    */
   StringMatcher NotNull = Objects::nonNull;
   /**
    * Matches CharSequences containing punctuation.
    */
   StringMatcher HasPunctuation = NotNull.and(CharMatcher.Punctuation::matchesAnyOf);
   /**
    * Matches CharSequences containing upper case
    */
   StringMatcher HasUpperCase = NotNull.and(CharMatcher.UpperCase::matchesAnyOf);
   /**
    * Matches CharSequences containing lower case
    */
   StringMatcher HasLowerCase = NotNull.and(CharMatcher.LowerCase::matchesAnyOf);
   /**
    * Matches CharSequences containing letters
    */
   StringMatcher HasLetter = NotNull.and(CharMatcher.Letter::matchesAnyOf);
   /**
    * Matches CharSequences containing digits
    */
   StringMatcher HasDigit = NotNull.and(CharMatcher.Digit::matchesAnyOf);
   /**
    * Matches CharSequences containing letter or digits
    */
   StringMatcher HasLetterOrDigit = NotNull.and(CharMatcher.LetterOrDigit::matchesAnyOf);
   /**
    * Matches CharSequences containing all letter or whitespace
    */
   StringMatcher LetterOrWhitespace = NotNull.and(CharMatcher.Letter.or(CharMatcher.WhiteSpace)::matchesAllOf);
   /**
    * Matches CharSequences are null
    */
   StringMatcher Null = Objects::isNull;
   /**
    * Matches CharSequences that are null or blank
    */
   StringMatcher NullOrBlank = Null.or(CharMatcher.WhiteSpace::matchesAllOf);
   /**
    * Matches CharSequences that are not null or blank
    */
   StringMatcher NotNullOrBlank = NullOrBlank.negate();
   /**
    * Matches CharSequences that are only lower case
    */
   StringMatcher LowerCase = NotNullOrBlank.and(CharMatcher.LowerCase::matchesAllOf);
   /**
    * Matches CharSequences that are only letters
    */
   StringMatcher Letter = NotNullOrBlank.and(CharMatcher.Letter::matchesAllOf);
   /**
    * Matches CharSequences that are only letter or digits
    */
   StringMatcher LetterOrDigit = NotNullOrBlank.and(CharMatcher.LetterOrDigit::matchesAllOf);
   /**
    * Matches digit only CharSequences
    */
   StringMatcher Digit = NotNullOrBlank.and(CharMatcher.Digit::matchesAllOf);
   /**
    * Matches punctuation only CharSequences
    */
   StringMatcher Punctuation = NotNullOrBlank.and(CharMatcher.Punctuation::matchesAllOf);
   /**
    * Matches upper case only CharSequences
    */
   StringMatcher UpperCase = NotNullOrBlank.and(CharMatcher.UpperCase::matchesAllOf);

   /**
    * String matcher that evaluates true if the CharSequence contains the given string to match.
    *
    * @param match the string to match
    * @return the string matcher
    */
   static StringMatcher contains(String match) {
      return contains(match, true);
   }

   /**
    * String matcher that evaluates true if the CharSequence contains the given string to match.
    *
    * @param match         the string to match
    * @param caseSensitive True case sensitive match, False case insensitive match.
    * @return the string matcher
    */
   static StringMatcher contains(String match, boolean caseSensitive) {
      final String prefix = caseSensitive
                            ? match
                            : match.toLowerCase();
      return sequence -> sequence != null && (caseSensitive
                                              ? sequence.toString().contains(prefix)
                                              : sequence.toString().toLowerCase().contains(prefix));
   }

   /**
    * String matcher that evaluates true if the CharSequence ends with the given string to match.
    *
    * @param match the string to match
    * @return the string matcher
    */
   static StringMatcher endsWith(String match) {
      return endsWith(match, true);
   }

   /**
    * String matcher that evaluates true if the CharSequence ends with the given string to match.
    *
    * @param match         the string to match
    * @param caseSensitive True case sensitive match, False case insensitive match.
    * @return the string matcher
    */
   static StringMatcher endsWith(String match, boolean caseSensitive) {
      final String suffix = caseSensitive
                            ? match
                            : match.toLowerCase();
      return sequence -> sequence != null && (caseSensitive
                                              ? sequence.toString().endsWith(suffix)
                                              : sequence.toString().toLowerCase().endsWith(suffix));
   }

   /**
    * String matcher that evaluates true if the CharSequence matches the given string to match.
    *
    * @param match the string to match
    * @return the string matcher
    */
   static StringMatcher matches(String match) {
      return matches(match, true);
   }

   /**
    * String matcher that evaluates true if the CharSequence matches the given string to match.
    *
    * @param match         the string to match
    * @param caseSensitive True case sensitive match, False case insensitive match.
    * @return the string matcher
    */
   static StringMatcher matches(String match, boolean caseSensitive) {
      return sequence -> sequence != null && (caseSensitive
                                              ? match.equals(sequence.toString())
                                              : match.equalsIgnoreCase(sequence.toString()));
   }

   /**
    * String matcher that evaluates true if the CharSequence is matched using the given regex.
    *
    * @param pattern the pattern to match
    * @return the string matcher
    */
   static StringMatcher regex(Pattern pattern) {
      return sequence -> sequence != null && pattern.matcher(sequence).find();
   }

   /**
    * String matcher that evaluates true if the CharSequence is matched using the given regex.
    *
    * @param pattern the pattern to match
    * @return the string matcher
    */
   static StringMatcher regex(String pattern) {
      return regex(Pattern.compile(pattern));
   }

   /**
    * String matcher that evaluates true if the CharSequence starts with the given string to match.
    *
    * @param match the string to match
    * @return the string matcher
    */
   static StringMatcher startsWith(String match) {
      return startsWith(match, true);
   }

   /**
    * String matcher that evaluates true if the CharSequence starts with the given string to match.
    *
    * @param match         the string to match
    * @param caseSensitive True case sensitive match, False case insensitive match.
    * @return the string matcher
    */
   static StringMatcher startsWith(String match, boolean caseSensitive) {
      final String prefix = caseSensitive
                            ? match
                            : match.toLowerCase();
      return sequence -> sequence != null && (caseSensitive
                                              ? sequence.toString().startsWith(prefix)
                                              : sequence.toString().toLowerCase().startsWith(prefix));
   }

   @Override
   default StringMatcher and(SerializablePredicate<? super CharSequence> other) {
      return sequence -> (test(sequence) && other.test(sequence));
   }

   @Override
   default StringMatcher negate() {
      return character -> !test(character);
   }

   @Override
   default StringMatcher or(SerializablePredicate<? super CharSequence> other) {
      return sequence -> (test(sequence) || other.test(sequence));
   }

}//END OF StringMatcher
