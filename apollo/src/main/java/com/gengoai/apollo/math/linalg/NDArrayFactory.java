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
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.function.SerializableFunction;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.function.Supplier;

import static com.gengoai.Validation.checkArgument;
import static com.gengoai.Validation.notNull;
import static com.gengoai.apollo.math.linalg.nd.*;
import static com.gengoai.tuple.Tuples.$;

public abstract class NDArrayFactory<T> implements Serializable {
   private static final long serialVersionUID = 1L;
   protected final Class<?> target;
   protected final Class<?>[] dims;

   public NDArrayFactory(Class<T> c) {
      this.target = Primitives.unwrap(c);
      this.dims = new Class<?>[]{
            Array.newInstance(target, 0).getClass(),
            Array.newInstance(target, 0, 0).getClass(),
            Array.newInstance(target, 0, 0, 0).getClass(),
            Array.newInstance(target, 0, 0, 0, 0).getClass()};
   }


   public static <V> NDArrayFactory<V> forType(@NonNull Class<V> type, boolean sparse) {
      type = Primitives.wrap(type);
      if (type == Float.class) {
         if (sparse) {
            return Cast.as(SFLOAT32);
         }
         return Cast.as(DFLOAT32);
      } else if (type == Integer.class) {
         return Cast.as(DINT32);
      } else if (type == Long.class) {
         return Cast.as(DINT64);
      } else if (type == String.class) {
         if (sparse) {
            return Cast.as(SSTRING);
         }
         return Cast.as(DSTRING);
      }
      throw new IllegalArgumentException("No Factory for type '" + type.getSimpleName() + "'");
   }

   public static <V> NDArrayFactory<V> forType(@NonNull Class<V> type) {
      return forType(type, Config.get("ndarray.sparse").asBooleanValue(false));
   }


   protected static Tuple2<Integer, Class<?>> probe(Object o) {
      Class<?> c = o.getClass();
      int depth = 0;
      while (c.isArray()) {
         depth++;
         c = c.getComponentType();
      }
      return $(depth, Primitives.unwrap(c));
   }

   protected static <T, V> SerializableFunction<Object, NDArray<V>>
   safe(Class<T> target, SerializableFunction<T, NDArray<V>> toWrap) {
      return o -> {
         try {
            return toWrap.apply(Converter.convert(o, target));
         } catch (Exception e) {
            return null;
         }
      };
   }

   public final NDArray<T> arange(double start, double end) {
      return arange(start, end, 1);
   }

   public final NDArray<T> arange(double start, double end, double increment) {
      int length = (int) Math.floor((end - start) / increment);
      if ((start + (length * increment)) < end) {
         length++;
      }
      return arange(Shape.shape(length), start, increment);
   }

   public NDArray<T> arange(@NonNull Shape shape, double start) {
      return arange(shape, start, 1);
   }

   public NDArray<T> arange(@NonNull Shape shape) {
      return arange(shape, 0, 1);
   }

   public NDArray<T> arange(@NonNull Shape shape, double start, double increment) {
      NDArray<T> zero = zeros(shape);
      double value = start;
      for (Index index : shape.range()) {
         if (getType() == String.class) {
            zero.set(index, Cast.as(Double.toString(value)));
         } else {
            zero.set(index, value);
         }
         value += increment;
      }
      return zero;
   }

   public NDArray<T> array(@NonNull Shape shape, Object data) {
      if (data == null && shape.isEmpty()) {
         return empty();
      }
      notNull(data, "Unable to construct NDArray<" + getType().getSimpleName() + "> from 'null' object");

      Tuple2<Integer, Class<?>> probe = probe(data);
      checkArgument(probe.v1 <= 4, "Expecting an a 1 to 4 dimension array.");

      if (probe.v2 != target) {
         data = Converter.convertSilently(data, dims[probe.v1 - 1]);
      }
      notNull(data, "Unable to construct NDArray<" + getType().getSimpleName() + "> from " + probe.v2.getSimpleName());

      NDArray<T> out = fromShapedArray(shape, data, probe.v1);

      checkArgument(shape.equals(out.shape()),
                    () -> "Unable to construct NDArray of shape " +
                          shape +
                          " from " +
                          out.shape());
      return out;
   }

   public NDArray<T> array(@NonNull Object data) {
      if (data == null) {
         return empty();
      }

      Tuple2<Integer, Class<?>> probe = probe(data);
      checkArgument(probe.v1 <= 4, "Expecting an a 1 to 4 dimension array.");

      if (probe.v2 != target) {
         if (probe.v1 == 0) {
            data = Converter.convertSilently(data, target);
         } else {
            data = Converter.convertSilently(data, dims[probe.v1 - 1]);
         }
      }
      notNull(data, "Unable to construct NDArray<" + getType().getSimpleName() + "> from " + probe.v2.getSimpleName());

      return fromArray(data, probe.v1);
   }

