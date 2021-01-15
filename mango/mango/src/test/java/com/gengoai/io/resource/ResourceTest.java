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

import com.gengoai.io.Resources;
import org.junit.Test;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class ResourceTest {

   @Test
   public void resourceCreationTest() {
      Resource r = Resources.from("classpath:com.gengoai");
      assertTrue(r instanceof ClasspathResource);

      r = Resources.from("file:This is a test");
      assertTrue(r instanceof FileResource);

      r = Resources.from(
         "http[userAgent=Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2, connectionTimeOut=1000]://www.yahoo.com");
      assertTrue(r instanceof URLResource);
      assertEquals("http://www.yahoo.com", r.toString());

      r = Resources.from("string:Now is the world");
      assertTrue(r instanceof StringResource);

      r = Resources.from("bytes[isCompressed=true, charset=UTF-8]:");
      assertTrue(r instanceof ByteArrayResource);
      assertTrue(r.isCompressed());
      assertTrue(r.getCharset() == StandardCharsets.UTF_8);
   }

   @Test
   public void testStd() throws Exception {

      InputStream stdin = System.in;
      System.setIn(new StringResource("This is not the end.").inputStream());
      Resource r = Resources.fromStdin();
      assertTrue(r.exists());
      assertEquals("This is not the end.", r.readToString().trim());
      System.setIn(stdin);


      PrintStream stdout = System.out;
      StringResource out = new StringResource();
      System.setOut(new PrintStream(out.outputStream()));
      Resources.fromStdout().write("This is not the end.");
      assertTrue(Resources.fromStdout().exists());
      assertEquals("This is not the end.", out.readToString().trim());
      System.setOut(stdout);


   }

   @Test
   public void testStreams() throws Exception {

      Resource r = Resources.fromString("");
      Resource r2 = Resources.fromOutputStream(r.outputStream());
      assertTrue(r2.exists());
      r2.write("This is a test");


      Resource r3 = Resources.fromInputStream(r.inputStream());
      assertTrue(r3.exists());
      assertEquals("This is a test", r3.readToString().trim());

   }

   @Test
   public void testClassPath() throws Exception {
      List<Resource> classpath = Resources.fromClasspath("com/").getChildren();
//    assertTrue(classpath.size() > 0);
   }


}
