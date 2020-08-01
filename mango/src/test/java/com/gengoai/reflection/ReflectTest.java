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

import com.gengoai.specification.Path;
import com.gengoai.specification.Protocol;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author David B. Bracewell
 */
public class ReflectTest {


   public static class TestClass {
      @Protocol
      private String name;
      @Path
      public int age;

      public TestClass(String name, int age) {
         this.name = name;
         this.age = age;
      }

      private TestClass() {

      }

      public void setFullName(String value) {
         this.name = value;
      }

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      private String makeme() {
         return name + " := " + age;
      }

   }


   @Test
   public void testAnnotations() throws Exception {
      TestClass tc = new TestClass("Jamie", 100);
      Reflect r = Reflect.onObject(tc).allowPrivilegedAccess();
      assertEquals(1, r.getFieldsWithAnnotation(Protocol.class).size());
      assertEquals(1, r.getFieldsWithAnnotation(Path.class).size());
      assertEquals("Jamie", r.getField("name")
                             .processAnnotations(Protocol.class, p -> tc.name).get(0));
      assertEquals("Jamie", r.getField("name")
                             .processAnnotation(Protocol.class, p -> tc.name));
      assertEquals(0, r.getField("name").processAnnotations(Path.class, p -> 1).size());
   }

   @Test
   public void test() throws Exception {
      TestClass tc = new TestClass("Jamie", 100);
      Reflect r = Reflect.onObject(tc).allowPrivilegedAccess();

      //test name
      assertEquals("Jamie", r.getMethod("getName").invoke());


      //Create an no-age jane
      assertEquals("jane", r.create()
                            .getField("name")
                            .set("jane")
                            .getOwner()
                            .getField("name")
                            .get());

      //Create a no-named 34 year old
      assertEquals(34, (int) r.getField("age")
                              .set(34)
                              .get());

      //Create a 100 year old jane
      assertEquals("jane", r.create("jane", 100).get("name"));

      //Create a Test class and set the age to 100
      assertEquals(100, (Number) r.create("jane", 100).get("age"));

      //Create a Test class and set the name to 89
      assertEquals("89", r.create().set("name", 89).get("name"));

      //Test privaleged method call
      assertEquals("jane := 100", r.create("jane", 100).getMethod("makeme").invoke());


      assertEquals("Joe", r.create().set("fullName", "Joe").get("name"));

   }

   @Test(expected = ReflectionException.class)
   public void testNoMethodException() throws Exception {
      TestClass tc = new TestClass("Jamie", 100);
      Reflect r = Reflect.onObject(tc);

      //test age
      assertEquals(100, (Number) r.getMethod("getAge").invoke());
   }

}//END OF ReflectTest
