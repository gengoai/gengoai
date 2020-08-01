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
 *
 */

package com.gengoai.apollo.math.statistics;

import org.apache.mahout.math.set.OpenIntHashSet;
import org.apache.mahout.math.set.OpenLongHashSet;

import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * The type Sampling.
 *
 * @author David B. Bracewell
 */
public final class Sampling {

   /**
    * Uniform ints int stream.
    *
    * @param N               the n
    * @param lower           the lower
    * @param upper           the upper
    * @param withReplacement the with replacement
    * @return the int stream
    */
   public static IntStream uniformInts(int N, int lower, int upper, boolean withReplacement) {
      return uniformInts(N, lower, upper, withReplacement, ThreadLocalRandom.current());
   }

   /**
    * Uniform ints int stream.
    *
    * @param N               the n
    * @param lower           the lower
    * @param upper           the upper
    * @param withReplacement the with replacement
    * @param random          the random
    * @return the int stream
    */
   public static IntStream uniformInts(int N, int lower, int upper, boolean withReplacement, Random random) {
      IntStream ints = random.ints(lower, upper);
      if (withReplacement) {
         return ints.limit(N);
      }
      OpenIntHashSet seen = new OpenIntHashSet();
      PrimitiveIterator.OfInt iterator = ints.iterator();
      while (seen.size() < N) {
         seen.add(iterator.next());
      }
      return IntStream.of(seen.keys().elements());
   }


   /**
    * Uniform longs long [ ].
    *
    * @param N               the n
    * @param lower           the lower
    * @param upper           the upper
    * @param withReplacement the with replacement
    * @return the long [ ]
    */
   public static long[] uniformLongs(int N, long lower, long upper, boolean withReplacement) {
      return uniformLongs(N, lower, upper, withReplacement, ThreadLocalRandom.current());
   }

   /**
    * Uniform longs long [ ].
    *
    * @param N               the n
    * @param lower           the lower
    * @param upper           the upper
    * @param withReplacement the with replacement
    * @param random          the random
    * @return the long [ ]
    */
   public static long[] uniformLongs(int N, long lower, long upper, boolean withReplacement, Random random) {
      LongStream longs = random.longs(lower, upper);
      if (withReplacement) {
         return longs.toArray();
      }
      OpenLongHashSet seen = new OpenLongHashSet();
      PrimitiveIterator.OfLong iterator = longs.iterator();
      while (seen.size() < N) {
         seen.add(iterator.next());
      }
      return seen.keys().elements();
   }


}//END OF Sampling
