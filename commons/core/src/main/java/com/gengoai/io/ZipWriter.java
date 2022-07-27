/*
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

import com.gengoai.io.resource.ByteArrayResource;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author David B. Bracewell
 */
public class ZipWriter implements AutoCloseable {
   private final ZipOutputStream outputStream;
   @Getter
   private final String base;

   public ZipWriter(@NonNull OutputStream outputStream) {
      this.outputStream = new ZipOutputStream(outputStream);
      this.base = "";
   }

   private ZipWriter(ZipOutputStream outputStream, String base) {
      this.outputStream = outputStream;
      this.base = Strings.appendIfNotPresent(base, "/");
   }

   public ZipWriter addDirectory(@NonNull String directoryPath) throws IOException {
      ZipEntry zipEntry = new ZipEntry(base + Strings.appendIfNotPresent(directoryPath, "/"));
      outputStream.putNextEntry(zipEntry);
      outputStream.closeEntry();
      return new ZipWriter(outputStream, zipEntry.getName());
   }

   public void addEntry(@NonNull String path, @NonNull Resource entry) throws IOException {
      ZipEntry zipEntry = new ZipEntry(base + path);
      outputStream.putNextEntry(zipEntry);
      outputStream.write(entry.readBytes());
      outputStream.closeEntry();
   }

   public void addEntry(@NonNull String path, @NonNull String string) throws IOException {
      addEntry(path, Resources.fromString(string));
   }

   public void addEntry(@NonNull String path, @NonNull Object entry) throws IOException {
      Resource byteArray = new ByteArrayResource();
      try {
         byteArray.writeObject(entry);
      } catch (Exception e) {
         throw new IOException(e);
      }
      addEntry(path, byteArray);
   }

   @Override
   public void close() throws IOException {
      outputStream.close();
   }
}//END OF ZipWriter
