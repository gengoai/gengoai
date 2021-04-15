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

package com.gengoai.reflection;

import com.gengoai.collection.Sets;
import com.gengoai.config.Config;
import com.gengoai.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author David B. Bracewell
 */
public class BeanUtilsTest {

   @Before
   public void setup() throws Exception {
      Config.initializeTest();
      Config.loadConfig(Resources.fromClasspath("com/gengoai/testing.conf"));
   }

   @Test
   public void testCollectionParam() throws Exception {
      TestBean testBean = BeanUtils.getBean(TestBean.class);
      assertTrue(testBean.getChildren().isEmpty());

      Map<String, Double> map = testBean.getStocks();
      assertEquals(120.5, map.get("GE"), 0);
      assertEquals(45.8, map.get("ATT"), 0);
      assertEquals(98.7, map.get("ZEB"), 0);

      testBean = Config.get("bean.redirect").cast();
      assertEquals(Sets.hashSetOf("Sam", "Ryan", "Billy"), testBean.getChildren());

      map = testBean.getStocks();
      assertEquals(120.5, map.get("GE"), 0);
      assertEquals(45.8, map.get("ATT"), 0);
      assertEquals(98.7, map.get("ZEB"), 0);

   }

   @Test
   public void test() throws Exception {
      TestBean testBean = BeanUtils.getNamedBean("testbean", TestBean.class);
      assertEquals("John", testBean.getName());
      assertTrue(Sets.difference(Sets.hashSetOf("Sam", "Ryan", "Billy"), testBean.getChildren()).isEmpty());
   }

   @Test
   public void testNamedBeanWithConstructor() throws Exception {
      TestBean testBean = BeanUtils.getNamedBean("testbean2", TestBean.class);
      assertEquals("John", testBean.getName());
      assertTrue(Sets.difference(Sets.hashSetOf("Sam", "Ryan", "Billy"), testBean.getChildren()).isEmpty());
   }

}
