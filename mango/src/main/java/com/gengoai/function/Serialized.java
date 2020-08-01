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
 */

package com.gengoai.function;

import java.util.function.*;

/**
 * The interface Serialized.
 */
public interface Serialized {

   /**
    * Runnable runnable.
    *
    * @param runnable the runnable
    * @return the runnable
    */
   static Runnable runnable(SerializableRunnable runnable) {
      return runnable;
   }

   /**
    * And serializable predicate.
    *
    * @param <T> the type parameter
    * @param p1  the p 1
    * @param p2  the p 2
    * @return the serializable predicate
    */
   static <T> SerializablePredicate<T> and(SerializablePredicate<? super T> p1, SerializablePredicate<? super T> p2) {
      return t -> p1.test(t) && p2.test(t);
   }

   /**
    * Or serializable predicate.
    *
    * @param <T> the type parameter
    * @param p1  the p 1
    * @param p2  the p 2
    * @return the serializable predicate
    */
   static <T> SerializablePredicate<T> or(SerializablePredicate<? super T> p1, SerializablePredicate<? super T> p2) {
      return t -> p1.test(t) || p2.test(t);
   }

   /**
    * Negate serializable predicate.
    *
    * @param <T> the type parameter
    * @param p1  the p 1
    * @return the serializable predicate
    */
   static <T> SerializablePredicate<T> negate(SerializablePredicate<? super T> p1) {
      return t -> !p1.test(t);
   }


   /**
    * And serializable bi predicate.
    *
    * @param <T> the type parameter
    * @param <U> the type parameter
    * @param p1  the p 1
    * @param p2  the p 2
    * @return the serializable bi predicate
    */
   static <T, U> SerializableBiPredicate<T, U> and(SerializableBiPredicate<? super T, ? super U> p1,
                                                   SerializableBiPredicate<? super T, ? super U> p2) {
      return (t, u) -> p1.test(t, u) && p2.test(t, u);
   }

   /**
    * Or serializable bi predicate.
    *
    * @param <T> the type parameter
    * @param <U> the type parameter
    * @param p1  the p 1
    * @param p2  the p 2
    * @return the serializable bi predicate
    */
   static <T, U> SerializableBiPredicate<T, U> or(SerializableBiPredicate<? super T, ? super U> p1,
                                                  SerializableBiPredicate<? super T, ? super U> p2) {
      return (t, u) -> p1.test(t, u) || p2.test(t, u);
   }

   /**
    * Negate serializable bi predicate.
    *
    * @param <T> the type parameter
    * @param <U> the type parameter
    * @param p1  the p 1
    * @return the serializable bi predicate
    */
   static <T, U> SerializableBiPredicate<T, U> negate(SerializableBiPredicate<? super T, ? super U> p1) {
      return (t, u) -> !p1.test(t, u);
   }

