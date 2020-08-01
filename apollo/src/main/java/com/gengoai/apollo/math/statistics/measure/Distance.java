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


import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.math.Math2;

/**
 * <p>Commonly used distance measures.</p>
 *
 * @author dbracewell
 */
public enum Distance implements DistanceMeasure {
   /**
    * <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Euclidean distance</a>
    */
   Euclidean {
      @Override
      public double calculate(NDArray v1, NDArray v2) {
         return Math.sqrt((v1.dot(v1) + v2.dot(v2)) - (2.0 * v1.dot(v2)));
      }
   },
   /**
    * Variation on Euclidean distance that doesn't take the square root of the sum of squared differences
    */
   SquaredEuclidean {
      @Override
      public double calculate(NDArray v1, NDArray v2) {
         return (v1.dot(v1) + v2.dot(v2)) - (2.0 * v1.dot(v2));
      }

   },
   /**
    * <a href="https://en.wiktionary.org/wiki/Manhattan_distance">Manhattan distance</a>
    */
   Manhattan {
      @Override
      public double calculate(NDArray v1, NDArray v2) {
         return v1.sub(v2).norm1();
      }


   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Hamming_distance">Hamming Distance</a>
    */
   Hamming {
      @Override
      public double calculate(NDArray v1, NDArray v2) {
         return v1.map(v2, (d1, d2) -> d1 != d2 ? 1 : 0).sum();
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Earth_mover%27s_distance">Earth mover's distance</a>
    */
   EarthMovers {
      @Override
      public double calculate(NDArray v1, NDArray v2) {
         double last = 0;
         double sum = 0;
         for (int i = 0; i < v1.length(); i++) {
            double d1 = v1.get(i);
            double d2 = v2.get(i);
            double dist = (d1 + last) - d2;
            sum += Math.abs(dist);
            last = dist;
         }
         return sum;
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Chebyshev_distance">Chebyshev distance</a>
    */
   Chebyshev {
      @Override
      public double calculate(NDArray v1, NDArray v2) {
         return v1.map(v2, (d1, d2) -> Math.abs(d1 - d2)).max();
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence">Kullback–Leibler divergence</a>
    */
   KLDivergence {
      @Override
      public double calculate(NDArray v1, NDArray v2) {
         return v1.map(v2, (d1, d2) -> d1 * Math2.safeLog(d1 / d2)).sum();
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Angular_distance">Angular distance</a>
    */
   Angular {
      @Override
      public double calculate(NDArray v1, NDArray v2) {
         return Math.acos(Similarity.Cosine.calculate(v1, v2)) / Math.PI;
      }
   }


}//END OF Distance
