package com.gengoai.string;

import com.gengoai.function.SerializablePredicate;
import lombok.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Commonly used Regex classes and constructs for building Patterns.</p>
 *
 * @author David B. Bracewell
 */
public final class Re {
   /**
    * An unescaped period representing match anything.
    */
   public static final String ANY = ".";
   /**
    * Backlash character
    */
   public static final String BACKSLASH = "\\";
   /**
    * The constant CARRIAGE_RETURN.
    */
   public static final String CARRIAGE_RETURN = "\f";
   /**
    * Unicode close punctuation
    */
   public static final String CLOSE_PUNCTUATION = "\\p{Pe}";
   /**
    * Unicode connector punctuation
    */
   public static final String CONNECTOR_PUNCTUATION = "\\p{Pc}";
   /**
    * Unicode currency characters
    */
   public static final String CURRENCY_SYMBOL = "\\p{Sc}";
   /**
    * Unicode dash punctuation
    */
   public static final String DASH_PUNCTUATION = "\\p{Pd}";
   /**
    * Unicode digits
    */
   public static final String DIGIT = "\\p{Nd}";
   /**
    * Escaped backslash
    */
   public static final String ESC_BACKSLASH = "\\\\";
   /**
    * Unicode final punctuation
    */
   public static final String FINAL_PUNCTUATION = "\\p{Pf}";
   /**
    * The constant FORM_FEED.
    */
   public static final String FORM_FEED = "\f";
   /**
    * Unicode initial punctuation
    */
   public static final String INITIAL_PUNCTUATION = "\\p{Pi}";
   /**
    * Unicode letter
    */
   public static final String LETTER = "\\p{L}";
   /**
    * The constant LINE_FEED.
    */
   public static final String LINE_FEED = "\n";
   /**
    * Unicode lowercase letter
    */
   public static final String LOWERCASE_LETTER = "\\p{Ll}";
   /**
    * Unicode mark characters.
    */
   public static final String MARK = "\\p{m}";
   /**
    * Unicode math symbols
    */
   public static final String MATH_SYMBOL = "\\p{Sm}";
   /**
    * Unicode modifier symbols
    */
   public static final String MODIFIER_SYMBOL = "\\p{Sk}";
   /**
    * Unicode numbers
    */
   public static final String NUMBER = "\\p{N}";
   /**
    * Unicode open punctuation
    */
   public static final String OPEN_PUNCTUATION = "\\p{Ps}";
   /**
    * Unicode other symbols
    */
   public static final String OTHER_SYMBOL = "\\p{So}";
   /**
    * Unicode punctuation
    */
   public static final String PUNCTUATION = "\\p{P}";
   /**
    * The quote character.
    */
   public static final String QUOTE = "\"";
   /**
    * Unicode symbol characters
    */
   public static final String SYMBOL = "\\p{S}";
   /**
    * The constant TAB.
    */
   public static final String TAB = "\t";
   /**
    * The constant UNICODE_WHITESPACE.
    */
   public static final String UNICODE_WHITESPACE = "\\p{Z}";
   /**
    * The constant NON_WHITESPACE.
    */
   public static final String NON_WHITESPACE = chars(true, UNICODE_WHITESPACE,
                                                     LINE_FEED,
                                                     FORM_FEED,
                                                     CARRIAGE_RETURN,
                                                     TAB);
   /**
    * Unicode uppercase letter
    */
   public static final String UPPERCASE_LETTER = "\\p{Lu}";
   /**
    * matches unicode whitespace.
    */
   public static final String WHITESPACE = chars(UNICODE_WHITESPACE,
                                                 LINE_FEED,
                                                 FORM_FEED,
                                                 CARRIAGE_RETURN,
                                                 TAB);
   /**
    * The constant MULTIPLE_WHITESPACE.
    */
   public static final String MULTIPLE_WHITESPACE = WHITESPACE + "+";
   /**
    * Word boundary
    */
   public static final String WORD_BOUNDARY = "\b";
   /**
    * The constant ZERO_OR_MORE_WHITESPACE.
    */
   public static final String ZERO_OR_MORE_WHITESPACE = WHITESPACE + "*";

   private Re() {
      throw new IllegalAccessError();
   }

   /**
    * Any string.
    *
    * @return the string
    */
   public static String any() {
      return ".";
   }

