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

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CharsetDetectingReaderTest {

  @Test
  public void testUTF16_LE() throws Exception {
    assertEquals("안녕하세요", read("/com/gengoai//io/UTF16_LE.txt"));
  }


  @Test
  public void testUTF16_BE() throws Exception {
    assertEquals("안녕하세요", read("/com/gengoai//io/UTF16_BE.txt"));
  }

  @Test
  public void testUTF8() throws Exception {
    assertEquals("안녕하세요", read("/com/gengoai//io/UTF8.txt"));
  }

  @Test
  public void testAutoDetect() throws Exception {
    assertEquals("今日は、僕の名前はデービットです。", read("/com/gengoai//io/SHIFT_JIS.txt"));
  }

  public String read(String resourceName) throws IOException {
    try (InputStream stream = CharsetDetectingReaderTest.class.getResourceAsStream(resourceName);
         BufferedReader reader = new BufferedReader(new CharsetDetectingReader(stream))
    ) {
      return reader.lines().collect(Collectors.joining("\n")).trim();
    }
  }


}//END OF EspressoReaderTest