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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class SpecifiableTest {

   @Test
   public void test() {
      TestBean specification = Specification.parse(
         "testBean:protocol:subProtocol0:subProtocol1::~/path;q=1;f=1.2;b=true;b=false",
         TestBean.class);
      assertEquals("testBean", specification.getSchema());
      assertEquals("protocol", specification.getProtocol());
      assertEquals("subProtocol0", specification.getSp1());
      assertEquals(Arrays.asList("subProtocol0", "subProtocol1"), specification.getSp());
      assertEquals("~/path", specification.getPath());
      assertEquals(1, specification.getQp1());
      assertEquals(1.2, specification.getQp2(), 0.01f);
      assertEquals(Arrays.asList(true, false), specification.getQp3());
      assertEquals(specification, Specification.parse(specification.toString(), TestBean.class));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testWrongSchema() {
      Specification.parse(
         "testBean2:protocol:subProtocol0:subProtocol1::~/path;q=1;f=1.2;b=true;b=false",
         TestBean.class);
   }
}