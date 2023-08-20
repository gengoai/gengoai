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

package com.gengoai.stream.local;

import com.gengoai.Validation;
import com.gengoai.collection.Iterators;
import com.gengoai.conversion.Cast;
import com.gengoai.function.*;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.Streams;
import com.gengoai.stream.*;
import lombok.NonNull;

import java.io.BufferedWriter;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.*;

import static com.gengoai.tuple.Tuples.$;

/**
 * The type Base java stream.
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
abstract class AbstractLocalMStream<T> implements MStream<T>, Serializable {
   private static final long serialVersionUID = 1L;

   public static void main(String[] args) {
      Iterator<String> si = new ShuffleIterator<>(
            IntStream.range(33, 126)
                     .mapToObj(Character::toString)
                     .collect(Collectors.toList())
                     .iterator()
      );
      Stream<String> stream = Streams.asStream(si);
      stream.forEach(System.out::println);
   }

   @Override
   public MStream<T> cache() {
      return new LocalInMemoryMStream<>(collect());
   }

   @Override
   public void close() throws Exception {
      javaStream().close();
   }

   @Override
   public <R> R collect(@NonNull Collector<? super T, ?, R> collector) {
      return javaStream().collect(collector);
   }

   @Override
   public List<T> collect() {
      return collect(Collectors.toList());
   }

   @Override
   public long count() {
      return javaStream().count();
   }

   @Override
   public Map<T, Long> countByValue() {
      return javaStream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
   }

   @Override
   public MStream<T> distinct() {
      return getContext().stream(javaStream().distinct());
   }

   @Override
   public MStream<T> filter(@NonNull SerializablePredicate<? super T> predicate) {
      return getContext().stream(javaStream().filter(predicate));
   }

   @Override
   public Optional<T> first() {
      return javaStream().findFirst();
   }

   @Override
   public <R> MStream<R> flatMap(@NonNull SerializableFunction<? super T, Stream<? extends R>> mapper) {
      return getContext().stream(javaStream().flatMap(mapper));
   }

   @Override
   public <R, U> MPairStream<R, U> flatMapToPair(SerializableFunction<? super T, Stream<? extends Map.Entry<? extends R, ? extends U>>> function) {
      return new LocalDefaultMPairStream<>(Cast.as(flatMap(function)));
   }

   @Override
   public T fold(T zeroValue, @NonNull SerializableBinaryOperator<T> operator) {
      return javaStream().reduce(zeroValue, operator);
   }

   @Override
   public void forEach(@NonNull SerializableConsumer<? super T> consumer) {
      javaStream().forEach(consumer);
   }

   @Override
   public void forEachLocal(@NonNull SerializableConsumer<? super T> consumer) {
      javaStream().forEach(consumer);
   }

   @Override
   public StreamingContext getContext() {
      return LocalStreamingContext.INSTANCE;
   }

   @Override
   public <U> MPairStream<U, Iterable<T>> groupBy(SerializableFunction<? super T, ? extends U> function) {
      return new LocalDefaultMPairStream<>(Cast.as(new LocalReusableMStream<>(() -> javaStream().collect(
            Collectors.groupingBy(function)).entrySet().stream())));
   }

   @Override
   public MStream<T> intersection(MStream<T> other) {
      if(other.isDistributed()) {
         return other.intersection(this);
      }
      final Set<T> set = other.collect(Collectors.toSet());
      return filter(set::contains);
   }

   @Override
   public boolean isDistributed() {
      return false;
   }

   @Override
   public boolean isEmpty() {
      return count() == 0;
   }

   @Override
   public Iterator<T> iterator() {
      return javaStream().iterator();
   }

   @Override
   public MStream<T> limit(long number) {
      return getContext().stream(javaStream().limit(number));
   }

   @Override
   public <R> MStream<R> map(SerializableFunction<? super T, ? extends R> function) {
      return getContext().stream(javaStream().map(function));
   }

   @Override
   public MDoubleStream mapToDouble(@NonNull SerializableToDoubleFunction<? super T> function) {
      return new LocalMDoubleStream(javaStream().mapToDouble(function));
   }

   @Override
   public <R, U> MPairStream<R, U> mapToPair(@NonNull SerializableFunction<? super T, ? extends Map.Entry<? extends R, ? extends U>> function) {
      return new LocalDefaultMPairStream<>(Cast.as(map(function)));
   }

   @Override
   public Optional<T> max(@NonNull SerializableComparator<? super T> comparator) {
      return javaStream().max(comparator);
   }

   @Override
   public Optional<T> min(@NonNull SerializableComparator<? super T> comparator) {
      return javaStream().min(comparator);
   }

   @Override
   public MStream<T> onClose(SerializableRunnable closeHandler) {
      return getContext().stream(javaStream().onClose(closeHandler));
   }

   @Override
   public MStream<T> parallel() {
      return getContext().stream(javaStream().parallel());
   }

   @Override
   public MStream<Stream<T>> partition(long partitionSize) {
      return getContext().stream(Streams.partition(javaStream(), partitionSize));
   }

   @Override
   public MStream<T> persist(@NonNull StorageLevel storageLevel) {
      return new LocalInMemoryMStream<>(collect());
   }

   @Override
   public Optional<T> reduce(@NonNull SerializableBinaryOperator<T> reducer) {
      return javaStream().reduce(reducer);
   }

   @Override
   public MStream<T> repartition(int numPartitions) {
      return this;
   }

   @Override
   public MStream<T> sample(boolean withReplacement, int number) {
      Validation.checkArgument(number >= 0, "Sample size must be non-negative.");
      if(number == 0) {
         return StreamingContext.local().empty();
      }
      if(withReplacement) {
         return cache().sample(true, number);
      } else {
         return shuffle(new Random(10)).limit(number);
      }
   }

   @Override
   public void saveAsTextFile(Resource location) {
      try(BufferedWriter writer = new BufferedWriter(location.writer())) {
         AtomicLong lineCounter = new AtomicLong();
         javaStream().forEach(Unchecked.consumer(o -> {
            writer.write(o.toString());
            writer.newLine();
            if(lineCounter.incrementAndGet() % 500 == 0) {
               writer.flush();
            }
         }));
      } catch(Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public MStream<T> shuffle(@NonNull Random random) {
      if(javaStream().isParallel()) {
         return getContext().stream(Streams.reusableStream(() -> StreamSupport.stream(new ShuffleSpliterator<>(
                                                                                            javaStream().spliterator(),
                                                                                            random),
                                                                                      true)));
      }
      return getContext().stream(Streams.reusableStream(() -> Streams.asStream(new ShuffleIterator<>(javaStream().iterator(),
                                                                                                     random))));
   }

   @Override
   public MStream<T> skip(long n) {
      return getContext().stream(javaStream().skip(n));
   }

   @Override
   public <R extends Comparable<R>> MStream<T> sortBy(boolean ascending,
                                                      @NonNull SerializableFunction<? super T, ? extends R> keyFunction) {
      final Comparator<T> comparator = ascending
                                       ? Comparator.comparing(keyFunction)
                                       : Cast.as(Comparator.comparing(keyFunction).reversed());
      return getContext().stream(javaStream().sorted(comparator));
   }

   @Override
   public List<T> take(int n) {
      Validation.checkArgument(n >= 0, "N must be non-negative.");
      if(n == 0) {
         return Collections.emptyList();
      }
      List<T> list = new ArrayList<>();
      Iterator<T> itr = iterator();
      for(int i = 0; i < n && itr.hasNext(); i++) {
         list.add(itr.next());
      }
      return list;
   }

   @Override
   public MStream<T> union(@NonNull MStream<T> other) {
      return getContext().stream(Streams.reusableStream(() -> Stream.concat(javaStream(), other.javaStream())));
   }

   @Override
   public <U> MPairStream<T, U> zip(@NonNull MStream<U> other) {
      return new LocalReusableMStream<>(() -> Streams.asStream(Iterators.zip(javaStream().iterator(),
                                                                             other.javaStream().iterator())))
            .mapToPair(e -> e);
   }

   @Override
   public MPairStream<T, Long> zipWithIndex() {
      final AtomicLong index = new AtomicLong();
      return mapToPair(e -> $(e, index.getAndIncrement()));
   }

}//END OF AbstractLocalMStream
