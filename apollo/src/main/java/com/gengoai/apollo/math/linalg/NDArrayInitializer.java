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

package com.gengoai.apollo.math.linalg;

import com.gengoai.Primitives;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableConsumer;
import com.gengoai.string.Strings;
import lombok.NonNull;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.Random;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public interface NDArrayInitializer<T> extends SerializableConsumer<NDArray<T>> {

   /**
    * Glorot and Bengio (2010) for hyperbolic tangent units
    */
   NDArrayInitializer<Number> xavier = (m) -> {
      var limit = Math.sqrt(6.0) / Math.sqrt(m.shape().rows() + m.shape().columns());
      var rnd = new RandomDataGenerator();
      m.mapiDouble(x -> rnd.nextUniform(-limit, limit));
   };

   NDArrayInitializer<Number> gaussian = gaussian(0, 1);

   static <T extends Number> NDArrayInitializer<T> binomial(int numberOfTrials, double probabilityOfSuccess) {
      return (m) -> {
         var rnd = new RandomDataGenerator();
         m.mapiDouble(x -> rnd.nextBinomial(numberOfTrials, probabilityOfSuccess));
      };
   }

   static <T> NDArrayInitializer<T> constant(T constantValue) {
      return (m) -> m.fill(constantValue);
   }

   static <T extends Number> NDArrayInitializer<T> constant(double constantValue) {
      return (m) -> m.fill(constantValue);
   }

   static <T extends Number> NDArrayInitializer<T> gaussian(double mu, double sigma) {
      return (m) -> {
         var rnd = new RandomDataGenerator();
         m.mapiDouble(x -> rnd.nextGaussian(mu, sigma));
      };
   }

   static <T> NDArrayInitializer<T> random() {
      return (m) -> {
         var rnd = new Random();
         Class<?> c = Primitives.wrap(m.getType());
         if (c == Integer.class) {
            Cast.<NDArray<Integer>>as(m).mapi(d -> rnd.nextInt());
         } else if (c == Long.class) {
            Cast.<NDArray<Long>>as(m).mapi(d -> rnd.nextLong());
         } else if (c == Boolean.class) {
            Cast.<NDArray<Boolean>>as(m).mapi(d -> rnd.nextBoolean());
         } else if (c == String.class) {
            Cast.<NDArray<String>>as(m).mapi(d -> Strings.randomHexString(rnd.nextInt(8) + 1));
         } else {
            m.mapiDouble(n -> rnd.nextDouble());
         }
      };
   }

   static NDArrayInitializer<String> randomHexString(int length) {
      return (m) -> {
         var rnd = new RandomDataGenerator();
         m.mapi(x -> rnd.nextHexString(length));
      };
   }

   static <T extends Number> NDArrayInitializer<T> sample(@NonNull RealDistribution distribution) {
      return (m) -> {
         m.mapiDouble(d -> distribution.sample());
      };
   }

   static <T extends Number> NDArrayInitializer<T> sample(@NonNull IntegerDistribution distribution) {
      return (m) -> {
         m.mapiDouble(d -> distribution.sample());
      };
   }

   static <T extends Number> NDArrayInitializer<T> sample(@NonNull IntSupplier distribution) {
      return (m) -> {
         m.mapiDouble(d -> distribution.getAsInt());
      };
   }

   static <T extends Number> NDArrayInitializer<T> sample(@NonNull DoubleSupplier distribution) {
      return (m) -> {
         m.mapiDouble(d -> distribution.getAsDouble());
      };
   }

   static <T extends Number> NDArrayInitializer<T> sample(@NonNull LongSupplier distribution) {
      return (m) -> {
         m.mapiDouble(d -> distribution.getAsLong());
      };
   }

   static <T extends Number> NDArrayInitializer<T> uniform(@NonNull Number low, @NonNull Number high) {
      return (m) -> {
         var rnd = new RandomDataGenerator();
         Class<?> c = Primitives.wrap(m.getType());
         if (c == Integer.class) {
            Cast.<NDArray<Integer>>as(m).mapi(d -> rnd.nextInt(low.intValue(), high.intValue()));
         } else if (c == Long.class) {
            Cast.<NDArray<Long>>as(m).mapi(d -> rnd.nextLong(low.longValue(), high.longValue()));
         } else {
            m.mapiDouble(n -> rnd.nextUniform(low.doubleValue(), high.doubleValue(), true));
         }
      };
   }

}//END OF NDArrayInitializer
