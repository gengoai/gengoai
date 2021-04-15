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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>A reader that understands Unicode Byte Order Marks and can also guess the character set if a BOM is not present
 * and a character set has not been specified.</p>
 *
 * @author David B. Bracewell
 */
public class CharsetDetectingReader extends Reader {

   private final static int BUFFER_SIZE = 8000;

   private final InputStream inStream;
   private Reader backing = null;
   private final Charset defaultCharset;
   private Charset charset = null;

   /**
    * Instantiates a new Espresso reader.
    *
    * @param inStream the in stream
    */
   public CharsetDetectingReader(InputStream inStream) {
      this(inStream, null);
   }

   /**
    * Instantiates a new Espresso reader.
    *
    * @param inStream       the in stream
    * @param defaultCharset the default charset
    */
   public CharsetDetectingReader(InputStream inStream, Charset defaultCharset) {
      this.inStream = inStream;
      this.defaultCharset = defaultCharset;
      this.charset = null;
   }

   private void determineCharset() throws IOException {
      PushbackInputStream pbIStream = new PushbackInputStream(inStream, BUFFER_SIZE);

      byte[] buffer = new byte[BUFFER_SIZE];
      int read = pbIStream.read(buffer, 0, buffer.length);
      if(read == -1) {
         return;
      }

      int bomOffset = 0;

      for(CommonBOM bom : CommonBOM.values()) {
         if(bom.matches(buffer)) {
            charset = bom.getCharset();
            bomOffset = bom.length();
            break;
         }
      }

      if(charset == null && defaultCharset == null) {
         charset = CharsetDetector.detect(buffer, 0, read);
         if(charset == null) {
            charset = StandardCharsets.UTF_8;
         }
      } else if(charset == null) {
         charset = defaultCharset;
      }

      pbIStream.unread(buffer, bomOffset, read - bomOffset);

      backing = new InputStreamReader(pbIStream, charset);
   }

   @Override
   public int read(char[] cbuf, int off, int len) throws IOException {
      if(backing == null) {
         determineCharset();
      }
      if(backing == null) {
         return -1;
      }
      return backing.read(cbuf, off, len);
   }

   @Override
   public void close() throws IOException {
      if(backing != null) {
         backing.close();
      }
   }

   /**
    * Gets charset.
    *
    * @return the charset
    */
   public Charset getCharset() {
      return charset;
   }

}//END OF CharsetDetectingReader
