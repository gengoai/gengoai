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

package com.gengoai;

import com.gengoai.collection.Lists;
import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.TypeUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class InternerTest {

   Interner<String> interner = new Interner<>();
   String A;
   String B;
   String C;

   @Before
   public void setUp() throws Exception {
      A = interner.intern("A");
      B = interner.intern("B");
      C = interner.intern("C");
   }

   @Test
   public void intern() throws Exception {
      assertTrue(A == interner.intern("A"));
      assertTrue(B == interner.intern("B"));
      assertTrue(C == interner.intern("C"));
      assertNotNull(interner.intern("D"));
   }

   @Test
   public void internAll() throws Exception {
      Collection<String> result = interner.internAll(Lists.arrayListOf("A", "B", "C", "D"));
      assertEquals(4, result.size());
   }

   @Test
   public void json() {
      JsonEntry entry = JsonEntry.from(interner);
      Interner<String> des = entry.as(TypeUtils.parameterizedType(Interner.class, String.class));
      assertEquals(interner, des);
   }
}