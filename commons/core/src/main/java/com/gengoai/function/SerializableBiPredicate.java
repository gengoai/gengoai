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
import java.util.function.BiPredicate;

/**
 * Version of BiPredicate that is serializable
 *
 * @param <T> Functional parameter
 * @param <U> Functional parameter
 */
@FunctionalInterface
public interface SerializableBiPredicate<T, U> extends BiPredicate<T, U>, Serializable {

   @Override
   default SerializableBiPredicate<T, U> negate() {
      return (t, u) -> !this.test(t, u);
   }

   @Override
   default SerializableBiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
      return (t, u) -> this.test(t, u) && other.test(t, u);
   }

   @Override
   default SerializableBiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
      return (t, u) -> this.test(t, u) || other.test(t, u);
   }

   default SerializableBiPredicate<T, U> and(SerializableBiPredicate<? super T, ? super U> other) {
      return (t, u) -> this.test(t, u) && other.test(t, u);
   }

   default SerializableBiPredicate<T, U> or(SerializableBiPredicate<? super T, ? super U> other) {
      return (t, u) -> this.test(t, u) || other.test(t, u);
   }

}//END OF SerializableBiPredicate