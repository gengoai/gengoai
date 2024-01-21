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

package com.gengoai.io;

import com.gengoai.SystemInfo;
import com.gengoai.collection.HashMapIndex;
import com.gengoai.collection.Index;
import com.gengoai.conversion.Converter;
import com.gengoai.string.CSVFormatter;
import com.gengoai.string.Strings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p> Wraps writing collections and maps in DSV format to resources. </p>
 *
 * @author David B. Bracewell
 */
public class CSVWriter implements AutoCloseable {

   private final CSVFormatter formatter;
   private final BufferedWriter writer;
   private Index<String> header = new HashMapIndex<>();

   /**
    * Instantiates a new Csv writer.
    *
    * @param csv    the csv
    * @param writer the writer
    * @throws IOException the io exception
    */
   public CSVWriter(CSV csv, Writer writer) throws IOException {
      this.formatter = csv.formatter();
      this.writer = new BufferedWriter(writer);
      if(csv.getHeader() != null && !csv.getHeader().isEmpty()) {
         writer.write(formatter.format(csv.getHeader()));
         writer.write(SystemInfo.LINE_SEPARATOR);
         header.addAll(csv.getHeader());
      }
   }

   /**
    * Flushes the writer
    * @throws IOException Something went wrong flushing the writer
    */
   public void flush() throws IOException {
      writer.flush();
   }

   /**
    * Writes the items in the row to the resource in DSV format.
    *
    * @param row the row
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write(Iterable<?> row) throws IOException {
      if(row != null) {
         writer.write(formatter.format(row));
      }
      writer.write(SystemInfo.LINE_SEPARATOR);
   }

   /**
    * Writes the items in the row to the resource in DSV format.
    *
    * @param row the row
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write(Iterator<?> row) throws IOException {
      if(row != null) {
         writer.write(formatter.format(row));
      }
      writer.write(SystemInfo.LINE_SEPARATOR);
   }

   /**
    * Writes the items in the row to the resource in DSV format.
    *
    * @param row the row
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write(Map<?, ?> row) throws IOException {
      if(row != null) {
         if(header.isEmpty()) {
            List<?> entries = row.values().stream().map(o -> Converter.convertSilently(o, String.class)).collect(
                  Collectors.toList());
            writer.write(formatter.format(entries));
            writer.write(SystemInfo.LINE_SEPARATOR);
         } else {
            writer.write(
                  formatter.format(
                        Stream.concat(
                              header.stream()
                                    .map(
                                          h -> row.containsKey(h)
                                               ? Converter.convertSilently(row.get(h), String.class)
                                               : Strings.EMPTY),
                              row.keySet().stream()
                                 .map(k -> Converter.convertSilently(k, String.class))
                                 .filter(h -> !header.contains(h))
                                 .map(h -> Converter.convertSilently(row.get(h), String.class))
                                     )
                              .collect(Collectors.toList())
                                  )
                        );
            writer.write(SystemInfo.LINE_SEPARATOR);
         }

      }

   }

   /**
    * Writes the items in the row to the resource in DSV format.
    *
    * @param row               the row
    * @param keyValueSeparator the character to use to separate keys and values
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write(Map<?, ?> row, char keyValueSeparator) throws IOException {
      if(row != null) {
         if(header.isEmpty()) {
            writer.write(formatter.format(row, keyValueSeparator));
         } else {
            writer.write(formatter.format(Stream.concat(header.stream()
                                                              .map(
                                                                    h -> row.containsKey(h)
                                                                         ? Converter.convertSilently(row.get(h),
                                                                                                     String.class)
                                                                         : Strings.EMPTY),
                                                        row.keySet().stream()
                                                           .map(k -> Converter.convertSilently(k, String.class))
                                                           .filter(h -> !header.contains(h))
                                                           .map(
                                                                 h -> Converter.convertSilently(row.get(h),
                                                                                                String.class))
                                                       )
                                                .collect(Collectors.toList())
                                         )
                        );
         }
      }
      writer.write(SystemInfo.LINE_SEPARATOR);
   }

   /**
    * Writes the items in the row to the resource in DSV format.
    *
    * @param row the row
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write(Object... row) throws IOException {
      if(row != null) {
         writer.write(formatter.format(row));
      }
      writer.write(SystemInfo.LINE_SEPARATOR);
   }

   @Override
   public void close() throws IOException {
      writer.close();
   }

}//END OF CSVWriter
