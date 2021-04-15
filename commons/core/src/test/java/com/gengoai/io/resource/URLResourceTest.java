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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class URLResourceTest {

  @Test
  public void testReadingFromWeb() throws Exception {
    URLResource r = new URLResource("http://www.google.com");
    assertTrue(r.readToString().length() > 0);
    assertTrue(r.exists());

    URLResource r2 = new URLResource("http://www.yahoo.com");
    assertNotEquals(r, r2);
    Resource child = r2.getChild("news");

    assertEquals("/news", child.path());

    URLResource r3 = new URLResource("http://www.google.com");
    assertEquals(r, r3);
    assertEquals(r.asURL(), r3.asURL());

  }

  @Test(expected = MalformedURLException.class)
  public void testURLError() throws Exception {
    new URLResource("arg matey");
  }

  @Test
  public void testGetChildURLError() throws Exception {
    URLResource r2 = new URLResource("http://www.yahoo.com");
    assertTrue(r2.getChild("bad://news") instanceof EmptyResource);
  }

  @Test
  public void testAsFile() throws Exception {
    URLResource url = Resources.fromUrl(new URL("file:///home/david"));
    assertEquals(new File("/home/david"), url.asFile().get());
  }

}//END OF URLResourceTest
