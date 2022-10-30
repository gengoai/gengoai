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

import com.gengoai.io.ResourceMonitor;
import lombok.NonNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.gengoai.stream.Streams.*;

/**
 * The type Reusable java long stream.
 */
final class ReusableJavaLongStream implements LongStream {
   /**
    * The Stream supplier.
    */
   final Supplier<LongStream> streamSupplier;

   /**
    * Instantiates a new Reusable java long stream.
    *
    * @param streamSupplier the stream supplier
    */
   ReusableJavaLongStream(Supplier<LongStream> streamSupplier) {
      this.streamSupplier = streamSupplier;
   }

   private static ReusableJavaLongStream n(Supplier<LongStream> streamSupplier) {
      return new ReusableJavaLongStream(streamSupplier);
   }


   @Override
   public LongStream filter(LongPredicate longPredicate) {
      return n(() -> streamSupplier.get().filter(longPredicate));
   }

   @Override
   public LongStream map(LongUnaryOperator longUnaryOperator) {
      return n(() -> streamSupplier.get().map(longUnaryOperator));
   }

   @Override
   public <U> Stream<U> mapToObj(LongFunction<? extends U> longFunction) {
      return reusableStream(() -> streamSupplier.get().mapToObj(longFunction));
   }

   @Override
   public IntStream mapToInt(LongToIntFunction longToIntFunction) {
      return reusableIntStream(() -> streamSupplier.get().mapToInt(longToIntFunction));
   }

   @Override
   public DoubleStream mapToDouble(LongToDoubleFunction longToDoubleFunction) {
      return reusableDoubleStream(() -> streamSupplier.get().mapToDouble(longToDoubleFunction));
   }

   @Override
   public LongStream flatMap(LongFunction<? extends LongStream> longFunction) {
      return n(() -> streamSupplier.get().flatMap(longFunction));
   }

   @Override
   public LongStream distinct() {
      return n(() -> streamSupplier.get().distinct());
   }

   @Override
   public LongStream sorted() {
      return n(() -> streamSupplier.get().sorted());
   }

   @Override
   public LongStream peek(LongConsumer longConsumer) {
      return n(() -> streamSupplier.get().peek(longConsumer));
   }

   @Override
   public LongStream limit(long l) {
      return n(() -> streamSupplier.get().limit(l));
   }

   @Override
   public LongStream skip(long l) {
      return n(() -> streamSupplier.get().skip(l));
   }

   @Override
   public void forEach(@NonNull LongConsumer longConsumer) {
      try(LongStream s = streamSupplier.get()) {
         s.forEach(longConsumer);
      }
   }

   @Override
   public void forEachOrdered(LongConsumer longConsumer) {
      try(LongStream s = streamSupplier.get()) {
         s.forEachOrdered(longConsumer);
      }
   }

   @Override
   public long[] toArray() {
      try(LongStream s = streamSupplier.get()) {
         return s.toArray();
      }
   }

   @Override
   public long reduce(long l, LongBinaryOperator longBinaryOperator) {
      try(LongStream s = streamSupplier.get()) {
         return s.reduce(l, longBinaryOperator);
      }
   }

   @Override
   public OptionalLong reduce(LongBinaryOperator longBinaryOperator) {
      try(LongStream s = streamSupplier.get()) {
         return s.reduce(longBinaryOperator);
      }
   }

   @Override
   public <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> objLongConsumer, BiConsumer<R, R> biConsumer) {
      try(LongStream s = streamSupplier.get()) {
         return s.collect(supplier, objLongConsumer, biConsumer);
      }
   }

   @Override
   public long sum() {
      try(LongStream s = streamSupplier.get()) {
         return s.sum();
      }
   }

   @Override
   public OptionalLong min() {
      try(LongStream s = streamSupplier.get()) {
         return s.min();
      }
   }

   @Override
   public OptionalLong max() {
      try(LongStream s = streamSupplier.get()) {
         return s.max();
      }
   }

   @Override
   public long count() {
      try(LongStream s = streamSupplier.get()) {
         return s.count();
      }
   }

   @Override
   public OptionalDouble average() {
      try(LongStream s = streamSupplier.get()) {
         return s.average();
      }
   }

   @Override
   public LongSummaryStatistics summaryStatistics() {
      try(LongStream s = streamSupplier.get()) {
         return s.summaryStatistics();
      }
   }

   @Override
   public boolean anyMatch(LongPredicate longPredicate) {
      try(LongStream s = streamSupplier.get()) {
         return s.anyMatch(longPredicate);
      }
   }

   @Override
   public boolean allMatch(LongPredicate longPredicate) {
      try(LongStream s = streamSupplier.get()) {
         return s.allMatch(longPredicate);
      }
   }

   @Override
   public boolean noneMatch(LongPredicate longPredicate) {
      try(LongStream s = streamSupplier.get()) {
         return s.noneMatch(longPredicate);
      }
   }

   @Override
   public OptionalLong findFirst() {
      try(LongStream s = streamSupplier.get()) {
         return s.findFirst();
      }
   }

   @Override
   public OptionalLong findAny() {
      try(LongStream s = streamSupplier.get()) {
         return s.findAny();
      }
   }

   @Override
   public DoubleStream asDoubleStream() {
      return reusableDoubleStream(() -> streamSupplier.get().asDoubleStream());
   }

   @Override
   public Stream<Long> boxed() {
      return reusableStream(() -> streamSupplier.get().boxed());
   }

   @Override
   public LongStream sequential() {
      return n(() -> streamSupplier.get().sequential());
   }

   @Override
   public LongStream parallel() {
      return n(() -> streamSupplier.get().parallel());
   }

   @Override
   public LongStream unordered() {
      return n(() -> streamSupplier.get().unordered());
   }

   @Override
   public LongStream onClose(Runnable runnable) {
      return n(() -> streamSupplier.get().onClose(runnable));
   }

   @Override
   public void close() {
   }

   @Override
   public PrimitiveIterator.OfLong iterator() {
      return ResourceMonitor.monitor(streamSupplier.get()).iterator();
   }

   @Override
   public Spliterator.OfLong spliterator() {
      return ResourceMonitor.monitor(streamSupplier.get()).spliterator();
   }

   @Override
   public boolean isParallel() {
      try(LongStream s = streamSupplier.get()) {
         return s.isParallel();
      }
   }
}//END OF ReusableJavaLongStream
