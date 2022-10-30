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

public class SimilarityMeasureTest {
   private final NumericNDArray v1 = nd.DFLOAT32.array(new float[]{1, 0, 1, 0, 1, 1});
   private final NumericNDArray v2 = nd.DFLOAT32.array(new float[]{1, 1, 0, 1, 0, 1});
   private final NumericNDArray v3 = nd.DFLOAT32.array(new float[]{1, 1, 0, 1, 0, 1});
   private final NumericNDArray v4 = nd.DFLOAT32.array(new float[]{0, 1, 0, 1, 0, 0});
   private final ContingencyTable c1 = ContingencyTable.create2X2(10, 20, 15, 100);

   @Test
   public void angular() {
      assertEquals(0.34, Similarity.Angular.calculate(v1, v2), 0.01d);
      assertEquals(0.34, Similarity.Angular.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(0.67, Similarity.Angular.asDistanceMeasure()
                                           .calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);

      assertEquals(1, Similarity.Angular.calculate(v3, v2), 0d);
      assertEquals(1, Similarity.Angular.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(0, Similarity.Angular.asDistanceMeasure().calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0d);

      assertEquals(0, Similarity.Angular.calculate(v1, v4), 0d);
      assertEquals(0, Similarity.Angular.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0d);
      assertEquals(1, Similarity.Angular.asDistanceMeasure().calculate(v1, v4), 0d);

      assertEquals(0.26, Similarity.Angular.calculate(c1), 0.01d);
   }

   @Test
   public void cosine() {
      assertEquals(0.5, Similarity.Cosine.calculate(v1, v2), 0d);
      assertEquals(0.5, Similarity.Cosine.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(0.5, Similarity.Cosine.asDistanceMeasure().calculate(v1, v2), 0d);


      assertEquals(1, Similarity.Cosine.calculate(v3, v2), 0d);
      assertEquals(1, Similarity.Cosine.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(0, Similarity.Cosine.asDistanceMeasure().calculate(v3, v2), 0d);

      assertEquals(0, Similarity.Cosine.calculate(v1, v4), 0d);
      assertEquals(0, Similarity.Cosine.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0d);
      assertEquals(1, Similarity.Cosine.asDistanceMeasure().calculate(v1, v4), 0d);

      assertEquals(0.39, Similarity.Cosine.calculate(c1), 0.01d);
   }

   @Test
   public void dice() {
      assertEquals(0.5, Similarity.Dice.calculate(v1, v2), 0d);
      assertEquals(0.5, Similarity.Dice.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(0.5, Similarity.Dice.asDistanceMeasure().calculate(v1, v2), 0d);

      assertEquals(1, Similarity.Dice.calculate(v3, v2), 0d);
      assertEquals(1, Similarity.Dice.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(0, Similarity.Dice.asDistanceMeasure().calculate(v3, v2), 0d);

      assertEquals(0, Similarity.Dice.calculate(v1, v4), 0d);
      assertEquals(0, Similarity.Dice.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0d);
      assertEquals(1, Similarity.Dice.asDistanceMeasure().calculate(v1, v4), 0d);

      assertEquals(0.571, Similarity.Dice.calculate(c1), 0.01d);
   }

   @Test
   public void diceGen2() {
      assertEquals(0.25, Similarity.DiceGen2.calculate(v1, v2), 0d);
      assertEquals(0.25, Similarity.DiceGen2.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(0.75, Similarity.DiceGen2.asDistanceMeasure().calculate(v1, v2), 0d);

      assertEquals(0.5, Similarity.DiceGen2.calculate(v3, v2), 0d);
      assertEquals(0.5, Similarity.DiceGen2.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(0.5, Similarity.DiceGen2.asDistanceMeasure().calculate(v3, v2), 0d);

      assertEquals(0, Similarity.DiceGen2.calculate(v1, v4), 0d);
      assertEquals(0, Similarity.DiceGen2.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0d);
      assertEquals(1, Similarity.DiceGen2.asDistanceMeasure().calculate(v1, v4), 0d);

      assertEquals(0.286, Similarity.DiceGen2.calculate(c1), 0.01d);
   }

   @Test
   public void dotProduct() {
      assertEquals(2, Similarity.DotProduct.calculate(v1, v2), 0d);
      assertEquals(2, Similarity.DotProduct.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(-1, Similarity.DotProduct.asDistanceMeasure().calculate(v1, v2), 0d);

      assertEquals(4, Similarity.DotProduct.calculate(v3, v2), 0d);
      assertEquals(4, Similarity.DotProduct.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(-3, Similarity.DotProduct.asDistanceMeasure().calculate(v3, v2), 0d);

      assertEquals(0, Similarity.DotProduct.calculate(v1, v4), 0d);
      assertEquals(0, Similarity.DotProduct.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0d);
      assertEquals(1, Similarity.DotProduct.asDistanceMeasure().calculate(v1, v4), 0d);

      assertEquals(10, Similarity.DotProduct.calculate(c1), 0.01d);

   }

   @Test
   public void jaccard() {
      assertEquals(0.34, Similarity.Jaccard.calculate(v1, v2), 0.01d);
      assertEquals(0.34, Similarity.Jaccard.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(0.66, Similarity.Jaccard.asDistanceMeasure().calculate(v1, v2), 0.01d);

      assertEquals(1, Similarity.Jaccard.calculate(v3, v2), 0d);
      assertEquals(1, Similarity.Jaccard.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(0, Similarity.Jaccard.asDistanceMeasure().calculate(v3, v2), 0d);

      assertEquals(0, Similarity.Jaccard.calculate(v1, v4), 0d);
      assertEquals(0, Similarity.Jaccard.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0d);
      assertEquals(1, Similarity.Jaccard.asDistanceMeasure().calculate(v1, v4), 0d);

      assertEquals(0.4, Similarity.Jaccard.calculate(c1), 0.01d);
   }

   @Test
   public void overlap() {
      assertEquals(0.5, Similarity.Overlap.calculate(v1, v2), 0.01d);
      assertEquals(0.5, Similarity.Overlap.calculate(v1.toDoubleArray(), v2.toDoubleArray()), 0.01d);
      assertEquals(0.5, Similarity.Overlap.asDistanceMeasure().calculate(v1, v2), 0.01d);

      assertEquals(1, Similarity.Overlap.calculate(v3, v2), 0d);
      assertEquals(1, Similarity.Overlap.calculate(v3.toDoubleArray(), v2.toDoubleArray()), 0d);
      assertEquals(0, Similarity.Overlap.asDistanceMeasure().calculate(v3, v2), 0d);

      assertEquals(0, Similarity.Overlap.calculate(v1, v4), 0d);
      assertEquals(0, Similarity.Overlap.calculate(v1.toDoubleArray(), v4.toDoubleArray()), 0d);
      assertEquals(1, Similarity.Overlap.asDistanceMeasure().calculate(v1, v4), 0d);

      assertEquals(0.67, Similarity.Overlap.calculate(c1), 0.01d);
   }

}
