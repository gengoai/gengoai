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

import org.junit.Test;

import java.io.File;
import java.net.URI;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class URIResourceTest {

   @Test
   public void testReadingFromWeb() throws Exception {
      URIResource r = new URIResource(new URI("http://www.google.com"));
      assertTrue(r.readToString().length() > 0);
      assertTrue(r.exists());

      URIResource r2 = new URIResource(new URI("http://www.yahoo.com"));
      assertNotEquals(r, r2);
      Resource child = r2.getChild("news");

      assertEquals("/news", child.path());

      URIResource r3 = new URIResource(new URI("http://www.google.com"));
      assertEquals(r, r3);
      assertEquals(r.asURL(), r3.asURL());

   }

   @Test
   public void testAsFile() throws Exception {
      URIResource r = new URIResource(new URI("file:///home/user/"));
      assertEquals(new File("/home/user"), r.asFile().orElse(null));
   }

}//END OF URLResourceTest
