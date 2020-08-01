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

package com.gengoai.collection;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class SortingTest {

   @Test
   public void testHashCodeComparator() throws Exception {
      String s1 = null;
      String s2 = "a";
      String s3 = "b";
      String s4 = "b";
      assertEquals(Sorting.hashCodeComparator().compare(s1, s2), 1);
      assertEquals(Sorting.hashCodeComparator().compare(s2, s1), -1);
      assertEquals(Sorting.hashCodeComparator().compare(s4, s3), 0);
   }
}//END OF SortingTest
