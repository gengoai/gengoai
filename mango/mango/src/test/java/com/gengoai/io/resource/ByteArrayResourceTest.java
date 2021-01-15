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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ByteArrayResourceTest {

  ByteArrayResource in;
  ByteArrayResource out = new ByteArrayResource();

  @Before
  public void setUp() throws Exception {
    in = new ByteArrayResource("ABC DEF GHI".getBytes());
  }

  @Test
  public void testRead() throws Exception {
    assertEquals("ABC DEF GHI", in.readToString().trim());
  }

  @Test
  public void testWrite() throws Exception {
    out.write("Writing to a byte array with a string");
    assertEquals("Writing to a byte array with a string", out.readToString().trim());
  }

  @Test
  public void testCanRead() throws Exception {
    assertTrue(out.canRead());
  }

  @Test
  public void testGetResourceName() throws Exception {
    assertTrue(out.descriptor().startsWith("com.gengoai.io.resource.ByteArrayResource@"));
  }

  @Test
  public void testCanWrite() throws Exception {
    assertTrue(out.canWrite());
  }

  @Test
  public void testExists() throws Exception {
    assertTrue(out.exists());
  }

}//END OF ByteArrayResourceTest