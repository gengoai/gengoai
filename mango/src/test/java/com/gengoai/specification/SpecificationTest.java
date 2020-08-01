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
 *
 */

package com.gengoai.specification;

import com.gengoai.config.Config;
import com.gengoai.string.Strings;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class SpecificationTest {

   @Test
   public void testMinimal() {
      Specification specification = Specification.parse("schema:protocol:subProtocol0::~/main/path");
      assertEquals("schema", specification.getSchema());
      assertEquals("protocol", specification.getProtocol());
      assertEquals("subProtocol0", specification.getSubProtocol(0));
      assertEquals("~/main/path", specification.getPath());
      assertTrue(specification.getAllQueryValues("empty").isEmpty());
      assertEquals("a", specification.getQueryValue("empty", "a"));
      assertTrue(specification.getQueryParameters().isEmpty());

      assertEquals("schema:protocol:subProtocol0::~/main/path", specification.toString());
   }


   @Test
   public void testNoPath() {
      Specification specification = Specification.parse("schema:protocol:subProtocol0");
      assertEquals("schema", specification.getSchema());
      assertEquals("protocol", specification.getProtocol());
      assertEquals("subProtocol0", specification.getSubProtocol(0));
      assertNull(specification.getPath());
      assertTrue(specification.getAllQueryValues("empty").isEmpty());
      assertEquals("a", specification.getQueryValue("empty", "a"));
      assertTrue(specification.getQueryParameters().isEmpty());

      assertEquals("schema:protocol:subProtocol0", specification.toString());
   }

   @Test
   public void testResolve() {
      Config.setProperty("RESOURCES", "/data/resources");
      Specification specification = Specification.parse("schema:protocol:subProtocol0::${RESOURCES}/test;p=a;v = c");
      assertEquals("/data/resources/test", specification.getPath());
   }

   @Test
   public void testQueries() {
      Specification specification = Specification.parse("schema:protocol:subProtocol0::~/main/path;p=a;v = c");
      assertEquals("schema", specification.getSchema());
      assertEquals("protocol", specification.getProtocol());
      assertEquals("subProtocol0", specification.getSubProtocol(0));
      assertEquals("~/main/path", specification.getPath());
      assertEquals(1, specification.getAllQueryValues("p").size());
      assertEquals("a", specification.getAllQueryValues("p").get(0));
      assertEquals("a", specification.getQueryValue("p", "b"));
      assertEquals(1, specification.getAllQueryValues("v").size());
      assertEquals("c", specification.getQueryValue("v", "d").strip());
   }

   @Test
   public void testNoPathQueries() {
      Specification specification = Specification.parse("schema:protocol:subProtocol0;p=a;v = c");
      assertEquals("schema", specification.getSchema());
      assertEquals("protocol", specification.getProtocol());
      assertEquals("subProtocol0", specification.getSubProtocol(0));
      assertNull(specification.getPath());
      assertEquals(1, specification.getAllQueryValues("p").size());
      assertEquals("a", specification.getAllQueryValues("p").get(0));
      assertEquals("a", specification.getQueryValue("p", "b"));
      assertEquals(1, specification.getAllQueryValues("v").size());
      assertEquals("c", specification.getQueryValue("v", "d").strip());
   }

   @Test
   public void testSchemaOnly() {
      Specification specification = Specification.parse("schema:");
      assertEquals("schema", specification.getSchema());
      assertTrue(Strings.isNullOrBlank(specification.getProtocol()));
      assertTrue(Strings.isNullOrBlank(specification.getSubProtocol(0)));
      assertTrue(Strings.isNullOrBlank(specification.getPath()));
      assertTrue(specification.getAllQueryValues("empty").isEmpty());
      assertTrue(specification.getQueryParameters().isEmpty());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testInvalidProtocol() {
      Specification.parse("schema:&$@");
   }

}