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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanDescriptorTest {

  private BeanDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    descriptor = new BeanDescriptor(TestBean.class);
  }

  @Test
  public void testHasReadMethod() throws Exception {
    //Real method
    assertTrue(descriptor.hasReadMethod("children"));

    //Fake method
    assertFalse(descriptor.hasReadMethod("monsters"));

    //final field
    assertTrue(descriptor.hasReadMethod("name"));
  }

  @Test
  public void testHasWriteMethod() throws Exception {
    //Real method
    assertTrue(descriptor.hasWriteMethod("children"));

    //Fake method
    assertFalse(descriptor.hasWriteMethod("monsters"));

    //final field
    assertFalse(descriptor.hasWriteMethod("name"));
  }

  @Test
  public void testGetReadMethod() throws Exception {
    //Real method
    assertNotNull(descriptor.getReadMethod("children"));

    //Fake method
    assertNull(descriptor.getReadMethod("monsters"));

    //final field
    assertNotNull(descriptor.getReadMethod("name"));


    //class
    assertNull(descriptor.getReadMethod("class"));
  }

  @Test
  public void testGetWriteMethod() throws Exception {
    //Real method
    assertNotNull(descriptor.getWriteMethod("children"));

    //Fake method
    assertNull(descriptor.getWriteMethod("monsters"));

    //final field
    assertNull(descriptor.getWriteMethod("name"));
  }

  @Test
  public void testGetReadMethodNames() throws Exception {
    Assert.assertEquals(
       Sets.hashSetOf("name", "stocks", "children"),
       descriptor.getReadMethodNames()
                       );
  }

  @Test
  public void testGetWriteMethodNames() throws Exception {
    Assert.assertEquals(
       Sets.hashSetOf("stocks", "children"),
       descriptor.getWriteMethodNames()
                       );
  }

  @Test
  public void testGetBeanClass() throws Exception {
    assertEquals(TestBean.class, descriptor.getBeanClass());
  }

  @Test
  public void testNumberOfReadMethods() throws Exception {
    assertEquals(3, descriptor.numberOfReadMethods());
  }

  @Test
  public void testNumberOfWriteMethods() throws Exception {
    assertEquals(2, descriptor.numberOfWriteMethods());
  }

  @Test
  public void testCreateInstance() throws Exception {
    assertNotNull(descriptor.createInstance());
  }

  @Test
  public void testCreateInstanceQuietly() throws Exception {
    assertNotNull(descriptor.createInstanceQuietly());
  }

}//END OF BeanDescriptorTest
