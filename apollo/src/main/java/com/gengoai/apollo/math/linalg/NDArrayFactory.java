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
import com.gengoai.apollo.math.linalg.dense.GenericDenseObjectNDArrayFactory;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.io.Serializable;

import static com.gengoai.apollo.math.linalg.nd.*;

/**
 * <p>Factory for constructing new NDArray of a given data type.</p>
 */
public abstract class NDArrayFactory implements Serializable {
   private static final long serialVersionUID = 1L;
   protected final Class<?> target;

   protected NDArrayFactory(Class<?> c) {
      this.target = Primitives.unwrap(c);
   }

   /**
    * <p>Gets a NDArrayFactory to create NDArrays of the given data type.</p>
    *
    * @param type   the data type of the NDArray (e.g. Float, Double, String, etc.)
    * @param sparse True if we want a factory for creating sparse NDArrays
    * @return the NDArrayFactory
    */
   public static <T extends NDArrayFactory> T forType(@NonNull Class<?> type, boolean sparse) {
      type = Primitives.wrap(type);

      if (type == Float.class) {
         return Cast.as(sparse ? SFLOAT32 : DFLOAT32);
      }

      if (type == Double.class) {
         return Cast.as(sparse ? SFLOAT64 : DFLOAT64);
      }

      if (type == Integer.class) {
         return Cast.as(sparse ? SINT32 : DINT32);
      }

      if (type == Long.class) {
         return Cast.as(sparse ? SINT64 : DINT64);
      }

      if (type == String.class) {
         return Cast.as(sparse ? SSTRING : DSTRING);
      }

      return Cast.as(new GenericDenseObjectNDArrayFactory<>(type));
   }

   /**
    * <p>Gets a NDArrayFactory to create NDArrays of the given data type. Examines the config setting
    * <code>ndarray.sparse</code> to determine if sparse factories are desired (by default False).</p>
    *
    * @param type the data type of the NDArray (e.g. Float, Double, String, etc.)
    * @return the NDArrayFactory
    */
   public static <T extends NDArrayFactory> T forType(@NonNull Class<?> type) {
      return forType(type, Config.get("ndarray.sparse").asBooleanValue(false));
   }


   /**
    * <p>Attempts to cast this factory as a NumericNDArrayFactory</p>
    *
    * @return this factory cast as a NumericNDArrayFactory
    * @throws IllegalArgumentException if this factory is not an instance of NumericNDArrayFactory
    */
   public NumericNDArrayFactory asNumeric() {
      throw new IllegalArgumentException("Cannot Cast this NDArrayFactory as a NumericNDArrayFactory");
   }

   /**
    * <p>Creates an empty NDArray</p>
    *
    * @return the NDArray
    */
   public NDArray empty() {
      return zeros(Shape.shape(0));
   }


   /**
    * <p>Gets the data type of the elements in the NDArrays created by this factory.</p>
    *
    * @return the  data type of the elements in the NDArrays created by this factory
    */
   public final Class<?> getType() {
      return Cast.as(Primitives.wrap(target));
   }


   /**
    * <p>Creates an NDArray with the given dimensions with zero elements.</p>
    *
    * @param dims the dimensions of the NDArray
    * @return the NDArray
    */
   public NDArray zeros(@NonNull int... dims) {
      return zeros(Shape.shape(dims));
   }

   /**
    * <p>Creates an NDArray with the given Shape with zero elements.</p>
    *
    * @param shape the shape of the NDArray
    * @return the NDArray
    */
   public abstract NDArray zeros(@NonNull Shape shape);

}//END OF NDArrayFactory
