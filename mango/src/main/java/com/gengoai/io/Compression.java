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
 *
 */

package com.gengoai.io;

import com.gengoai.Validation;
import lombok.NonNull;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.gengoai.Validation.notNull;

/**
 * Methodologies for compressing and decompressing InputStream and OutputStream
 *
 * @author David B. Bracewell
 */
public enum Compression {
   /**
    * No Compression
    */
   NONE(new byte[]{}) {
      @Override
      public InputStream decompressInputStream(InputStream inputStream) {
         return notNull(inputStream);
      }

      @Override
      public OutputStream compressOutputStream(OutputStream outputStream) {
         return notNull(outputStream);
      }
   },
   ZIP(new byte[]{0x50, 0x4B}) {
      @Override
      public OutputStream compressOutputStream(@NonNull OutputStream outputStream) throws IOException {
         return outputStream;
      }

      @Override
      public InputStream decompressInputStream(@NonNull InputStream inputStream) throws IOException {
         return inputStream;
      }
   },
   /**
    * GZIP Compression
    */
   GZIP(new byte[]{0x1F, (byte) 0x8B}) {
      @Override
      public InputStream decompressInputStream(InputStream inputStream) throws IOException {
         return new GZIPInputStream(notNull(inputStream));
      }

      @Override
      public OutputStream compressOutputStream(OutputStream outputStream) throws IOException {
         return new GZIPOutputStream(notNull(outputStream));
      }
   },
   /**
    * BZIP2 Compression
    */
   BZIP2(new byte[]{0x42, 0x5A, 0x68}) {
      @Override
      public InputStream decompressInputStream(InputStream inputStream) throws IOException {
         return new BZip2CompressorInputStream(notNull(inputStream));
      }

      @Override
      public OutputStream compressOutputStream(OutputStream outputStream) throws IOException {
         return new BZip2CompressorOutputStream(notNull(outputStream));
      }
   },
   XZ(new byte[]{-3, 55, 122, 88, 90, 0}) {
      @Override
      public OutputStream compressOutputStream(OutputStream outputStream) throws IOException {
         return new XZCompressorOutputStream(outputStream);
      }

      @Override
      public InputStream decompressInputStream(InputStream inputStream) throws IOException {
         return new XZCompressorInputStream(inputStream);
      }
   };

   private static final int LONGEST_MAGIC_NUMBER = 6;
   private final byte[] header;

   Compression(byte[] header) {
      this.header = header;
   }

   /**
    * Detects the compression of the given InputStream returning a {@link CompressedInputStream} that allows for reading
    * from the compressed stream.
    *
    * @param is the InputStream to read from
    * @return the {@link CompressedInputStream}
    * @throws IOException Something went wrong detecting the compression
    */
   public static CompressedInputStream detect(InputStream is) throws IOException {
      Validation.notNull(is);
      byte[] buffer = new byte[LONGEST_MAGIC_NUMBER];
      PushbackInputStream pushbackInputStream = new PushbackInputStream(is, LONGEST_MAGIC_NUMBER);
      int read = pushbackInputStream.read(buffer);
      if (read == -1) {
         return new CompressedInputStream(is, NONE);
      }
      pushbackInputStream.unread(buffer, 0, read);
      for (Compression value : values()) {
         if (value != NONE && matches(value.header, buffer)) {
            return new CompressedInputStream(pushbackInputStream, value);
         }
      }
      return new CompressedInputStream(pushbackInputStream, NONE);
   }

   public static Compression detectCompression(@NonNull InputStream is) throws IOException {
      Validation.notNull(is);
      byte[] buffer = new byte[LONGEST_MAGIC_NUMBER];
      PushbackInputStream pushbackInputStream = new PushbackInputStream(is, LONGEST_MAGIC_NUMBER);
      int read = pushbackInputStream.read(buffer);
      if (read == -1) {
         return Compression.NONE;
      }
      pushbackInputStream.unread(buffer, 0, read);
      for (Compression value : values()) {
         if (value != NONE && matches(value.header, buffer)) {
            return value;
         }
      }
      return NONE;
   }

   private static boolean matches(byte[] a1, byte[] a2) {
      if (a1.length > a2.length) {
         return false;
      }
      for (int i = 0; i < a1.length; i++) {
         if (a1[i] != a2[i]) {
            return false;
         }
      }
      return true;
   }

   /**
    * Compress the given OutputStream.
    *
    * @param outputStream the OutputStream to compress
    * @return An OutputStream that will compress its content using the algorithm associated with this {@link
    * Compression} algorithm
    * @throws IOException Something went wrong wrapping the output stream
    */
   public abstract OutputStream compressOutputStream(OutputStream outputStream) throws IOException;

   /**
    * Decompresses the given InputStream.
    *
    * @param inputStream the InputStream to decompress
    * @return An InputStream that will decompress its content using the algorithm associated with this {@link
    * Compression} algorithm
    * @throws IOException Something went wrong wrapping the input stream
    */
   public abstract InputStream decompressInputStream(InputStream inputStream) throws IOException;

}//END OF Compression
