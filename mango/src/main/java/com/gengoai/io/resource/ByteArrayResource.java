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

package com.gengoai.io.resource;

import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;

import java.io.*;
import java.util.Objects;

/**
 * A Resource that wraps a byte array.
 *
 * @author David B. Bracewell
 */
public class ByteArrayResource extends BaseResource implements NonTraversableResource {
   private static final long serialVersionUID = 9152033221857665242L;
   private final ByteArrayOutputStream buffer;

   /**
    * Instantiates a new Byte array resource.
    */
   public ByteArrayResource() {
      this.buffer = new ByteArrayOutputStream();
   }

   /**
    * Instantiates a new Byte array resource.
    *
    * @param b an initial byte array
    */
   public ByteArrayResource(byte[] b) {
      this(b, 0, b.length);
   }

   /**
    * Instantiates a new Byte array resource.
    *
    * @param b      an initial byte array
    * @param offset the offset into the byte array
    * @param len    the number of bytes to copy
    */
   public ByteArrayResource(byte[] b, int offset, int len) {
      this.buffer = new ByteArrayOutputStream();
      this.buffer.write(b, offset, len);
   }

   @Override
   public Resource append(byte[] byteArray) throws IOException {
      buffer.write(byteArray);
      return this;
   }

   @Override
   public boolean exists() {
      return true;
   }

   @Override
   public boolean equals(Object o) {
      if(this == o) return true;
      if(!(o instanceof ByteArrayResource)) return false;
      ByteArrayResource that = (ByteArrayResource) o;
      return Objects.equals(buffer, that.buffer);
   }

   @Override
   public int hashCode() {
      return Objects.hash(buffer);
   }

   @Override
   public MStream<String> lines() throws IOException {
      return StreamingContext.local().stream(buffer.toString(getCharset().name()).split("\r?\n"));
   }

   @Override
   protected InputStream createInputStream() throws IOException {
      return new ByteArrayInputStream(buffer.toByteArray());
   }

   @Override
   protected OutputStream createOutputStream() throws IOException {
      return buffer;
   }

}//END OF ByteArrayResource
