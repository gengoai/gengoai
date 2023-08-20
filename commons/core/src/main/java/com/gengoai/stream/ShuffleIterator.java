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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The type Shuffle iterator.
 *
 * @param <T> the type parameter
 */
public class ShuffleIterator<T> implements Iterator<T> {
   private final Iterator<T> backing;
   private final LinkedList<T> buffer = new LinkedList<>();
   private final Random random;

   /**
    * Instantiates a new Shuffle iterator.
    *
    * @param backing the backing
    */
   public ShuffleIterator(Iterator<T> backing) {
      this(backing, ThreadLocalRandom.current());
   }

   public ShuffleIterator(@NonNull Iterator<T> backing, @NonNull Random random) {
      this.backing = backing;
      this.random = random;
   }

   @Override
   public boolean hasNext() {
      return buffer.size() > 0 || backing.hasNext();
   }

   @Override
   public T next() {
      if(buffer.isEmpty()) {
         for(int i = 0; i < 500 && backing.hasNext(); i++) {
            buffer.add(backing.next());
         }
         Collections.shuffle(buffer, random);
      }
      if(backing.hasNext()) {
         if(random.nextDouble() < random.nextDouble()) {
            return buffer.removeFirst();
         }
         return backing.next();
      }
      return buffer.removeFirst();
   }
}//END OF ShuffleIterator
