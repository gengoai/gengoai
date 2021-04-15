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

import com.gengoai.Validation;
import org.mozilla.universalchardet.UniversalDetector;

import java.nio.charset.Charset;

/**
 * Convenience method for detecting the character set of a byte array.
 *
 * @author David B. Bracewell
 */
public class CharsetDetector {

   /**
    * Detects the character set for the buffer.
    *
    * @param buffer The buffer
    * @param offset where to start
    * @param length the length to read
    * @return The detected charset or null
    */
   public static Charset detect(byte[] buffer, int offset, int length) {
      Validation.checkArgument(length > 0);
      Validation.checkArgument(offset >= 0);

      final UniversalDetector detector = new UniversalDetector(null);
      try {
         detector.handleData(buffer, offset, length);
         detector.dataEnd();
         return Charset.forName(detector.getDetectedCharset());
      } catch(Exception e) {
         return null;
      }
   }

}// END OF CharsetDetector
