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

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class FileUtilsTest {

  @Test
  public void testFileUtils() throws Exception {
    String unixPath = "/home/user/documents/";
    String unixPathNoTrailingSlash = "/home/user/documents";
    String windowsPath = "\\home\\user\\documents";
    String fileName = "/home/user/documents/picture.jpg";

    assertEquals(unixPath, FileUtils.toUnix(windowsPath) +"/");
    assertEquals(windowsPath+"\\", FileUtils.toWindows(unixPath));

    assertEquals(windowsPath + "\\", FileUtils.directory(windowsPath));
    assertEquals(unixPath, FileUtils.directory(unixPath));
    assertEquals(unixPath, FileUtils.directory(fileName));

    assertEquals("documents", FileUtils.baseName(windowsPath));
    assertEquals("documents", FileUtils.baseName(unixPath));

    assertEquals("picture.jpg", FileUtils.baseName(fileName));
    assertEquals("picture", FileUtils.baseName(fileName, ".jpg"));

    assertEquals("jpg", FileUtils.extension(fileName));

  }
}//END OF FileUtilsTest
