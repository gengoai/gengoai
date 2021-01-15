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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Enumeration of common Unicode Byte Order Marks
 *
 * @author David B. Bracewell
 */
public enum CommonBOM {
   /**
    * The UTF_8 BOM
    */
   UTF_8(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, StandardCharsets.UTF_8),
   /**
    * The UTF_16BE BOM.
    */
   UTF_16BE(new byte[]{(byte) 0xFE, (byte) 0xFF}, StandardCharsets.UTF_16BE),
   /**
    * The UTF_16LE BOM.
    */
   UTF_16LE(new byte[]{(byte) 0xFF, (byte) 0xFE}, StandardCharsets.UTF_16LE),
   /**
    * The UTF_32BE BOM.
    */
   UTF_32BE(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF}, Charset.forName("UTF-32BE")),
   /**
    * The UTF_32LE BOM.
    */
   UTF_32LE(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFE}, Charset.forName("UTF-32LE"));

   /**
    * The constant MAX_BOM_SIZE.
    */
   public final static int MAX_BOM_SIZE;

   //Determine the max BOM size based on the current items in the enum
   static {
      int max = 0;
      for(CommonBOM bom : CommonBOM.values()) {
         if(bom.length() > max) {
            max = bom.length();
         }
      }
      MAX_BOM_SIZE = max;
   }

   private final byte[] bom;
   private final Charset charset;

   CommonBOM(byte[] bom, Charset charset) {
      this.bom = bom;
      this.charset = charset;
   }

   /**
    * The number of byhtes in the bom
    *
    * @return The BOM length
    */
   public int length() {
      return bom.length;
   }

   /**
    * The charset associated with the bom
    *
    * @return The associated <code>Charset</code>
    */
   public Charset getCharset() {
      return charset;
   }

   /**
    * Determines if the BOM is at the beginning of a given byte array.
    *
    * @param rhs The byte array to compare against
    * @return True if the BOM is present, false otherwise
    */
   public boolean matches(byte[] rhs) {
      if(rhs == null || rhs.length < bom.length) {
         return false;
      }
      for(int i = 0; i < bom.length; i++) {
         if(bom[i] != rhs[i]) {
            return false;
         }
      }
      return true;
   }

}// END OF CommonBOM
