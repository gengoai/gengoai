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
import com.gengoai.math.Math2;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author David B. Bracewell
 */
public class Math2Test {

   @Test
   public void rescale() throws Exception {
      assertEquals(2.0, Math2.rescale(2, 1, 5, 1, 5));
      assertEquals(3.25, Math2.rescale(2, 1, 5, 1, 10));
   }

   @Test(expected = IllegalArgumentException.class)
   public void badOrig() {
      Math2.rescale(1, 23, 1, 0, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void badNew() {
      Math2.rescale(1, 0, 1, 11, 1);
   }

   @Test
   public void clip() throws Exception {
      assertEquals(2.0, Math2.clip(4, -2, 2));
      assertEquals(-2.0, Math2.clip(-9, -2, 2));
      assertEquals(1.0, Math2.clip(1, -2, 2));
   }

   @Test(expected = IllegalArgumentException.class)
   public void badClipRange() {
      Math2.clip(1, 11, 1);
   }

   @Test
   public void sum() throws Exception {
      assertEquals(3, Math2.sum(1, 2));
      assertEquals(3L, Math2.sum(1L, 2L));
      assertEquals(3.5, Math2.sum(1.5, 2.0));
      assertEquals(3.5, Math2.sum(Lists.arrayListOf(1.5, 2.0)));
   }

   @Test
   public void summaryStatistics() throws Exception {
      assertEquals(1.5, Math2.summaryStatistics(1,2).getAverage());
      assertEquals(5.0, Math2.summaryStatistics(1L, 2L).getSumOfSquares());
      assertEquals(0.5, Math2.summaryStatistics(1.0, 2.0).getSampleVariance());
   }

}