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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author David B. Bracewell
 */
public class StringResourceTest {

  @Test
  public void test() throws Exception {
    Resource r = Resources.from("string:This is a test");
    assertTrue(r instanceof StringResource);
    assertTrue(r.exists());
    assertEquals("This is a test", r.readToString().trim());

    r = new StringResource();
    r.write("This is a test");
    assertEquals("This is a test", r.readToString().trim());

    r.append("\nNow we are here");
    assertEquals("This is a test\nNow we are here", r.readToString().trim());

    r = new StringResource("a");
    Resource r2 = new StringResource("a");
    assertEquals(r, r2);

  }

}
