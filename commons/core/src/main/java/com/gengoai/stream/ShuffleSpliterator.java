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

package com.gengoai.stream;

import lombok.NonNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * The type Shuffle spliterator.
 *
 * @param <T> the type parameter
 */
public class ShuffleSpliterator<T> implements Spliterator<T> {
   private final Spliterator<T> backing;
   private final List<T> buffer = new ArrayList<>();
   private final Random random;

   /**
    * Instantiates a new Shuffle spliterator.
    *
    * @param backing the backing
    */
   public ShuffleSpliterator(Spliterator<T> backing) {
      this(backing, ThreadLocalRandom.current());
   }

   /**
    * Instantiates a new Shuffle spliterator.
    *
    * @param backing the backing
    */
   public ShuffleSpliterator(@NonNull Spliterator<T> backing, @NonNull Random random) {
      this.backing = backing;
      this.random = random;
   }


   @Override
   public boolean tryAdvance(Consumer<? super T> consumer) {
      if(buffer.isEmpty()) {
         while(buffer.size() < 500 && backing.tryAdvance(buffer::add)) {
            //Noop
         }
         Collections.shuffle(buffer);
      }
      if(buffer.isEmpty()) {
         return backing.tryAdvance(consumer);
      }
      if(random.nextDouble() < random.nextDouble()) {
         consumer.accept(buffer.remove(buffer.size() - 1));
         return true;
      }
      if(!backing.tryAdvance(consumer)) {
         consumer.accept(buffer.remove(buffer.size() - 1));
         return true;
      }
      return true;
   }

   @Override
   public Spliterator<T> trySplit() {
      Spliterator<T> split = backing.trySplit();
      if(split != null) {
         return new ShuffleSpliterator<>(split);
      }
      return null;
   }

   @Override
   public long estimateSize() {
      return backing.estimateSize();
   }

   @Override
   public int characteristics() {
      return CONCURRENT;
   }
}//END OF ShuffleSpliterator