   /**
    * Converts the given array of strings into a regex character class.
    *
    * @param negated True if the class should be negated.
    * @param chars   the components of the character class
    * @return the character class
    */
   public static String chars(boolean negated, @NonNull CharSequence... chars) {
      StringBuilder builder = new StringBuilder("[");
      if (negated) {
         builder.append("^");
      }
      builder.append(String.join("", chars));
      builder.append("]");
      return builder.toString();
   }

   /**
    * Converts the given array of characters into a regex character class.
    *
    * @param negated True if the class should be negated.
    * @param chars   the components of the character class
    * @return the character class
    */
   public static String chars(boolean negated, @NonNull char... chars) {
      StringBuilder out = new StringBuilder("[");
      if (negated) {
         out.append("^");
      }
      for (char c : chars) {
         out.append(c);
      }
      return out.append("]").toString();
   }

   /**
    * Converts the given array of strings into a regex character class.
    *
    * @param chars the components of the character class
    * @return the character class
    */
   public static String chars(String... chars) {
      return chars(false, chars);
   }

   /**
    * Converts the given array of chars into a regex character class.
    *
    * @param chars the components of the character class
    * @return the character class
    */
   public static String chars(char... chars) {
      return chars(false, chars);
   }

   /**
    * E string.
    *
    * @param character the character
    * @return the string
    */
   public static String e(char character) {
      return "\\" + character;
   }

   /**
    * Greedy one or more string.
    *
    * @param sequence the sequence
    * @return the string
    */
   public static String greedyOneOrMore(@NonNull CharSequence... sequence) {
      return String.format("%s+?", String.join("", sequence));
   }

   /**
    * Greedy zero or more string.
    *
    * @param sequence the sequence
    * @return the string
    */
   public static String greedyZeroOrMore(@NonNull CharSequence... sequence) {
      return String.format("%s*?", String.join("", sequence));
   }

   /**
    * Group string.
    *
    * @param sequence the sequence
    * @return the string
    */
   public static String group(@NonNull CharSequence... sequence) {
      return String.format("(%s)", String.join("", sequence));
   }

   /**
    * Generates a regular expression to match the entire line, i.e. <code>^pattern$</code>
    *
    * @param patterns The patterns making up the line
    * @return The regluar expresion
    */
   public static String line(@NonNull CharSequence... patterns) {
      return String.format("^%s$", re(patterns));
   }

   /**
    * Creates a {@link SerializablePredicate} to match the given Pattern by calling find on the resulting matcher.
    *
    * @param pattern the pattern to match
    * @return the {@link SerializablePredicate}
    */
   public static SerializablePredicate<CharSequence> match(@NonNull Pattern pattern) {
      return s -> pattern.matcher(s).find();
   }

   /**
    * Creates a {@link SerializablePredicate} to match the given Pattern by calling find on the resulting matcher.
    *
    * @param pattern the pattern to match
    * @return the {@link SerializablePredicate}
    */
   public static SerializablePredicate<CharSequence> match(@NonNull CharSequence... pattern) {
      return match(Pattern.compile(re(pattern)));
   }

   /**
    * Creates a {@link SerializablePredicate} to match the given Pattern by calling matches on the resulting matcher.
    *
    * @param pattern the pattern to match
    * @return the {@link SerializablePredicate}
    */
   public static SerializablePredicate<CharSequence> matchAll(@NonNull Pattern pattern) {
      return s -> pattern.matcher(s).matches();
   }

   /**
    * Creates a {@link SerializablePredicate} to match the given Pattern by calling matches on the resulting matcher.
    *
    * @param pattern the pattern to match
    * @return the {@link SerializablePredicate}
    */
   public static SerializablePredicate<CharSequence> matchAll(@NonNull CharSequence... pattern) {
      return matchAll(Pattern.compile(re(pattern)));
   }

   /**
    * Max string.
    *
    * @param max      the max
    * @param sequence the sequence
    * @return the string
    */
   public static String max(int max, @NonNull CharSequence... sequence) {
      return String.format("(?:%s){,%d}", String.join("", sequence), max);
   }

   /**
    * Min string.
    *
    * @param min      the min
    * @param sequence the sequence
    * @return the string
    */
   public static String min(int min, @NonNull CharSequence... sequence) {
      return String.format("(?:%s){%d}", String.join("", sequence), min);
   }

   /**
    * Defines the given regex as a named match group.
    *
    * @param groupName the group name
    * @param regex     the regex
    * @return the named match group
    */
   public static String namedGroup(@NonNull CharSequence groupName, @NonNull CharSequence... regex) {
      return String.format("(?<%s>%s)", groupName, String.join("", regex));
   }

