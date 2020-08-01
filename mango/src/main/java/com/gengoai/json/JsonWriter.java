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
package com.gengoai.json;

import java.io.Closeable;
import java.io.IOException;

/**
 * The type JSON writer.
 *
 * @author David B. Bracewell
 */
public final class JsonWriter implements AutoCloseable, Closeable {
   @Override
   public void close() throws IOException {

   }
   //   private final com.google.gson.stream.JsonWriter writer;
//   private boolean isArray;
//
//   /**
//    * Instantiates a new JSON writer.
//    *
//    * @param resource the resource
//    * @throws IOException the structured iO exception
//    */
//   public JsonWriter(Resource resource) throws IOException {
//      this.writer = new com.google.gson.stream.JsonWriter(resource.writer());
//   }
//
//   /**
//    * Begins a new array
//    *
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter beginArray() throws IOException {
//      writer.beginArray();
//      return this;
//   }
//
//   /**
//    * Begins a new array with given name
//    *
//    * @param arrayName the name
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter beginArray(String arrayName) throws IOException {
//      name(arrayName);
//      writer.beginArray();
//      return this;
//   }
//
//   /**
//    * Begin document as an object structure
//    *
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter beginDocument() throws IOException {
//      return beginDocument(false);
//   }
//
//   /**
//    * Begin document
//    *
//    * @param isArray True the document is an array structure, false is an object structure
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter beginDocument(boolean isArray) throws IOException {
//      this.isArray = isArray;
//      if (isArray) {
//         writer.beginArray();
//      } else {
//         writer.beginObject();
//      }
//      return this;
//   }
//
//   /**
//    * Begins a new object
//    *
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter beginObject() throws IOException {
//      writer.beginObject();
//      return this;
//   }
//
//   /**
//    * Begins a new object with a given name
//    *
//    * @param objectName the name
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter beginObject(String objectName) throws IOException {
//      name(objectName);
//      writer.beginObject();
//      return this;
//   }
//
//   @Override
//   public void close() throws IOException {
//      writer.close();
//   }
//
//   /**
//    * Ends the current array
//    *
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter endArray() throws IOException {
//      writer.endArray();
//      return this;
//   }
//
//   /**
//    * End document.
//    *
//    * @throws IOException Something went wrong writing
//    */
//   public void endDocument() throws IOException {
//      if (isArray) {
//         writer.endArray();
//      } else {
//         writer.endObject();
//      }
//   }
//
//   /**
//    * Ends the current object
//    *
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter endObject() throws IOException {
//      writer.endObject();
//      return this;
//   }
//
//   /**
//    * Flushes the writer.
//    *
//    * @throws IOException Something went wrong writing
//    */
//   public void flush() throws IOException {
//      writer.flush();
//   }
//
//   public void name(String name) throws IOException {
//      writer.name(name);
//   }
//
//   /**
//    * Writes a  null value
//    *
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter nullValue() throws IOException {
//      writer.nullValue();
//      return this;
//   }
//
//   /**
//    * Writes a  key value pair
//    *
//    * @param key   the key
//    * @param value the value
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter property(String key, Object value) throws IOException {
//      name(key);
//      value(value);
//      return this;
//   }
//
//   public JsonWriter setHtmlSafe(boolean htmlSafe) {
//      writer.setHtmlSafe(htmlSafe);
//      return this;
//   }
//
//   public JsonWriter setLenient(boolean isLenient) {
//      writer.setLenient(isLenient);
//      return this;
//   }
//
//   /**
//    * Sets the output to indented by some number of spaces
//    *
//    * @param numberOfSpaces The number of spaces to indent by
//    * @return This JSONWriter
//    */
//   public JsonWriter spaceIndent(int numberOfSpaces) {
//      if (numberOfSpaces >= 0) {
//         writer.setIndent(Strings.repeat(' ', numberOfSpaces));
//      }
//      return this;
//   }
//
//   /**
//    * Sets the output to be indented by a tab
//    *
//    * @return This JSONWriter
//    */
//   public JsonWriter tabIndent() {
//      writer.setIndent("\t");
//      return this;
//   }
//
//
//   /**
//    * Writes an array value
//    *
//    * @param value the value
//    * @return This structured writer
//    * @throws IOException Something went wrong writing
//    */
//   public JsonWriter value(Object value) throws IOException {
//      if (value == null) {
//         writer.nullValue();
//      } else if (value instanceof JsonElement) {
//         write(Cast.as(value));
//      } else if (value instanceof Double) {
//         writer.value(Cast.<Double>as(value));
//      } else if (value instanceof Long) {
//         writer.value(Cast.<Long>as(value));
//      } else if (value instanceof Number) {
//         writer.value(Cast.<Number>as(value));
//      } else if (value instanceof CharSequence) {
//         writer.value(value.toString());
//      } else if (value instanceof Boolean) {
//         writer.value(Cast.<Boolean>as(value));
//      } else if (value instanceof Enum) {
//         writer.value(Cast.<Enum>as(value).name());
//      } else if (value instanceof EnumValue) {
//         writer.value(Cast.<EnumValue>as(value).name());
//      } else if (value instanceof Iterable) {
//         writer.beginArray();
//         Iterable<?> iterable = Cast.as(value);
//         for (Object o : iterable) {
//            value(o);
//         }
//         writer.endArray();
//      } else if (value instanceof Iterator) {
//         value(Iterables.asIterable(Cast.as(value)));
//      } else {
//         write(JsonEntry.from(value));
//      }
//      return this;
//   }
//
//   public JsonWriter write(JsonEntry element) throws IOException {
//      Streams.write(element.getElement(), writer);
//      return this;
//   }

}//END OF JSONWriter