package com.gengoai;/*
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

import com.gengoai.io.resource.ByteArrayResource;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import org.junit.Test;

import static org.junit.Assert.*;

public class DynamicEnumTest {

   public static final NamesEnum WITH_SPACE = NamesEnum.make("WITH spAce");

   @Test(expected = IllegalArgumentException.class)
   public void testBadBlank() throws Exception {
      NamesEnum.make("   ");
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadEmpty() throws Exception {
      NamesEnum.make("");
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadPeriod() throws Exception {
      NamesEnum.make(".");
   }

   @Test
   public void testCompare() throws Exception {
      assertTrue(WITH_SPACE.compareTo(NamesEnum.make("ZEBRA")) < 0);
   }

   @Test
   public void testIsInstance() throws Exception {
      assertTrue(WITH_SPACE.isInstance(WITH_SPACE));
      assertFalse(WITH_SPACE.isInstance(NamesEnum.make("NOT A SPACE")));
   }

   @Test
   public void testJson() throws Exception {
      NamesEnum name = NamesEnum.make("name");
      String json = Json.dumps(name);
      NamesEnum nameDes = Json.parse(json, NamesEnum.class);
      assertEquals(name, nameDes);
   }

   @Test
   public void testName() throws Exception {
      assertEquals("WITH_SPACE", WITH_SPACE.name());
      assertEquals(NamesEnum.class.getCanonicalName() + ".WITH_SPACE", WITH_SPACE.canonicalName());
   }

   @Test
   public void testReferenceEquality() throws Exception {
      assertSame(WITH_SPACE, NamesEnum.make("with space"));
      Resource bytes = new ByteArrayResource();
      bytes.writeObject(WITH_SPACE);
      NamesEnum isItWhiteSpace = bytes.readObject();
      assertSame(WITH_SPACE, isItWhiteSpace);
      assertSame(WITH_SPACE, NamesEnum.make("with space"));
   }

   @Test
   public void testValues() throws Exception {
      assertTrue(NamesEnum.values().contains(WITH_SPACE));
   }

}