   public NDArray<T> array(@NonNull Shape shape, long[] a) {
      return array(shape, (Object) a);
   }

   public NDArray<T> array(long[] a) {
      return array((Object) a);
   }

   public NDArray<T> array(long[][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(long[][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(long[][][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(@NonNull Shape shape, int[] a) {
      return array(shape, (Object) a);
   }

   public NDArray<T> array(int[] a) {
      return array((Object) a);
   }

   public NDArray<T> array(int[][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(int[][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(int[][][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(@NonNull Shape shape, float[] a) {
      return array(shape, (Object) a);
   }

   public NDArray<T> array(float[] a) {
      return array((Object) a);
   }

   public NDArray<T> array(float[][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(float[][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(float[][][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(@NonNull Shape shape, double[] a) {
      return array(shape, (Object) a);
   }

   public NDArray<T> array(double[] a) {
      return array((Object) a);
   }

   public NDArray<T> array(double[][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(double[][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(double[][][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(@NonNull Shape shape, boolean[] a) {
      return array(shape, (Object) a);
   }

   public NDArray<T> array(boolean[] a) {
      return array((Object) a);
   }

   public NDArray<T> array(boolean[][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(boolean[][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(boolean[][][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(@NonNull Shape shape, String[] a) {
      return array(shape, (Object) a);
   }

   public NDArray<T> array(String[] a) {
      return array((Object) a);
   }

   public NDArray<T> array(String[][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(String[][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(String[][][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(Object[] a) {
      return array((Object) a);
   }

   public NDArray<T> array(Object[][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(Object[][][] a) {
      return array((Object) a);
   }

   public NDArray<T> array(Object[][][][] a) {
      return array((Object) a);
   }

   public NDArray<T> create(@NonNull Shape shape, @NonNull NDArrayInitializer<? super T> initializer) {
      NDArray<T> zeros = zeros(shape);
      initializer.accept(Cast.as(zeros));
      return zeros;
   }

   public NDArray<T> create(int length, @NonNull NDArrayInitializer<? super T> initializer) {
      return create(Shape.shape(length), initializer);
   }

   public NDArray<T> create(int rows, int columns, @NonNull NDArrayInitializer<? super T> initializer) {
      return create(Shape.shape(rows, columns), initializer);
   }

   public NDArray<T> create(int channels, int rows, int columns, @NonNull NDArrayInitializer<? super T> initializer) {
      return create(Shape.shape(channels, rows, columns), initializer);
   }

   public NDArray<T> create(int kernels, int channels, int rows, int columns, @NonNull NDArrayInitializer<? super T> initializer) {
      return create(Shape.shape(kernels, channels, rows, columns), initializer);
   }

   public NDArray<T> create(int length, @NonNull Supplier<? extends T> initializer) {
      return create(Shape.shape(length), initializer);
   }

   public NDArray<T> create(int rows, int columns, @NonNull Supplier<? extends T> initializer) {
      return create(Shape.shape(rows, columns), initializer);
   }

   public NDArray<T> create(int channels, int rows, int columns, @NonNull Supplier<? extends T> initializer) {
      return create(Shape.shape(channels, rows, columns), initializer);
   }

   public NDArray<T> create(int kernels, int channels, int rows, int columns, @NonNull Supplier<? extends T> initializer) {
      return create(Shape.shape(kernels, channels, rows, columns), initializer);
   }

   public NDArray<T> create(@NonNull Shape shape, @NonNull Supplier<? extends T> generator) {
      NDArray<T> out = zeros(shape);
      for (Index index : shape.range()) {
         out.set(index, generator.get());
      }
      return out;
   }

   public NDArray<T> empty() {
      return zeros(Shape.shape(0));
   }

   public NDArray<T> eye(int size) {
      return zeros(Shape.shape(size, size));
   }

   protected abstract NDArray<T> fromArray(Object array, int depth);

   protected abstract NDArray<T> fromShapedArray(Shape shape, Object array, int depth);

   public final Class<T> getType() {
      return Cast.as(Primitives.wrap(target));
   }

   public NDArray<T> ones(@NonNull Shape shape) {
      return zeros(shape).fill(1);
   }

   public NDArray<T> ones(@NonNull int... dims) {
      return ones(Shape.shape(dims));
   }

   public NDArray<T> scalar(double value) {
      if (getType() == String.class) {
         return zeros(Shape.shape(1)).set(0, Cast.as(Double.toString(value)));
      }
      return zeros(Shape.shape(1)).set(0, value);
   }

   public NDArray<T> scalar(T value) {
      return zeros(Shape.shape(1)).set(0, value);
   }

   public NDArray<T> zeros(@NonNull int... dims) {
      return zeros(Shape.shape(dims));
   }

   public abstract NDArray<T> zeros(@NonNull Shape shape);

}//END OF NDArrayFactory
