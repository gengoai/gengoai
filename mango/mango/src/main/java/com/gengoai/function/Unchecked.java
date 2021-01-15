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

import com.gengoai.conversion.Cast;

import java.util.function.LongFunction;

/**
 * <p>Wrappers to turn Checked functional classes into unchecked by throwing a Runtime exception.</p>
 */
public interface Unchecked {

   static <T> SerializableConsumer<T> consumer(CheckedConsumer<T> consumer,
                                               SerializableFunction<Throwable, Throwable> throwableHandler) {
      return t -> {
         try {
            consumer.accept(t);
         } catch (Throwable throwable) {
            sneakyThrow(throwable);
         }
      };
   }

   static void throwChecked(Throwable throwable) {
      Unchecked.<RuntimeException>sneakyThrow(throwable);
   }

   static <E extends Throwable> void sneakyThrow(Throwable throwable) throws E {
      throw Cast.<E>as(throwable);
   }

   static SerializableRunnable runnable(CheckedRunnable runnable) {
      return () -> {
         try {
            runnable.run();
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }

   /**
    * Generates a version of DoubleToIntFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableDoubleToIntFunction doubleToIntFunction(CheckedDoubleToIntFunction checked) {
      return (t) -> {
         try {
            return checked.applyAsInt(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of IntToDoubleFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableIntToDoubleFunction intToDoubleFunction(CheckedIntToDoubleFunction checked) {
      return (t) -> {
         try {
            return checked.applyAsDouble(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of Consumer that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableConsumer<T> consumer(CheckedConsumer<T> checked) {
      return (t) -> {
         try {
            checked.accept(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of IntPredicate that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableIntPredicate intPredicate(CheckedIntPredicate checked) {
      return (t) -> {
         try {
            return checked.test(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of ObjLongConsumer that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableObjLongConsumer<T> objLongConsumer(CheckedObjLongConsumer<T> checked) {
      return (t, value) -> {
         try {
            checked.accept(t, value);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of BiPredicate that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param <U>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T, U> SerializableBiPredicate<T, U> biPredicate(CheckedBiPredicate<T, U> checked) {
      return (t, U) -> {
         try {
            return checked.test(t, U);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of DoubleUnaryOperator that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableDoubleUnaryOperator doubleUnaryOperator(CheckedDoubleUnaryOperator checked) {
      return (t) -> {
         try {
            return checked.applyAsDouble(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of IntUnaryOperator that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableIntUnaryOperator intUnaryOperator(CheckedIntUnaryOperator checked) {
      return (t) -> {
         try {
            return checked.applyAsInt(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of LongUnaryOperator that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableLongUnaryOperator longUnaryOperator(CheckedLongUnaryOperator checked) {
      return (t) -> {
         try {
            return checked.applyAsLong(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of BooleanSupplier that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableBooleanSupplier booleanSupplier(CheckedBooleanSupplier checked) {
      return () -> {
         try {
            return checked.getAsBoolean();
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of IntSupplier that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableIntSupplier intSupplier(CheckedIntSupplier checked) {
      return () -> {
         try {
            return checked.getAsInt();
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of IntBinaryOperator that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableIntBinaryOperator intBinaryOperator(CheckedIntBinaryOperator checked) {
      return (t, u) -> {
         try {
            return checked.applyAsInt(t, u);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of ObjIntConsumer that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableObjIntConsumer<T> objIntConsumer(CheckedObjIntConsumer<T> checked) {
      return (t, value) -> {
         try {
            checked.accept(t, value);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of LongBinaryOperator that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableLongBinaryOperator longBinaryOperator(CheckedLongBinaryOperator checked) {
      return (t, u) -> {
         try {
            return checked.applyAsLong(t, u);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of UnaryOperator that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableUnaryOperator<T> unaryOperator(CheckedUnaryOperator<T> checked) {
      return (t) -> {
         try {
            return checked.apply(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of BinaryOperator that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableBinaryOperator<T> binaryOperator(CheckedBinaryOperator<T> checked) {
      return (t, u) -> {
         try {
            return checked.apply(t, u);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of Predicate that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializablePredicate<T> predicate(CheckedPredicate<T> checked) {
      return (t) -> {
         try {
            return checked.test(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of ToDoubleFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableToDoubleFunction<T> toDoubleFunction(CheckedToDoubleFunction<T> checked) {
      return (t) -> {
         try {
            return checked.applyAsDouble(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of Supplier that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableSupplier<T> supplier(CheckedSupplier<T> checked) {
      return () -> {
         try {
            return checked.get();
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of ToDoubleBiFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param <U>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T, U> SerializableToDoubleBiFunction<T, U> toDoubleBiFunction(CheckedToDoubleBiFunction<T, U> checked) {
      return (t, u) -> {
         try {
            return checked.applyAsDouble(t, u);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of LongPredicate that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableLongPredicate longPredicate(CheckedLongPredicate checked) {
      return (t) -> {
         try {
            return checked.test(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of BiConsumer that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param <U>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T, U> SerializableBiConsumer<T, U> biConsumer(CheckedBiConsumer<T, U> checked) {
      return (t, u) -> {
         try {
            checked.accept(t, u);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of LongSupplier that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableLongSupplier longSupplier(CheckedLongSupplier checked) {
      return () -> {
         try {
            return checked.getAsLong();
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of ToLongFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableToLongFunction<T> toLongFunction(CheckedToLongFunction<T> checked) {
      return (t) -> {
         try {
            return checked.applyAsLong(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of IntFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <R>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <R> SerializableIntFunction<R> intFunction(CheckedIntFunction<R> checked) {
      return (t) -> {
         try {
            return checked.apply(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of IntConsumer that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableIntConsumer intConsumer(CheckedIntConsumer checked) {
      return (t) -> {
         try {
            checked.accept(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of BiFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param <U>     Functional parameter
    * @param <R>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T, U, R> SerializableBiFunction<T, U, R> biFunction(CheckedBiFunction<T, U, R> checked) {
      return (t, u) -> {
         try {
            return checked.apply(t, u);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of LongToDoubleFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableLongToDoubleFunction longToDoubleFunction(CheckedLongToDoubleFunction checked) {
      return (t) -> {
         try {
            return checked.applyAsDouble(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of DoubleBinaryOperator that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableDoubleBinaryOperator doubleBinaryOperator(CheckedDoubleBinaryOperator checked) {
      return (t, u) -> {
         try {
            return checked.applyAsDouble(t, u);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of LongFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <R>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <R> LongFunction<R> longFunction(CheckedLongFunction<R> checked) {
      return (t) -> {
         try {
            return checked.apply(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of LongToIntFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableLongToIntFunction longToIntFunction(CheckedLongToIntFunction checked) {
      return (t) -> {
         try {
            return checked.applyAsInt(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of ToLongBiFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param <U>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T, U> SerializableToLongBiFunction<T, U> toLongBiFunction(CheckedToLongBiFunction<T, U> checked) {
      return (t, u) -> {
         try {
            return checked.applyAsLong(t, u);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of DoublePredicate that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableDoublePredicate doublePredicate(CheckedDoublePredicate checked) {
      return (t) -> {
         try {
            return checked.test(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of DoubleFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <R>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <R> SerializableDoubleFunction<R> doubleFunction(CheckedDoubleFunction<R> checked) {
      return (t) -> {
         try {
            return checked.apply(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of LongConsumer that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableLongConsumer longConsumer(CheckedLongConsumer checked) {
      return (t) -> {
         try {
            checked.accept(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of Function that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param <R>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T, R> SerializableFunction<T, R> function(CheckedFunction<T, R> checked) {
      return (t) -> {
         try {
            return checked.apply(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of DoubleToLongFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableDoubleToLongFunction doubleToLongFunction(CheckedDoubleToLongFunction checked) {
      return (t) -> {
         try {
            return checked.applyAsLong(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of DoubleConsumer that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableDoubleConsumer doubleConsumer(CheckedDoubleConsumer checked) {
      return (t) -> {
         try {
            checked.accept(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of IntToLongFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableIntToLongFunction intToLongFunction(CheckedIntToLongFunction checked) {
      return (t) -> {
         try {
            return checked.applyAsLong(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of ToIntFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableToIntFunction<T> toIntFunction(CheckedToIntFunction<T> checked) {
      return (t) -> {
         try {
            return checked.applyAsInt(t);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of DoubleSupplier that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param checked The checked functional
    * @return The checked functional.
    */
   static SerializableDoubleSupplier doubleSupplier(CheckedDoubleSupplier checked) {
      return () -> {
         try {
            return checked.getAsDouble();
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of ToIntBiFunction that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param <U>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T, U> SerializableToIntBiFunction<T, U> toIntBiFunction(CheckedToIntBiFunction<T, U> checked) {
      return (t, u) -> {
         try {
            return checked.applyAsInt(t, u);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


   /**
    * Generates a version of ObjDoubleConsumer that will capture exceptions and rethrow them as runtime exceptions
    *
    * @param <T>     Functional parameter
    * @param checked The checked functional
    * @return The checked functional.
    */
   static <T> SerializableObjDoubleConsumer<T> objDoubleConsumer(CheckedObjDoubleConsumer<T> checked) {
      return (t, value) -> {
         try {
            checked.accept(t, value);
         } catch (Throwable e) {
            throwChecked(e);
            throw new IllegalStateException(e);
         }
      };
   }


}//END OF Unchecked