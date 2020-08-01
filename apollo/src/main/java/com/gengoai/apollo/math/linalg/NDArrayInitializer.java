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

package com.gengoai.apollo.math.linalg;

import com.gengoai.function.SerializableConsumer;

import java.util.Random;

/**
 * Encapsulates logic for initializing an {@link NDArray} with initial values.
 *
 * @author David B. Bracewell
 */
public interface NDArrayInitializer extends SerializableConsumer<NDArray> {

   /**
    * Glorot and Bengio (2010) for sigmoid units
    */
   NDArrayInitializer glorotAndBengioSigmoid = (m) -> {
      double max = 4 * Math.sqrt(6.0) / Math.sqrt(m.rows() + m.columns());
      double min = -max;
      m.mapi(x -> min + (max - min) * Math.random());
   };

   /**
    * Glorot and Bengio (2010) for hyperbolic tangent units
    */
   NDArrayInitializer glorotAndBengioTanH = (m) -> {
      double max = Math.sqrt(6.0) / Math.sqrt(m.rows() + m.columns());
      double min = -max;
      m.mapi(x -> min + (max - min) * Math.random());
   };

   /**
    * Initializes the values in an NDArray to random values with on average <code>sparsity</code> percentage zero
    * entries.
    *
    * @param sparsity the sparsity factor (the desired percentage of elements that should have a zero value).
    * @param rnd      the Random number generator
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer sparseRandom(double sparsity, Random rnd) {
      return n -> n.mapi(v -> {
         if (rnd.nextDouble() >= sparsity) {
            return rnd.nextDouble();
         }
         return 0;
      });
   }

   /**
    * Initializes the values in an NDArray to random values with on average <code>sparsity</code> percentage zero
    * entries.
    *
    * @param sparsity the sparsity factor (the desired percentage of elements that should have a zero value).
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer sparseRandom(double sparsity) {
      return sparseRandom(sparsity, rnd);
   }

   /**
    * Initializes the values to <code>1.0</code>
    */
   NDArrayInitializer ones = (m) -> m.mapi(x -> 1d);
   /**
    * Random gaussian initializer
    */
   NDArrayInitializer randn = randn(new Random());
   /**
    * Default Random object to use
    */
   Random rnd = new Random(123);
   /**
    * Random initializer using <code>Random#nextDouble()</code>
    */
   NDArrayInitializer rand = (m) -> m.mapi(d -> rnd.nextDouble());
   /**
    * Zero-Value Initializer
    */
   NDArrayInitializer zeroes = (m) -> m.mapi(x -> 0d);

   /**
    * Random initializer using <code>Random#nextDouble()</code> that takes a Random object to use
    *
    * @param rnd the Random to use for generating values
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer rand(Random rnd) {
      return (m) -> m.mapi(d -> rnd.nextDouble());
   }

   /**
    * Uniform random initializer
    *
    * @param rnd the Random object to use for generating random values
    * @param min the minimum or lower bound of the numbers to generate
    * @param max the maximum or upper bound of the numbers to generate
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer rand(Random rnd, int min, int max) {
      return (m) -> m.mapi(d -> min + rnd.nextDouble() * max);
   }

   /**
    * Uniform random initializer
    *
    * @param min the minimum or lower bound of the numbers to generate
    * @param max the maximum or upper bound of the numbers to generate
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer rand(int min, int max) {
      return rand(new Random(), min, max);
   }

   /**
    * Random gaussian initializer
    *
    * @param rnd the Random object to use for generating random values
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer randn(Random rnd) {
      return (m) -> m.mapi(d -> rnd.nextGaussian());
   }


}//END OF NDArrayInitializer
