/*
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

package com.gengoai.apollo.math.measure;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CorrelationMeasureTest {
   private final NumericNDArray v1 = nd.DFLOAT32.array(new float[]{1, 0, 1, 0, 1, 1});
   private final NumericNDArray v2 = nd.DFLOAT32.array(new float[]{1, 1, 0, 1, 0, 1});
   private final ContingencyTable c1 = ContingencyTable.create2X2(10, 20, 15, 100);

   @Test
   public void kendall() {
      assertEquals(-0.50d, Correlation.Kendall.calculate(v1, v2), 0.01);
      assertEquals(-0.50d, Correlation.Kendall.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01);
      assertEquals(1.50d, Correlation.Kendall.asDistanceMeasure().calculate(v1, v2), 0.01);
   }

   @Test
   public void pearson() {
      assertEquals(-0.50d, Correlation.Pearson.calculate(v1, v2), 0.01);
      assertEquals(-0.50d, Correlation.Pearson.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01);
      assertEquals(1.50d, Correlation.Pearson.asDistanceMeasure().calculate(v1, v2), 0.01);
   }

   @Test
   public void rSquared() {
      assertEquals(0.25, Correlation.R_Squared.calculate(v1, v2), 0.01);
      assertEquals(0.25, Correlation.R_Squared.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01);
      assertEquals(0.75, Correlation.R_Squared.asDistanceMeasure().calculate(v1, v2), 0.01);
   }

   @Test
   public void spearman() {
      assertEquals(-0.50d, Correlation.Spearman.calculate(v1, v2), 0.01);
      assertEquals(-0.50d, Correlation.Spearman.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01);
      assertEquals(1.50d, Correlation.Spearman.asDistanceMeasure().calculate(v1, v2), 0.01);
   }
}
