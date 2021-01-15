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

import com.gengoai.conversion.Cast;
import com.gengoai.io.CSV;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.gengoai.Validation.checkArgument;

/**
 * <p> Formats a series of items in Delimited Separated Value format. </p>
 *
 * @author David B. Bracewell
 */
public class CSVFormatter implements Serializable {
   private static final long serialVersionUID = 1L;
   private final String comment;
   private final String delimiter;
   private final String doubleEscape;
   private final String doubleQuote;
   private final String escape;
   private final String quote;

   /**
    * Instantiates a new CSV formatter.
    *
    * @param csvFormat the csv format
    */
   public CSVFormatter(CSV csvFormat) {
      this.delimiter = Character.toString(csvFormat.getDelimiter());
      this.escape = Character.toString(csvFormat.getEscape());
      this.quote = Character.toString(csvFormat.getQuote());
      this.comment = Character.toString(csvFormat.getComment());
      this.doubleEscape = escape + escape;
      this.doubleQuote = quote + quote;
   }

   /**
    * Formats the items in the given <code>Stream</code>.
    *
    * @param stream the stream
    * @return the string
    */
   public String format(Stream<?> stream) {
      if (stream == null) {
         return Strings.EMPTY;
      }
      return format(stream.iterator());
   }

   /**
    * Formats the items in an <code>Iterator</code>.
    *
    * @param itr The iterator to format
    * @return A String representing the single DSV formatted row
    */
   public String format(Iterator<?> itr) {
      StringBuilder rowString = new StringBuilder();

      for (; itr.hasNext(); ) {
         Object n = itr.next();
         String s = n == null ? Strings.EMPTY : n.toString();
         if (escape.equals("\\")) {
            s = s.replaceAll(Pattern.quote(escape), doubleEscape + doubleEscape);
         } else {
            s = s.replaceAll(Pattern.quote(escape), doubleEscape);
         }
         s = s.replaceAll(Pattern.quote(comment), doubleEscape + comment);
         if (s.contains(delimiter) || s.contains("\r") || s.contains("\n") || s.contains(quote)) {
            rowString.append(quote);
            s = s.replaceAll(Pattern.quote(quote), doubleQuote);
            rowString.append(s);
            rowString.append(quote);
         } else {
            rowString.append(s);
         }

         if (itr.hasNext()) {
            rowString.append(delimiter);
         }
      }
      return rowString.toString();
   }

   /**
    * Formats the items in a <code>Map</code>. Keys and values are separated using <code>:</code>.
    *
    * @param map The Map to format
    * @return A String representing the single DSV formatted row of Map entries
    */
   public String format(Map<?, ?> map) {
      return format(map, ':');
   }

   /**
    * Formats the items in a <code>Map</code>.
    *
    * @param map               The Map to format
    * @param keyValueSeparator the character to use to separate keys and values
    * @return A String representing the single DSV formatted row of Map entries
    */
   public String format(Map<?, ?> map, char keyValueSeparator) {
      if (map == null) {
         return Strings.EMPTY;
      }
      checkArgument(keyValueSeparator != ' ');
      checkArgument(keyValueSeparator != delimiter.charAt(0),
                    "Key-Value separator must differ from the field delimiter");

      StringBuilder rowString = new StringBuilder();
      for (Iterator<?> itr = map.entrySet().iterator(); itr.hasNext(); ) {
         Map.Entry<?, ?> entry = Cast.as(itr.next());
         String key = entry.getKey() == null ? "null" : entry.getKey().toString();
         String value = entry.getValue() == null ? "null" : entry.getValue().toString();

         key = key.replaceAll(Pattern.quote(escape), doubleEscape + doubleEscape);
         value = value.replaceAll(Pattern.quote(escape), doubleEscape + doubleEscape);

         rowString.append(quote);

         if (key.indexOf(keyValueSeparator) >= 0) {
            rowString.append(doubleQuote).append(key.replaceAll(quote, doubleQuote))
                     .append(doubleQuote);
         } else {
            rowString.append(key.replaceAll(quote, doubleQuote));
         }

         rowString.append(keyValueSeparator);

         if (value.indexOf(keyValueSeparator) >= 0) {
            rowString.append(doubleQuote).append(value.replaceAll(quote, doubleQuote))
                     .append(doubleQuote);
         } else {
            rowString.append(value.replaceAll(quote, doubleQuote));
         }

         rowString.append(quote);

         if (itr.hasNext()) {
            rowString.append(delimiter);
         }

      }

      return rowString.toString();
   }

   /**
    * Formats the items in an <code>Iterable</code>.
    *
    * @param iterable the iterable
    * @return the string
    */
   public String format(Iterable<?> iterable) {
      if (iterable == null) {
         return Strings.EMPTY;
      }
      return format(iterable.iterator());
   }

   /**
    * Formats the items in an Array.
    *
    * @param array The array to format
    * @return A String representing the single DSV formatted row
    */
   public String format(Object... array) {
      if (array == null) {
         return Strings.EMPTY;
      }
      return format(Arrays.asList(array).iterator());
   }

}//END OF CSVFormatter
