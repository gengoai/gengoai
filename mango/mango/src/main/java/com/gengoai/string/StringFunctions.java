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

import com.gengoai.function.SerializableFunction;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Commonly used string transforms as {@link SerializableFunction}
 *
 * @author David B. Bracewell
 */
public enum StringFunctions implements SerializableFunction<CharSequence, String> {
   /**
    * Transforms a string into upper case format
    */
   UPPER_CASE {
      @Override
      public String apply(CharSequence input) {
         return input == null
                ? null
                : input.toString().toUpperCase();
      }
   },
   /**
    * Transforms a string into lower case format
    */
   LOWER_CASE {
      @Override
      public String apply(CharSequence input) {
         return input == null
                ? null
                : input.toString().toLowerCase();
      }
   },
   /**
    * Transforms a string into title case format
    */
   TITLE_CASE {
      @Override
      public String apply(CharSequence input) {
         if(input == null) {
            return null;
         }
         if(input.toString().isEmpty()) {
            return input.toString();
         }
         char[] chars = input.toString().toLowerCase().toCharArray();
         chars[0] = Character.toUpperCase(chars[0]);
         for(int i = 1; i < input.length() - 1; i++) {
            if(Character.isWhitespace(chars[i - 1])) {
               chars[i] = Character.toUpperCase(chars[i]);
            }
         }
         return new String(chars);
      }
   },
   /**
    * Reverses a string
    */
   REVERSE {
      @Override
      public String apply(CharSequence input) {
         return input == null
                ? null
                : new StringBuilder(input).reverse().toString();
      }
   },
   /**
    * Trims a function using unicode whitespace and invisible characters
    */
   TRIM {
      @Override
      public String apply(CharSequence input) {
         return input == null
                ? null
                : input.toString().strip();
      }
   },
   /**
    * Normalizes the string using {@link Normalizer.Form#NFKC}
    */
   CANONICAL_NORMALIZATION {
      @Override
      public String apply(CharSequence input) {
         return input == null
                ? null
                : Normalizer.normalize(input, Normalizer.Form.NFKC);
      }
   },
   /**
    * Normalizes the string using by removing diacritics
    */
   DIACRITICS_NORMALIZATION {
      @Override
      public String apply(CharSequence input) {
         return input == null
                ? null
                : Normalizer.normalize(Normalizer.normalize(input, Normalizer.Form.NFD)
                                                 .replaceAll("\\p{InCombiningDiacriticalMarks}+", ""),
                                       Normalizer.Form.NFC);
      }
   },
   /**
    * left trims strings of whitespace
    */
   LEFT_TRIM {
      @Override
      public String apply(CharSequence input) {
         return input == null
                ? null
                : input.toString().stripLeading();
      }
   },
   /**
    * Right trims strings of whitespace
    */
   RIGHT_TRIM {
      @Override
      public String apply(CharSequence input) {
         return input == null
                ? null
                : input.toString().stripTrailing();
      }
   },
   /**
    * Converts nulls to empty strings
    */
   NULL_TO_EMPTY {
      @Override
      public String apply(CharSequence input) {
         return input == null
                ? Strings.EMPTY
                : input.toString();
      }
   };


   /**
    * Creates a function that performs a regular expression replacement on a string
    *
    * @param pattern     The regex pattern
    * @param replacement The replacement text
    * @return The function
    */
   public static SerializableFunction<String, String> REGEX_REPLACE(String pattern, String replacement) {
      return REGEX_REPLACE(Pattern.compile(pattern), replacement);
   }

   /**
    * Creates a function that performs a regular expression replacement on a string
    *
    * @param pattern     The regex pattern
    * @param replacement The replacement text
    * @return The function
    */
   public static SerializableFunction<String, String> REGEX_REPLACE(final Pattern pattern, final String replacement) {
      return arg0 -> arg0 == null
                     ? null
                     : pattern.matcher(arg0).replaceAll(replacement);
   }


}//END OF StringFunctions
