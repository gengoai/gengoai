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
import com.gengoai.string.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class FileResourceTest {

   Resource tempDir;

   @Before
   public void setUp() throws Exception {
      tempDir = Resources.fromFile(Resources.temporaryDirectory().asFile().orElse(null));
   }


   @Test
   public void testAsFile() throws Exception {
      File tempFile = Resources.temporaryDirectory().asFile().orElse(null);
      Assert.assertEquals(tempFile, Resources.fromFile(tempFile).asFile().get());
   }

   @Test
   public void testAsURL() throws Exception {
      File tempFile = Resources.temporaryDirectory().asFile().orElse(null);
      Assert.assertEquals(tempFile.toURI().toURL(), Resources.fromFile(tempFile).asURL().get());
   }

   @Test
   public void testCanReadAndWrite() throws Exception {
      tempDir.delete(true);
      assertFalse(tempDir.canRead());
      assertTrue(tempDir.canWrite());


      tempDir.mkdirs();
      assertTrue(tempDir.canRead());
      assertTrue(tempDir.canWrite());
   }


   @Test
   public void testGetChildren() throws Exception {
      tempDir.mkdirs();
      tempDir.getChild("one").mkdirs();
      tempDir.getChild("only").mkdirs();
      tempDir.getChild("two").mkdirs();
      tempDir.getChild("three").mkdirs();


      assertEquals(4, tempDir.getChildren().size());
      assertEquals(2, tempDir.getChildren("on*").size());

      tempDir.delete(true);
   }

   @Test
   public void testFileNaming() throws Exception {
      Resource file = Resources.fromFile("/home/test/test.txt");
      assertEquals("test.txt", file.baseName());
      Assert.assertEquals(Resources.fromFile("/home/test"), file.getParent());
      assertEquals("/home/test/test.txt", file.path());
      assertEquals("file:/home/test/test.txt", file.descriptor());
   }

   @Test
   public void testReadWriteAppend() throws Exception {
      tempDir.mkdirs();
      Resource tempFile = tempDir.getChild(Strings.randomHexString(10));
      tempFile.write("This is output");
      assertEquals("This is output", tempFile.readToString().trim());

      tempFile.append(". This a second");
      assertEquals("This is output. This a second", tempFile.readToString().trim());

      tempFile.append("\nSecond Line");

      int i = 0;
      for (String line : tempFile.readLines()) {
         if (i == 0) {
            assertEquals("This is output. This a second", line);
         } else {
            assertEquals("Second Line", line.trim());
         }
         i++;
      }


   }


   @Test
   public void testMkdirsAndDelete() throws Exception {
      //Mkdirs
      tempDir.mkdirs();
      assertTrue(tempDir.exists());

      assertTrue(tempDir.isDirectory());

      //Mkdir with child
      Resource tempChild = tempDir.getChild(Strings.randomHexString(10));
      tempChild.mkdir();
      assertTrue(tempChild.exists());

      assertTrue(tempChild.delete());

      //Delete recursively
      tempDir.delete(true);
      assertFalse(tempDir.exists());
   }

}//END OF FileResourceTest