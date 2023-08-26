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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssociationMeasureTest {
   private final ContingencyTable c1 = ContingencyTable.create2X2(10, 20, 15, 100);

   @Test
   public void chiSquare() {
      assertEquals(24.02, Association.CHI_SQUARE.calculate(c1), 0.01d);
      assertEquals(0d, Association.CHI_SQUARE.pValue(c1), 0.01d);
   }

   @Test
   public void gSquare() {
      assertEquals(19.41, Association.G_SQUARE.calculate(c1), 0.01d);
      assertEquals(0d, Association.G_SQUARE.pValue(c1), 0.01d);
   }

   @Test
   public void mikolov() {
      assertEquals(0.1, Association.MIKOLOV.calculate(c1), 0.01d);
   }

   @Test
   public void mutualInformation() {
      assertEquals(0.14, Association.MI.calculate(c1), 0.01d);
   }

   @Test
   public void normalizedPointWiseMutualInformation() {
      assertEquals(0.52, Association.NPMI.calculate(c1), 0.01d);
   }

   @Test
   public void oddRatio() {
      assertEquals(15.0, Association.ODDS_RATIO.calculate(c1), 0.01d);
      assertEquals(0d, Association.ODDS_RATIO.pValue(c1), 0.01d);
   }

   @Test
   public void pointWiseMutualInformation() {
      assertEquals(1.74, Association.PMI.calculate(c1), 0.01d);
   }

   @Test
   public void poissonStirling() {
      assertEquals(2.04, Association.POISSON_STIRLING.calculate(c1), 0.01d);
   }

   @Test
   public void relativeRisk() {
      assertEquals(8.0, Association.RELATIVE_RISK.calculate(c1), 0.01d);
      assertEquals(0.02, Association.RELATIVE_RISK.pValue(c1), 0.01d);
   }

   @Test
   public void tScore() {
      assertEquals(2.21, Association.T_SCORE.calculate(c1), 0.01d);
      assertEquals(0.14, Association.T_SCORE.pValue(c1), 0.01d);
   }

}
