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
 * Version of Consumer that is serializable and checked
 *
 * @param <T> Functional parameter
 */
@FunctionalInterface
public interface CheckedConsumer<T> extends Serializable {

   /**
    * Accept.
    *
    * @param t the t
    * @throws Throwable the throwable
    */
   void accept(T t) throws Throwable;

   /**
    * As consumer serializable consumer.
    *
    * @return the serializable consumer
    */
   default <R> CheckedFunction<T, R> asFunction(R returnValue) {
      return t -> {
         this.accept(t);
         return returnValue;
      };
   }

   /**
    * As consumer serializable consumer.
    *
    * @return the serializable consumer
    */
   default <R> CheckedFunction<T, R> asFunction() {
      return asFunction(null);
   }

   static <T, R> CheckedFunction<T, R> asFunction(CheckedConsumer<T> consumer, R returnValue) {
      return notNull(consumer).asFunction(returnValue);
   }

   static <T, R> CheckedFunction<T, R> asFunction(CheckedConsumer<T> consumer) {
      return asFunction(consumer, null);
   }


}//END OF CheckedConsumer
