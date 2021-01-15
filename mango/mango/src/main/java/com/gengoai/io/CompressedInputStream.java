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

import java.io.IOException;
import java.io.InputStream;

import static com.gengoai.Validation.notNull;

/**
 * An InputStream implementation that is associated with a {@link Compression} algorithm for decompressing the incoming
 * bytes.
 *
 * @author David B. Bracewell
 */
public class CompressedInputStream extends InputStream {
   private final InputStream backing;
   private final Compression compression;

   /**
    * Instantiates a new CompressedInputStream.
    *
    * @param backing     the backing InputStream to decompress
    * @param compression the compression algorithm to use
    * @throws IOException Something went wrong wrapping the backing InputStream
    */
   public CompressedInputStream(InputStream backing, Compression compression) throws IOException {
      this.compression = notNull(compression, "Compression algorithm must not be null");
      this.backing = compression.decompressInputStream(notNull(backing, "Backing InputStream must not be null"));
   }

   @Override
   public void close() throws IOException {
      backing.close();
   }

   /**
    * Gets the Compression algorithm used.
    *
    * @return the compression
    */
   public Compression getCompression() {
      return compression;
   }

   @Override
   public int read() throws IOException {
      return backing.read();
   }

}//END OF CompressedInputStream
