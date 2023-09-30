package com.gengoai.string;


import com.gengoai.Language;
import lombok.NonNull;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines an objects which acts similar to Java String.
 */
public interface StringLike extends CharSequence {

    @Override
    default char charAt(int i) {
        return toString().charAt(i);
    }

    /**
     * Returns true if and only if this string contains the specified sequence of char values.
     *
     * @param string the sequence to search for
     * @return true if this string contains s, false otherwise
     */
    default boolean contains(CharSequence string) {
        return toString().contains(string);
    }

    /**
     * Determines if the content of this HString equals the given char sequence.
     *
     * @param content the content to check for equality
     * @return True if equals, False otherwise
     */
    default boolean contentEquals(CharSequence content) {
        return content != null && toString().contentEquals(content);
    }

    /**
     * Determines if the content of this HString equals the given char sequence ignoring case.
     *
     * @param content the content to check for equality
     * @return True if equals, False otherwise
     */
    default boolean contentEqualsIgnoreCase(CharSequence content) {
        return content != null && toString().equalsIgnoreCase(content.toString());
    }

    /**
     * Determines if this HString ends with the given suffix.
     *
     * @param suffix the suffix to check
     * @return True ends with the given suffix, False otherwise
     */
    default boolean endsWith(CharSequence suffix) {
        return suffix != null && toString().endsWith(suffix.toString());
    }

    /**
     * Gets the language of the HString. If no language is set for this HString, the language of document will be
     * returned. In the event that this HString is not associated with a document, the default language will be
     * returned.
     *
     * @return The language of the HString
     */
    default Language getLanguage() {
        return Language.fromLocale(Locale.getDefault());
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring.
     *
     * @param text the substring to search for.
     * @return the index of the first occurrence of the specified substring, or -1 if there is no such occurrence.
     * @see String#indexOf(String) String#indexOf(String)String#indexOf(String)String#indexOf(String)String#indexOf(String)String#indexOf(String)String#indexOf(String)
     */
    default int indexOf(@NonNull CharSequence text) {
        return indexOf(text.toString(), 0);
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring.
     *
     * @param text  the substring to search for.
     * @param start the index to to start searching from
     * @return the index of the first occurrence of the specified substring, or -1 if there is no such occurrence.
     * @see String#indexOf(String, int) String#indexOf(String, int)String#indexOf(String, int)String#indexOf(String,
     * int)String#indexOf(String, int)String#indexOf(String, int)String#indexOf(String, int)
     */
    default int indexOf(@NonNull CharSequence text, int start) {
        return toString().indexOf(text.toString(), start);
    }

    @Override
    default int length() {
        return toString().length();
    }

    /**
     * Returns a regular expression matcher for the given pattern over this HString
     *
     * @param pattern the pattern to search for
     * @return the matcher
     */
    default Matcher matcher(@NonNull String pattern) {
        return Pattern.compile(pattern).matcher(this.toString());
    }

    /**
     * Returns a regular expression matcher for the given pattern over this HString
     *
     * @param pattern the pattern to search for
     * @return the matcher
     */
    default Matcher matcher(@NonNull Pattern pattern) {
        return pattern.matcher(this.toString());
    }

    /**
     * Tells whether this string matches the given regular expression.
     *
     * @param regex the regular expression
     * @return true if, and only if, this string matches the given regular expression
     * @see String#matches(String) String#matches(String)String#matches(String)String#matches(String)String#matches(String)String#matches(String)String#matches(String)
     */
    default boolean matches(@NonNull String regex) {
        return toString().matches(regex);
    }

    /**
     * Replaces all substrings of this string that matches the given string with the given replacement.
     *
     * @param oldString the old string
     * @param newString the new string
     * @return the string
     * @see String#replace(CharSequence, CharSequence) String#replace(CharSequence, CharSequence)String#replace(CharSequence,
     * CharSequence)String#replace(CharSequence, CharSequence)String#replace(CharSequence,
     * CharSequence)String#replace(CharSequence, CharSequence)String#replace(CharSequence, CharSequence)
     */
    default String replace(@NonNull CharSequence oldString, @NonNull CharSequence newString) {
        return toString().replace(oldString, newString);
    }

    /**
     * Replaces all substrings of this string that matches the given regular expression with the given replacement.
     *
     * @param regex       the regular expression
     * @param replacement the string to be substituted
     * @return the resulting string
     * @see String#replaceAll(String, String) String#replaceAll(String, String)String#replaceAll(String,
     * String)String#replaceAll(String, String)String#replaceAll(String, String)String#replaceAll(String,
     * String)String#replaceAll(String, String)
     */
    default String replaceAll(@NonNull String regex, @NonNull String replacement) {
        return toString().replaceAll(regex, replacement);
    }

    /**
     * Replaces the first substring of this string that matches the given regular expression with the given replacement.
     *
     * @param regex       the regular expression
     * @param replacement the string to be substituted
     * @return the resulting string
     * @see String#replaceFirst(String, String) String#replaceFirst(String, String)String#replaceFirst(String,
     * String)String#replaceFirst(String, String)String#replaceFirst(String, String)String#replaceFirst(String,
     * String)String#replaceFirst(String, String)
     */
    default String replaceFirst(@NonNull String regex, @NonNull String replacement) {
        return toString().replaceFirst(regex, replacement);
    }

    /**
     * Tests if this HString starts with the specified prefix.
     *
     * @param prefix the prefix
     * @return true if the HString starts with the specified prefix
     */
    default boolean startsWith(@NonNull CharSequence prefix) {
        return toString().startsWith(prefix.toString());
    }

    @Override
    default CharSequence subSequence(int startInclusive, int endExclusive) {
        return toString().subSequence(startInclusive, endExclusive);
    }

    /**
     * Converts this string to a new character array.
     *
     * @return a newly allocated character array whose length is the length of this string and whose contents are
     * initialized to contain the character sequence represented by this string.
     */
    default char[] toCharArray() {
        return toString().toCharArray();
    }

    /**
     * To lower case.
     *
     * @return the string
     * @see String#toLowerCase(Locale) String#toLowerCase(Locale)String#toLowerCase(Locale)String#toLowerCase(Locale)String#toLowerCase(Locale)String#toLowerCase(Locale)String#toLowerCase(Locale)NOTE:
     * Uses locale associated with the HString's langauge
     */
    default String toLowerCase() {
        return toString().toLowerCase(getLanguage().asLocale());
    }

    /**
     * Converts the HString to upper case
     *
     * @return the upper case version of the HString
     * @see String#toUpperCase(Locale) String#toUpperCase(Locale)String#toUpperCase(Locale)String#toUpperCase(Locale)String#toUpperCase(Locale)String#toUpperCase(Locale)String#toUpperCase(Locale)NOTE:
     * Uses locale associated with the HString's langauge
     */
    default String toUpperCase() {
        return toString().toUpperCase(getLanguage().asLocale());
    }

}//END OF StringLike
