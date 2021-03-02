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
import java.util.function.Supplier;

/**
 * <p>Functional Interface that initializes the values of a given NDArray.</p>
 *
 * @param <T> the type of NDArray
 */
public interface NDArrayInitializer<T extends NDArray> extends SerializableConsumer<T> {

   /**
    * Xavier initialization for Neural Networks
    */
   NDArrayInitializer<NumericNDArray> xavier = (m) -> {
      var limit = Math.sqrt(6.0) / Math.sqrt(m.shape().rows() + m.shape().columns());
      var rnd = new RandomDataGenerator();
      m.mapiDouble(x -> rnd.nextUniform(-limit, limit));
   };

   /**
    * <p>Generates  random values using a Gaussian distribution with mean 0 and standard deviation of 1</p>
    */
   NDArrayInitializer<NumericNDArray> gaussian = gaussian(0, 1);

   /**
    * <p>Generates random values using a binomial distribution with <code>numberOfTrials</code> and
    * <code>probabilityOfSuccess</code></p>
    *
    * @param numberOfTrials       the number of trials
    * @param probabilityOfSuccess the probability of success
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<NumericNDArray> binomial(int numberOfTrials, double probabilityOfSuccess) {
      return (m) -> {
         var rnd = new RandomDataGenerator();
         m.mapiDouble(x -> rnd.nextBinomial(numberOfTrials, probabilityOfSuccess));
      };
   }

   /**
    * <p>Initializes an NDArray by setting all elements to a constant value.</p>
    *
    * @param <T>           the type of NDArray
    * @param constantValue the constant value
    * @return the NDArrayInitializer
    */
   static <T extends NDArray> NDArrayInitializer<T> constant(Object constantValue) {
      return (m) -> m.fill(constantValue);
   }

   /**
    * <p>Initializes an NDArray by setting all elements to a constant value.</p>
    *
    * @param constantValue the constant value
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<NumericNDArray> constant(double constantValue) {
      return (m) -> m.fill(constantValue);
   }

   /**
    * <p>Generates random values using a Gaussian distribution with mean <code>mu</code> and standard deviation of
    * <code>sigma</code></p>
    *
    * @param mu    the mean
    * @param sigma the standard deviation
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<NumericNDArray> gaussian(double mu, double sigma) {
      return (m) -> {
         var rnd = new RandomDataGenerator();
         m.mapiDouble(x -> rnd.nextGaussian(mu, sigma));
      };
   }

   /**
    * <p>Generates random values using Java's Random class with specific implementation based on the data type of the
    * NDArray.</p>
    *
    * @param <T> the type of NDArray
    * @return the NDArrayInitializer
    */
   static <T extends NDArray> NDArrayInitializer<T> random() {
      return (m) -> {
         var rnd = new Random();
         Class<?> c = Primitives.wrap(m.getType());
         if (c == Integer.class) {
            Cast.<NumericNDArray>as(m).mapiDouble(d -> rnd.nextInt());
         } else if (c == Long.class) {
            Cast.<NumericNDArray>as(m).mapiDouble(d -> rnd.nextLong());
         } else if (c == String.class) {
            Cast.<ObjectNDArray<String>>as(m).mapi(d -> Strings.randomHexString(rnd.nextInt(8) + 1));
         } else {
            Cast.<NumericNDArray>as(m).mapiDouble(n -> rnd.nextDouble());
         }
      };
   }

   /**
    * <p>Generates random strings of hexadecimal characters of given <code>length</code></p>
    *
    * @param length the length of the strings to generate
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<ObjectNDArray<String>> randomHexString(int length) {
      return (m) -> {
         var rnd = new RandomDataGenerator();
         m.mapi(x -> rnd.nextHexString(length));
      };
   }

   /**
    * <p>Generates random values from the given RealDistribution</p>
    *
    * @param distribution the distribution
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<NumericNDArray> sample(@NonNull RealDistribution distribution) {
      return (m) -> {
         m.mapiDouble(d -> distribution.sample());
      };
   }

   /**
    * <p>Generates random values from the given distribution</p>
    *
    * @param distribution the distribution
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<NumericNDArray> sample(@NonNull IntegerDistribution distribution) {
      return (m) -> {
         m.mapiDouble(d -> distribution.sample());
      };
   }

   /**
    * <p>Generates values from the given IntSupplier</p>
    *
    * @param supplier the supplier
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<NumericNDArray> sample(@NonNull IntSupplier supplier) {
      return (m) -> {
         m.mapiDouble(d -> supplier.getAsInt());
      };
   }

   /**
    * <p>Generates values from the given DoubleSupplier</p>
    *
    * @param supplier the supplier
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<NumericNDArray> sample(@NonNull DoubleSupplier supplier) {
      return (m) -> {
         m.mapiDouble(d -> supplier.getAsDouble());
      };
   }

   /**
    * <p>Generates values from the given LongSupplier</p>
    *
    * @param supplier the supplier
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<NumericNDArray> sample(@NonNull LongSupplier supplier) {
      return (m) -> {
         m.mapiDouble(d -> supplier.getAsLong());
      };
   }

   /**
    * <p>Generates values from the given Supplier</p>
    *
    * @param <T>      the data type of the ObjectNDArray
    * @param supplier the supplier
    * @return the NDArrayInitializer
    */
   static <T> NDArrayInitializer<ObjectNDArray<T>> sample(@NonNull Supplier<T> supplier) {
      return (m) -> {
         m.map(v -> supplier.get());
      };
   }


   /**
    * <p>Generates random values from uniformly within <code>[low, high)</code></p>
    *
    * @param low  the lower bound of the randomly generated number
    * @param high the upper bound of the randomly generated number
    * @return the NDArrayInitializer
    */
   static NDArrayInitializer<NumericNDArray> uniform(@NonNull Number low, @NonNull Number high) {
      return (m) -> {
         var rnd = new RandomDataGenerator();
         Class<?> c = Primitives.wrap(m.getType());
         if (c == Integer.class) {
            m.mapiDouble(d -> rnd.nextInt(low.intValue(), high.intValue()));
         } else if (c == Long.class) {
            m.mapiDouble(d -> rnd.nextLong(low.longValue(), high.longValue()));
         } else {
            m.mapiDouble(n -> rnd.nextUniform(low.doubleValue(), high.doubleValue(), true));
         }
      };
   }

}//END OF NDArrayInitializer
