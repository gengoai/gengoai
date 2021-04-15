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

import com.gengoai.function.SerializableFunction;
import com.gengoai.function.Unchecked;
import com.gengoai.stream.Streams;
import com.gengoai.string.Strings;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Reads files in delimiter separated value format using a {@link CSV} specification.</p>
 *
 * @author David B. Bracewell
 */
public class CSVReader implements Closeable, AutoCloseable, Iterable<List<String>> {
   private static final int END_OF_ROW = 2;
   private static final int IN_FIELD = 3;
   private static final int IN_QUOTE = 1;
   private static final int OUT_QUOTE = 4;
   private static final int START = 0;
   private final Queue<Integer> buffer = new LinkedList<>();
   private final int comment;
   private final int delimiter;
   private final int escape;
   private final boolean keepEmptyCells;
   private final int quote;
   private final Reader reader;
   private int STATE = START;
   private StringBuilder cell = new StringBuilder();
   private List<String> row;
   private List<String> header;
   private boolean wasQuoted = false;

   /**
    * Instantiates a new CSVReader.
    *
    * @param builder the CSV specification
    * @param reader  the reader to read from
    * @throws IOException Something went wrong initializing the CSVReader
    */
   public CSVReader(CSV builder, Reader reader) throws IOException {
      this.delimiter = builder.getDelimiter();
      this.escape = builder.getEscape();
      this.quote = builder.getQuote();
      this.comment = builder.getComment();
      this.keepEmptyCells = builder.isKeepEmptyCells();
      this.reader = new BufferedReader(reader);
      this.header = builder.getHeader() == null
            ? Collections.emptyList()
            : builder.getHeader();
      if (builder.getHasHeader() && header.isEmpty()) {
         header = nextRow();
         row.clear();
      }
   }

   private void addCell(boolean isQuoted) {
      String cellString = cell.toString();
      if (STATE == IN_FIELD) {
         cellString = cellString.strip();
      }
      if (keepEmptyCells || !Strings.isNullOrBlank(cellString)) {
         if (cellString.length() > 0 && cellString.charAt(cellString.length() - 1) == escape) {
            cellString += " ";
         }
         String cellStr = cellString.replaceAll("\\\\(.)", "$1");
         row.add(isQuoted
                       ? cellStr
                       : cellStr.trim());
      }
      cell.setLength(0);
      wasQuoted = false;
   }

   private int beginOfLine(int c) throws IOException {
      if (c == comment) {
         readToEndOfLine();
         return START;
      } else if (c == quote) {
         wasQuoted = true;
         return IN_QUOTE;
      } else if (c == delimiter) {
         addCell(wasQuoted);
         return IN_FIELD;
      } else if (c == escape) {
         cell.append((char) escape).append(escape());
         return IN_FIELD;
      } else if (c == '\n') {
         return END_OF_ROW;
      } else if (!Character.isWhitespace(c)) {
         cell.append((char) c);
         return IN_FIELD;
      }
      gobbleWhiteSpace();
      return START;
   }

   private int bufferPeek() throws IOException {
      if (buffer.isEmpty()) {
         int next = reader.read();
         buffer.add(next);
      }
      return buffer.peek();
   }

   @Override
   public void close() throws IOException {
      reader.close();
   }

   private char escape() throws IOException {
      int c = reader.read();
      if (c == -1) {
         throw new IOException("Premature EOF");
      }
      return (char) c;
   }

   /**
    * Convenience method for consuming all rows in the CSV file
    *
    * @param consumer the consumer to use to process the rows
    */
   @Override
   public void forEach(Consumer<? super List<String>> consumer) {
      try (Stream<List<String>> stream = stream()) {
         stream.forEach(consumer);
      }
   }

   /**
    * Gets the header read in from the csv file or specified via the CSV specification.
    *
    * @return The header of the CSV file
    */
   public List<String> getHeader() {
      if (header == null) {
         return Collections.emptyList();
      }
      return Collections.unmodifiableList(header);
   }

   private void gobbleWhiteSpace() throws IOException {
      while (bufferPeek() != -1
            && Character.isWhitespace(bufferPeek())
            && bufferPeek() != '\r'
            && bufferPeek() != '\n') {
         read();
      }
   }