   /**
    * Generates a serialized version of DoubleToIntFunction
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static DoubleToIntFunction doubleToIntFunction(SerializableDoubleToIntFunction serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of IntToDoubleFunction
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static IntToDoubleFunction intToDoubleFunction(SerializableIntToDoubleFunction serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of Consumer
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> Consumer<T> consumer(SerializableConsumer<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of IntPredicate
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static IntPredicate intPredicate(SerializableIntPredicate serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of ObjLongConsumer
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> ObjLongConsumer<T> objLongConsumer(SerializableObjLongConsumer<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of BiPredicate
    *
    * @param <T>        Functional parameter
    * @param <U>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T, U> BiPredicate<T, U> biPredicate(SerializableBiPredicate<T, U> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of DoubleUnaryOperator
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static DoubleUnaryOperator doubleUnaryOperator(SerializableDoubleUnaryOperator serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of IntUnaryOperator
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static IntUnaryOperator intUnaryOperator(SerializableIntUnaryOperator serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of LongUnaryOperator
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static LongUnaryOperator longUnaryOperator(SerializableLongUnaryOperator serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of BooleanSupplier
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static BooleanSupplier booleanSupplier(SerializableBooleanSupplier serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of IntSupplier
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static IntSupplier intSupplier(SerializableIntSupplier serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of IntBinaryOperator
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static IntBinaryOperator intBinaryOperator(SerializableIntBinaryOperator serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of ObjIntConsumer
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> ObjIntConsumer<T> objIntConsumer(SerializableObjIntConsumer<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of LongBinaryOperator
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static LongBinaryOperator longBinaryOperator(SerializableLongBinaryOperator serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of UnaryOperator
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> UnaryOperator<T> unaryOperator(SerializableUnaryOperator<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of BinaryOperator
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> BinaryOperator<T> binaryOperator(SerializableBinaryOperator<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of Predicate
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> Predicate<T> predicate(SerializablePredicate<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of ToDoubleFunction
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> ToDoubleFunction<T> toDoubleFunction(SerializableToDoubleFunction<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of Supplier
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> Supplier<T> supplier(SerializableSupplier<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of ToDoubleBiFunction
    *
    * @param <T>        Functional parameter
    * @param <U>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T, U> ToDoubleBiFunction<T, U> toDoubleBiFunction(SerializableToDoubleBiFunction<T, U> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of LongPredicate
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static LongPredicate longPredicate(SerializableLongPredicate serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of BiConsumer
    *
    * @param <T>        Functional parameter
    * @param <U>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T, U> BiConsumer<T, U> biConsumer(SerializableBiConsumer<T, U> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of LongSupplier
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static LongSupplier longSupplier(SerializableLongSupplier serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of ToLongFunction
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> ToLongFunction<T> toLongFunction(SerializableToLongFunction<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of IntFunction
    *
    * @param <R>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <R> IntFunction<R> intFunction(SerializableIntFunction<R> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of IntConsumer
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static IntConsumer intConsumer(SerializableIntConsumer serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of BiFunction
    *
    * @param <T>        Functional parameter
    * @param <U>        Functional parameter
    * @param <R>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T, U, R> BiFunction<T, U, R> biFunction(SerializableBiFunction<T, U, R> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of LongToDoubleFunction
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static LongToDoubleFunction longToDoubleFunction(SerializableLongToDoubleFunction serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of DoubleBinaryOperator
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static DoubleBinaryOperator doubleBinaryOperator(SerializableDoubleBinaryOperator serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of LongFunction
    *
    * @param <R>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <R> LongFunction<R> longFunction(SerializableLongFunction<R> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of LongToIntFunction
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static LongToIntFunction longToIntFunction(SerializableLongToIntFunction serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of ToLongBiFunction
    *
    * @param <T>        Functional parameter
    * @param <U>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T, U> ToLongBiFunction<T, U> toLongBiFunction(SerializableToLongBiFunction<T, U> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of DoublePredicate
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static DoublePredicate doublePredicate(SerializableDoublePredicate serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of DoubleFunction
    *
    * @param <R>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <R> DoubleFunction<R> doubleFunction(SerializableDoubleFunction<R> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of LongConsumer
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static LongConsumer longConsumer(SerializableLongConsumer serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of Function
    *
    * @param <T>        Functional parameter
    * @param <R>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T, R> Function<T, R> function(SerializableFunction<T, R> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of DoubleToLongFunction
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static DoubleToLongFunction doubleToLongFunction(SerializableDoubleToLongFunction serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of DoubleConsumer
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static DoubleConsumer doubleConsumer(SerializableDoubleConsumer serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of IntToLongFunction
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static IntToLongFunction intToLongFunction(SerializableIntToLongFunction serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of ToIntFunction
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> ToIntFunction<T> toIntFunction(SerializableToIntFunction<T> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of DoubleSupplier
    *
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static DoubleSupplier doubleSupplier(SerializableDoubleSupplier serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of ToIntBiFunction
    *
    * @param <T>        Functional parameter
    * @param <U>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T, U> ToIntBiFunction<T, U> toIntBiFunction(SerializableToIntBiFunction<T, U> serialized) {
      return serialized;
   }


   /**
    * Generates a serialized version of ObjDoubleConsumer
    *
    * @param <T>        Functional parameter
    * @param serialized The serialized functional
    * @return The serialized functional.
    */
   static <T> ObjDoubleConsumer<T> objDoubleConsumer(SerializableObjDoubleConsumer<T> serialized) {
      return serialized;
   }


}//END OF Serialized