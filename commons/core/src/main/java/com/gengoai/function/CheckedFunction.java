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

import java.io.Serializable;

import static com.gengoai.Validation.notNull;

/**
 * Version of Function that is serializable and checked
 *
 * @param <T> Functional parameter
 * @param <R> Functional parameter
 */
@FunctionalInterface
public interface CheckedFunction<T, R> extends Serializable {

   /**
    * Apply r.
    *
    * @param t the t
    * @return the r
    * @throws Throwable the throwable
    */
   R apply(T t) throws Throwable;

   /**
    * As consumer serializable consumer.
    *
    * @return the serializable consumer
    */
   default CheckedConsumer<T> asConsumer() {
      return this::apply;
   }

   /**
    * As consumer serializable consumer.
    *
    * @return the serializable consumer
    */
   static <T> CheckedConsumer<T> asConsumer(CheckedFunction<T, ?> function) {
      return notNull(function).asConsumer();
   }

   static <T, R> CheckedFunction<T, R> literal(R returnValue) {
      return t -> returnValue;
   }

   /**
    * Compose checked function.
    *
    * @param <V>    the type parameter
    * @param before the before
    * @return the checked function
    */
   default <V> CheckedFunction<V, R> compose(CheckedFunction<? super V, ? extends T> before) {
      return v -> apply(before.apply(v));
   }

   /**
    * And then checked function.
    *
    * @param <V>   the type parameter
    * @param after the after
    * @return the checked function
    */
   default <V> CheckedFunction<T, V> andThen(CheckedFunction<? super R, ? extends V> after) {
      return t -> after.apply(apply(t));
   }

   /**
    * Identity serializable function.
    *
    * @param <T> the type parameter
    * @return the serializable function
    */
   static <T> CheckedFunction<T, T> identity() {
      return t -> t;
   }

}//END OF CheckedFunction
