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

package com.gengoai.apollo.math.statistics.measure;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.math.Math2;
import lombok.NonNull;

/**
 * <p>Common methods for determining the similarity between two items</p>
 *
 * @author David B. Bracewell
 */
public enum Similarity implements SimilarityMeasure {
   /**
    * <a href="https://en.wikipedia.org/wiki/Dot_product">The dot product</a>
    */
   DotProduct {
      @Override
      public double calculate(@NonNull NDArray v1, @NonNull NDArray v2) {
         return v1.dot(v2);
      }

      @Override
      public double calculate(@NonNull ContingencyTable table) {
         return table.get(0, 0);
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient">The Dice Coefficient</a>
    */
   Dice {
      @Override
      public double calculate(@NonNull NDArray v1, @NonNull NDArray v2) {
         return (2 * v1.dot(v2)) / (v1.sum() + v2.sum());
      }

      @Override
      public double calculate(@NonNull ContingencyTable table) {
         Validation.checkArgument(table.rowCount() == table.columnCount() && table.rowCount() == 2,
                                  "Only supports 2x2 contingency tables.");
         return 2 * table.get(0, 0) / (table.columnSum(0) + table.rowSum(0));
      }
   },
   /**
    * <a href="http://wortschatz.uni-leipzig.de/~sbordag/aalw05/Referate/14_MiningRelationen_WSA_opt/Curran_03.pdf">Variation
    * of the Dice's Coefficient</a>
    */
   DiceGen2 {
      @Override
      public double calculate(@NonNull NDArray v1, @NonNull NDArray v2) {
         return v1.dot(v2) / (v1.sum() + v2.sum());
      }

      @Override
      public double calculate(@NonNull ContingencyTable table) {
         Validation.checkArgument(table.rowCount() == table.columnCount() && table.rowCount() == 2,
                                  "Only supports 2x2 contingency tables.");
         return 2 * table.get(0, 0) / (table.columnSum(0) + table.rowSum(0));
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Cosine_similarity">Cosine Similarity</a>
    */
   Cosine {
      @Override
      public double calculate(@NonNull NDArray v1, @NonNull NDArray v2) {
         double v1Norm = v1.norm2();
         double v2Norm = v2.norm2();
         if(v1Norm == 0 && v2Norm == 0) {
            return 1.0;
         } else if(v1Norm == 0 || v2Norm == 0) {
            return 0.0;
         }
         return v1.dot(v2) / (v1Norm * v2Norm);
      }

      @Override
      public double calculate(@NonNull ContingencyTable table) {
         Validation.checkArgument(table.rowCount() == table.columnCount() && table.rowCount() == 2,
                                  "Only supports 2x2 contingency tables.");
         double c = Math.sqrt(Math.pow(table.get(0, 0), 2) + Math.pow(table.get(0, 1), 2));
         double r = Math.sqrt(Math.pow(table.get(0, 0), 2) + Math.pow(table.get(1, 0), 2));
         return table.get(0, 0) / (r + c);
      }
   },
   /**
    * Extended version of the <a href="https://en.wikipedia.org/wiki/Jaccard_index">Jaccard Index</a>, which is also
    * known as the <code>Tanimoto</code> coefficient.
    */
   Jaccard {
      @Override
      public double calculate(@NonNull NDArray v1, @NonNull NDArray v2) {
         if(v1.size() == 0 && v2.size() == 0) {
            return 1.0;
         }
         double norm = v1.map(v2, Math::max).sum();
         if(norm == 0) {
            return v1.sumOfSquares() == 0 && v2.sumOfSquares() == 0
                   ? 1.0
                   : 0.0;
         }
         return v1.map(v2, Math::min).sum() / norm;
      }

      @Override
      public double calculate(@NonNull ContingencyTable table) {
         Validation.checkArgument(table.rowCount() == table.columnCount() && table.rowCount() == 2,
                                  "Only supports 2x2 contingency tables.");
         return table.get(0, 0) / (table.get(0, 0) + table.get(0, 1) + table.get(1, 0));
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Overlap_coefficient">Overlap Coefficient</a>
    */
   Overlap {
      @Override
      public double calculate(@NonNull NDArray v1, @NonNull NDArray v2) {
         if(v1.size() == 0 && v2.size() == 0) {
            return 1.0;
         }
         double v1Norm = v1.sumOfSquares();
         double v2Norm = v2.sumOfSquares();
         if(v1Norm == 0 && v2Norm == 0) {
            return 1.0;
         } else if(v1Norm == 0 || v2Norm == 0) {
            return 0.0;
         }
         return Math2.clip(v1.dot(v2) / Math.min(v1Norm, v2Norm), -1, 1);
      }

      @Override
      public double calculate(@NonNull ContingencyTable table) {
         Validation.notNull(table);
         Validation.checkArgument(table.rowCount() == table.columnCount() && table.rowCount() == 2,
                                  "Only supports 2x2 contingency tables.");
         return table.get(0, 0) / Math.min(table.columnSum(0), table.rowSum(0));
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Cosine_similarity#Angular_distance_and_similarity">Angular similarity
    * (variant of Cosine)</a>
    */
   Angular {
      @Override
      public double calculate(@NonNull ContingencyTable table) {
         return 1.0 - Math.acos(Cosine.calculate(table)) / Math.PI;
      }

      @Override
      public double calculate(@NonNull NDArray v1, @NonNull NDArray v2) {
         return 1.0 - Math.acos(Cosine.calculate(v1, v2)) / Math.PI;
      }

      @Override
      public DistanceMeasure asDistanceMeasure() {
         return Distance.Angular;
      }
   }

}//END OF SimilarityMeasures
