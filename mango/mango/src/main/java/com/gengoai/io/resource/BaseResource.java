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

import com.gengoai.Validation;
import com.gengoai.io.CompressedInputStream;
import com.gengoai.io.Compression;
import com.gengoai.string.Strings;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Abstract base resource
 *
 * @author David B. Bracewell
 */
public abstract class BaseResource implements Resource, Serializable {
   private static final long serialVersionUID = 1L;
   private String charset = StandardCharsets.UTF_8.name();
   private Compression compression = Compression.NONE;

   @Override
   public Optional<File> asFile() {
      return Optional.empty();
   }

   @Override
   public Optional<URI> asURI() {
      return Optional.empty();
   }

   @Override
   public String baseName() {
      return Strings.EMPTY;
   }

   @Override
   public boolean canRead() {
      return true;
   }

   @Override
   public boolean canWrite() {
      return true;
   }

   @Override
   public final Resource compressed() {
      return setIsCompressed(true);
   }

   /**
    * Create input stream input stream.
    *
    * @return the input stream
    * @throws IOException the io exception
    */
   protected InputStream createInputStream() throws IOException {
      if(asFile().isPresent()) {
         return new FileInputStream(asFile().orElseThrow(NullPointerException::new));
      }
      throw new UnsupportedOperationException();
   }

   /**
    * Create output stream output stream.
    *
    * @return the output stream
    * @throws IOException the io exception
    */
   protected OutputStream createOutputStream() throws IOException {
      if(asFile().isPresent()) {
         return new FileOutputStream(asFile().orElseThrow(NullPointerException::new));
      }
      throw new UnsupportedOperationException();
   }

   @Override
   public String descriptor() {
      return super.toString();
   }

   @Override
   public final Charset getCharset() {
      if(charset == null) {
         return StandardCharsets.UTF_8;
      }
      return Charset.forName(charset);
   }

   @Override
   public InputStream inputStream() throws IOException {
      InputStream is = createInputStream();
      Validation.notNull(is, "Error creating inputStream for: " + descriptor());
      CompressedInputStream compressedInputStream = Compression.detect(is);
      setCompression(compressedInputStream.getCompression());
      return compressedInputStream;
   }

   @Override
   public final boolean isCompressed() {
      return compression != Compression.NONE;
   }

   @Override
   public boolean isDirectory() {
      return false;
   }

   @Override
   public OutputStream outputStream() throws IOException {
      Validation.checkState(canWrite(), "This is resource cannot be written to.");
      return compression.compressOutputStream(createOutputStream());
   }

   @Override
   public String path() {
      return Strings.EMPTY;
   }

   @Override
   public final Resource setCharset(Charset charset) {
      this.charset = charset.name();
      return this;
   }

   @Override
   public Resource setCompression(Compression compression) {
      this.compression = Validation.notNull(compression);
      return this;
   }

   @Override
   public final Resource setIsCompressed(boolean isCompressed) {
      if(isCompressed) {
         compression = Compression.GZIP;
      } else {
         compression = Compression.NONE;
      }
      return this;
   }

   @Override
   public final String toString() {
      return descriptor();
   }

   @Override
   public final Resource uncompressed() {
      return setIsCompressed(false);
   }

}//END OF BaseResource
