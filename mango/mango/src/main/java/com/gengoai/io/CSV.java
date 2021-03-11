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

import com.gengoai.function.Unchecked;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.Streams;
import com.gengoai.string.CSVFormatter;
import lombok.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * <p>Specification of a delimited separated file, or more commonly refereed to as CSV. Provides methods to build a CSV
 * specification and then create a reader, writer, or formatter.</p>
 *
 * @author David B. Bracewell
 */
public class CSV implements Serializable {
   private static final long serialVersionUID = 1L;

   private char comment = '#';
   private char delimiter = ',';
   private char escape = '\\';
   private boolean keepEmptyCells = true;
   private char quote = '\"';
   private boolean hasHeader = false;
   private List<String> header = null;

   /**
    * <p>Creates a CSV builder object</p>
    *
    * @return the csv builder builder object
    */
   public static CSV builder() {
      return new CSV();
   }

   /**
    * <p>Convenience method for quickly retrieving a csv version of a CSV builder</p>
    *
    * @return the csv builder builder with delimiter set to a comma character
    */
   public static CSV csv() {
      return new CSV();
   }

   /**
    * <p>Convenience method for quickly retrieving a tsv version of a CSV builder</p>
    *
    * @return the csv builder builder with delimiter set to a tab character
    */
   public static CSV tsv() {
      return CSV.builder().delimiter('\t');
   }

   /**
    * Sets the character that signifies a comment
    *
    * @param commentChar the comment char
    * @return the csv builder
    */
   public CSV comment(char commentChar) {
      this.comment = commentChar;
      return this;
   }

   /**
    * Sets the delimiter character
    *
    * @param delimiter the delimiter
    * @return the csv builder
    */
   public CSV delimiter(char delimiter) {
      this.delimiter = delimiter;
      return this;
   }

   /**
    * Sets the escape character
    *
    * @param escape the escape
    * @return the csv builder
    */
   public CSV escape(char escape) {
      this.escape = escape;
      return this;
   }

   /**
    * Creates a CSVFormatter using this specification
    *
    * @return the CSVFormatter
    */
   public CSVFormatter formatter() {
      return new CSVFormatter(this);
   }

   /**
    * Gets the comment character.
    *
    * @return the comment character.
    */
   public char getComment() {
      return comment;
   }

   /**
    * Gets the delimiter character.
    *
    * @return the delimiter character
    */
   public char getDelimiter() {
      return delimiter;
   }

   /**
    * Gets the escape character.
    *
    * @return the escape character
    */
   public char getEscape() {
      return escape;
   }

   /**
    * Determines if the CSV file is expected to have a header or not
    *
    * @return True if the first line of the csv file is the header, false if there is no header
    */
   public boolean getHasHeader() {
      return hasHeader;
   }

   /**
    * Gets the name of the column headers.
    *
    * @return the names of the column headers
    */
   public List<String> getHeader() {
      return header;
   }

   /**
    * Gets the quote character.
    *
    * @return the quote character
    */
   public char getQuote() {
      return quote;
   }

   /**
    * Specifies that the CSV file has a header.
    *
    * @return the csv builder builder
    */
   public CSV hasHeader() {
      return hasHeader(true);
   }

   /**
    * Specifies whether or not the CSV file has a header.
    *
    * @param hasHeader true the csv has a header, false it does not
    * @return the csv builder
    */
   public CSV hasHeader(boolean hasHeader) {
      this.hasHeader = hasHeader;
      return this;
   }

   /**
    * Specifies the names of the items in the CSV header.
    *
    * @param items the names of the columns (i.e. header names)
    * @return the csv builder
    */
   public CSV header(String... items) {
      this.header = (items == null
                     ? null
                     : Arrays.asList(items));
      if(header != null && header.size() > 0) {
         this.hasHeader = true;
      }
      return this;
   }

   /**
    * Specifies the names of the items in the CSV header.
    *
    * @param items the names of the columns (i.e. header names)
    * @return the csv builder
    */
   public CSV header(List<String> items) {
      this.header = items;
      if(header != null && header.size() > 0) {
         this.hasHeader = true;
      }
      return this;
   }

   /**
    * Determines if empty cells should be kept or not
    *
    * @return True if empty cells are kept, False if they are removed
    */
   public boolean isKeepEmptyCells() {
      return keepEmptyCells;
   }

   /**
    * Creates an iterator  over the rows of the csv file
    *
    * @param resource the resource to read from
    * @return the stream of items in the csv file
    * @throws IOException Something went wrong reading the file
    */
   public Iterator<List<String>> iterator(@NonNull Resource resource) throws IOException {
      return new CSVRowListIterator(reader(resource));
   }

   /**
    * Specifies that empty cells should be kept
    *
    * @return the csv builder
    */
   public CSV keepEmptyCells() {
      this.keepEmptyCells = true;
      return this;
   }

   /**
    * Sets the quote character
    *
    * @param quote the quote
    * @return the csv builder
    */
   public CSV quote(char quote) {
      this.quote = quote;
      return this;
   }

   /**
    * Creates a CSVReader using this specification from a given reader
    *
    * @param reader the reader to wrap
    * @return The CSVReader
    * @throws IOException Something went wrong initializing the reader
    */
   public CSVReader reader(Reader reader) throws IOException {
      return new CSVReader(this, reader);
   }

   /**
    * Creates a CSVReader using this specification from a given resource
    *
    * @param resource the resource to read from
    * @return The CSVReader
    * @throws IOException Something went wrong initializing the resource
    */
   public CSVReader reader(Resource resource) throws IOException {
      return reader(resource.reader());
   }

   /**
    * Specifies that empty cells should be removed
    *
    * @return the csv builder
    */
   public CSV removeEmptyCells() {
      this.keepEmptyCells = false;
      return this;
   }

   /**
    * Creates a reusable Java Stream over the rows of the csv file
    *
    * @param resource the resource to read from
    * @return the stream of items in the csv file
    * @throws IOException Something went wrong reading the file
    */
   public Stream<List<String>> rowListStream(@NonNull Resource resource) throws IOException {
      return Streams.reusableStream(Unchecked.supplier(() -> Streams.asStream(new CSVRowListIterator(reader(resource)))));
   }

   /**
    * Creates a reusable Java Stream over the rows of the csv file returning them as map with key as the column name and
    * value as the value. When the column is not in the header the column name will be "AutoColumn-" and column number.
    *
    * @param resource the resource to read from
    * @return the stream of items in the csv file
    * @throws IOException Something went wrong reading the file
    */
   public Stream<Map<String, String>> rowMapStream(@NonNull Resource resource) throws IOException {
      return Streams.reusableStream(Unchecked.supplier(() -> Streams.asStream(new CSVRowMapIterator(reader(resource)))));
   }

   /**
    * Creates a CSVWriter using this specification from a given writer
    *
    * @param writer the writer to wrap
    * @return The CSVWriter
    * @throws IOException Something went wrong initializing the writer
    */
   public CSVWriter writer(Writer writer) throws IOException {
      return new CSVWriter(this, writer);
   }

   /**
    * Creates a CSVWriter using this specification from a given resource
    *
    * @param resource the resource to write to
    * @return The CSVWriter
    * @throws IOException Something went wrong initializing the resource
    */
   public CSVWriter writer(Resource resource) throws IOException {
      return writer(resource.writer());
   }
}//END OF CSV