   private int inField(int c, boolean isQuoted) throws IOException {
      if (c == quote && isQuoted) {
         if (bufferPeek() == quote) {
            read();
         } else {
            return OUT_QUOTE;
         }
      } else if (c == quote && Strings.isNullOrBlank(cell.toString())) {
         return IN_QUOTE;
      } else if (c == delimiter && !isQuoted) {
         addCell(isQuoted);
         gobbleWhiteSpace();
         return START;
      } else if (c == escape) {
         cell.append((char) escape).append(escape());
         return isQuoted
               ? IN_QUOTE
               : IN_FIELD;
      } else if (c == '\r' && !isQuoted) {
         if (bufferPeek() == '\n') {
            read();
            return END_OF_ROW;
         }
      } else if (c == '\n' && !isQuoted) {
         return END_OF_ROW;
      }
      cell.append((char) c);
      return isQuoted
            ? IN_QUOTE
            : IN_FIELD;
   }

   @Override
   public Iterator<List<String>> iterator() {
      return new RowIterator();
   }

   /**
    * Reads the next row from the CSV file retuning null if there are not anymore
    *
    * @return the row as a list of strings or null if there are not any more rows to read
    * @throws IOException Something went wrong reading the file
    */
   public List<String> nextRow() throws IOException {
      row = new ArrayList<>();
      STATE = START;
      int c;
      int readCount = 0;
      gobbleWhiteSpace();
      while ((c = read()) != -1) {
         if (c == '\r') {
            if (bufferPeek() == '\n') {
               continue;
            } else {
               c = '\n';
            }
         }
         readCount++;
         switch (STATE) {
            case START:
               STATE = beginOfLine(c);
               break;
            case IN_QUOTE:
               wasQuoted = true;
               STATE = inField(c, true);
               break;
            case IN_FIELD:
               STATE = inField(c, false);
               break;
            case OUT_QUOTE:
               STATE = outQuote(c);
               break;
            default:
               throw new IOException("State [" + STATE + "]");
         }
         if (STATE == END_OF_ROW) {
            break;
         }
      }
      if (readCount > 0) {
         addCell(wasQuoted);
      }
      if (row.isEmpty()) {
         return null;
      }
      return new ArrayList<>(row);
   }

   private int outQuote(int c) throws IOException {
      if (c == '\n') {
         return END_OF_ROW;
      } else if (c == delimiter) {
         addCell(true);
         gobbleWhiteSpace();
         return IN_FIELD;
      } else if (Character.isWhitespace(c)) {
         gobbleWhiteSpace();
         return OUT_QUOTE;
      }
      System.out.println(cell);
      throw new IOException("Illegal character [" + (char) c + "] outside of the end quote of a cell.");
   }

   public <R> List<R> processRows(SerializableFunction<List<String>, Optional<R>> converter) throws IOException {
      List<String> row;
      List<R> rval = new ArrayList<>();
      try {
         while ((row = nextRow()) != null) {
            if (row.size() > 0) {
               Optional<R> r = converter.apply(row);
               r.ifPresent(rval::add);
            }
         }
      } finally {
         QuietIO.closeQuietly(this);
      }
      return rval;
   }

   private int read() throws IOException {
      if (buffer.isEmpty()) {
         return reader.read();
      }
      return buffer.remove();
   }

   /**
    * Runs the iterator over the entire resource returning all the rows it processed.
    *
    * @return the list of rows (lists)
    * @throws IOException the iO exception
    */
   public List<List<String>> readAll() throws IOException {
      return stream().collect(Collectors.toList());
   }

   private void readToEndOfLine() throws IOException {
      do {
         int c = reader.read();
         if (c == -1 || c == '\n') {
            return;
         }
      } while (true);
   }

   /**
    * Creates a new stream over the rows in the file that will close underlying reader on stream close.
    *
    * @return the stream of rows in the file
    */
   public Stream<List<String>> stream() {
      return Streams.asStream(new RowIterator()).onClose(Unchecked.runnable(this::close));
   }

   private class RowIterator implements Iterator<List<String>> {
      private List<String> row = null;

      private boolean advance() {
         if (row == null) {
            try {
               row = nextRow();
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         }
         return row != null;
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public List<String> next() {
         if (!advance()) {
            throw new NoSuchElementException();
         }
         List<String> c = row;
         row = null;
         return c;
      }

   }

}//END OF CSVReader