   /**
    * Defines a negative lookahead for the given regex.
    *
    * @param regex the regex
    * @return the regex
    */
   public static String negLookahead(@NonNull CharSequence... regex) {
      return String.format("(?!%s)", String.join("", regex));
   }

   /**
    * Defines a negative non-consuming lookahead for the given regex.
    *
    * @param regex the regex
    * @return the regex
    */
   public static String negLookbehind(@NonNull CharSequence... regex) {
      return String.format("(?<!%s)", String.join("", regex));
   }

   /**
    * Next string.
    *
    * @param m the m
    * @return the string
    */
   public static String next(Matcher m) {
      if (m.find()) {
         return m.group();
      }
      return Strings.EMPTY;
   }

   /**
    * Defines the given regex as a non-matching group
    *
    * @param regex the regex
    * @return the non-matching group
    */
   public static String nonMatchingGroup(@NonNull CharSequence... regex) {
      return String.format("(?:%s)", String.join("", regex));
   }

   /**
    * Converts the given array of strings into a negated regex character class.
    *
    * @param chars the components of the character class
    * @return the negated character class
    */
   public static String notChars(CharSequence... chars) {
      return chars(true, chars);
   }

   /**
    * Converts the given array of strings into a negated regex character class.
    *
    * @param chars the components of the character class
    * @return the negated character class
    */
   public static String notChars(char... chars) {
      return chars(true, chars);
   }

   /**
    * One or more string.
    *
    * @param sequence the sequence
    * @return the string
    */
   public static String oneOrMore(@NonNull CharSequence... sequence) {
      return String.format("(?:%s)+", String.join("", sequence));
   }

   /**
    * Combines the given regex patterns as alternations. Should be wrapped as a group.
    *
    * @param sequence the regex
    * @return the alternation
    */
   public static String or(@NonNull CharSequence... sequence) {
      return String.format("(?:%s)", String.join("|", sequence));
   }

   /**
    * Defines a positive lookahead for the given regex.
    *
    * @param regex the regex
    * @return the regex
    */
   public static String posLookahead(@NonNull CharSequence... regex) {
      return String.format("(?=%s)", String.join("", regex));
   }

   /**
    * Defines a non-consuming positive lookahead for the given regex.
    *
    * @param regex the regex
    * @return the regex
    */
   public static String posLookbehind(@NonNull CharSequence... regex) {
      return String.format("(?<=%s)", String.join("", regex));
   }

   /**
    * Q string.
    *
    * @param pattern the pattern
    * @return the string
    */
   public static String q(@NonNull CharSequence pattern) {
      return Pattern.quote(pattern.toString());
   }

   /**
    * Compiles the given patterns, treating them as a sequence, with the given flags.
    *
    * @param flags    the flags
    * @param patterns the patterns
    * @return the pattern
    */
   public static Pattern r(int flags, @NonNull CharSequence... patterns) {
      return Pattern.compile(String.join("", patterns), flags);
   }

   /**
    * Compiles the given patterns, treating them as a sequence.
    *
    * @param patterns the patterns
    * @return the pattern
    */
   public static Pattern r(@NonNull CharSequence... patterns) {
      return Pattern.compile(String.join("", patterns));
   }

   /**
    * Range string.
    *
    * @param min      the min
    * @param max      the max
    * @param sequence the sequence
    * @return the string
    */
   public static String range(int min, int max, @NonNull CharSequence... sequence) {
      return String.format("(?:%s){%d,%d}", String.join("", sequence), min, max);
   }

   /**
    * Combines the given regex patterns into a sequence.
    *
    * @param sequence the regex
    * @return the string
    */
   public static String re(@NonNull CharSequence... sequence) {
      return String.format("(?:%s)", String.join("", sequence));
   }

   /**
    * Zero or more string.
    *
    * @param sequence the sequence
    * @return the string
    */
   public static String zeroOrMore(@NonNull CharSequence... sequence) {
      return String.format("(?:%s)*", String.join("", sequence));
   }

   /**
    * Zero or one string.
    *
    * @param sequence the sequence
    * @return the string
    */
   public static String zeroOrOne(@NonNull CharSequence... sequence) {
      return String.format("(?:%s)?", String.join("", sequence));
   }


}//END OF Regex
