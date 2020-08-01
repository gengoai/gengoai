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

import com.gengoai.conversion.Cast;
import com.gengoai.function.*;
import com.gengoai.stream.*;
import com.gengoai.tuple.Tuples;
import lombok.NonNull;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;

public abstract class AbstractLocalMPairStream<K, V> implements MPairStream<K, V>, Serializable {
   private static final long serialVersionUID = 1L;

   private static <T, U> MPairStream<T, U> create(MStream<Map.Entry<T, U>> stream) {
      return new LocalDefaultMPairStream<>(stream);
   }

   protected abstract MStream<Map.Entry<K, V>> asMStream();

   @Override
   public Stream<Map.Entry<K, V>> javaStream() {
      return asMStream().javaStream();
   }

   @Override
   public MPairStream<K, V> cache() {
      return create(asMStream().cache());
   }

   @Override
   public List<Map.Entry<K, V>> collectAsList() {
      return asMStream().collect();
   }

   @Override
   public Map<K, V> collectAsMap() {
      return asMStream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
   }

   @Override
   public long count() {
      return asMStream().count();
   }

   @Override
   public MPairStream<K, V> filter(@NonNull SerializableBiPredicate<? super K, ? super V> predicate) {
      return create(asMStream().filter(e -> predicate.test(e.getKey(), e.getValue())));
   }

   @Override
   public MPairStream<K, V> filterByKey(@NonNull SerializablePredicate<K> predicate) {
      return create(asMStream().filter(e -> predicate.test(e.getKey())));
   }

   @Override
   public MPairStream<K, V> filterByValue(@NonNull SerializablePredicate<V> predicate) {
      return create(asMStream().filter(e -> predicate.test(e.getValue())));
   }

   @Override
   public <R, V1> MPairStream<R, V1> flatMapToPair(SerializableBiFunction<? super K, ? super V, Stream<Map.Entry<? extends R, ? extends V1>>> function) {
      return create(asMStream().flatMap(e -> Cast.as(function.apply(e.getKey(), e.getValue()))));
   }

   @Override
   public void forEach(@NonNull SerializableBiConsumer<? super K, ? super V> consumer) {
      javaStream().forEach(e -> consumer.accept(e.getKey(), e.getValue()));
   }

   @Override
   public void forEachLocal(@NonNull SerializableBiConsumer<? super K, ? super V> consumer) {
      javaStream().forEach(e -> consumer.accept(e.getKey(), e.getValue()));
   }

   @Override
   public StreamingContext getContext() {
      return LocalStreamingContext.INSTANCE;
   }

   @Override
   public MPairStream<K, Iterable<V>> groupByKey() {
      return asMStream().groupBy(Map.Entry::getKey)
                        .mapToPair((k, v) -> Tuples.$(k, Cast.as(Streams.asStream(v)
                                                                        .map(Map.Entry::getValue)
                                                                        .collect(Collectors.toList()))));
   }

   @Override
   public MPairStream<K, V> persist(@NonNull StorageLevel storageLevel) {
      return create(asMStream().persist(storageLevel));
   }

   @Override
   public boolean isEmpty() {
      return asMStream().isEmpty();
   }


   @Override
   public <V1> MPairStream<K, Map.Entry<V, V1>> join(@NonNull MPairStream<? extends K, ? extends V1> other) {
      Map<K, Iterable<V1>> rhs = Cast.as(other.groupByKey().collectAsMap());
      return flatMapToPair((k, v) -> {
         if(rhs.containsKey(k)) {
            return Streams.asStream(rhs.get(k)).map(rv -> Tuples.$(k, Tuples.$(v, rv)));
         }
         return Stream.empty();
      });
   }

   @Override
   public MStream<K> keys() {
      return asMStream().map(Map.Entry::getKey);
   }

   @Override
   public <V1> MPairStream<K, Map.Entry<V, V1>> leftOuterJoin(@NonNull MPairStream<? extends K, ? extends V1> other) {
      Map<K, Iterable<V1>> rhs = Cast.as(other.groupByKey().collectAsMap());
      return flatMapToPair((k, v) -> {
         if(rhs.containsKey(k)) {
            return Streams.asStream(rhs.get(k)).map(rv -> Tuples.$(k, Tuples.$(v, rv)));
         }
         else {
            return Stream.of(Tuples.$(k, Tuples.$(v, null)));
         }
      });
   }

   @Override
   public <R> MStream<R> map(@NonNull SerializableBiFunction<? super K, ? super V, ? extends R> function) {
      return asMStream().map(e -> Cast.as(function.apply(e.getKey(), e.getValue())));
   }

   @Override
   public MDoubleStream mapToDouble(@NonNull SerializableToDoubleBiFunction<? super K, ? super V> function) {
      return asMStream().mapToDouble(e -> function.applyAsDouble(e.getKey(), e.getValue()));
   }

   @Override
   public <R, V1> MPairStream<R, V1> mapToPair(@NonNull SerializableBiFunction<? super K, ? super V, ? extends Map.Entry<? extends R, ? extends V1>> function) {
      return create(asMStream().map(e -> Cast.as(function.apply(e.getKey(), e.getValue()))));
   }

   @Override
   public Optional<Map.Entry<K, V>> max(@NonNull SerializableComparator<Map.Entry<K, V>> comparator) {
      return asMStream().max(comparator);
   }

   @Override
   public Optional<Map.Entry<K, V>> min(SerializableComparator<Map.Entry<K, V>> comparator) {
      return asMStream().min(comparator);
   }

   @Override
   public MPairStream<K, V> onClose(@NonNull SerializableRunnable closeHandler) {
      return create(asMStream().onClose(closeHandler));
   }

   @Override
   public MPairStream<K, V> parallel() {
      return create(asMStream().parallel());
   }

   @Override
   public MPairStream<K, V> reduceByKey(@NonNull SerializableBinaryOperator<V> operator) {
      return groupByKey().mapToPair((t, u) -> $(t, Streams.asStream(u).reduce(operator).orElse(null)));
   }

   @Override
   public MPairStream<K, V> repartition(int numPartitions) {
      return this;
   }

   @Override
   public <V1> MPairStream<K, Map.Entry<V, V1>> rightOuterJoin(@NonNull MPairStream<? extends K, ? extends V1> other) {
      final Map<K, Iterable<V>> lhs = groupByKey().collectAsMap();
      return other.flatMapToPair((k, v) -> {
         if(lhs.containsKey(k)) {
            return Streams.asStream(lhs.get(k)).map(lv -> Tuples.$(k, Tuples.$(lv, v)));
         }
         else {
            return Stream.of($(k, $(null, v)));
         }
      });
   }

   @Override
   public MPairStream<K, V> sample(boolean withReplacement, long number) {
      return create(asMStream().sample(withReplacement, (int) number));
   }

   @Override
   public MPairStream<K, V> shuffle(@NonNull Random random) {
      return create(asMStream().shuffle(random));
   }

   @Override
   public MPairStream<K, V> sortByKey(@NonNull SerializableComparator<K> comparator) {
      return create(new LocalReusableMStream<>(
            javaStream().sorted((e1, e2) -> comparator.compare(e1.getKey(), e2.getKey()))
      ));
   }

   @Override
   public MPairStream<K, V> union(@NonNull MPairStream<? extends K, ? extends V> other) {
      if(other.isDistributed()) {
         return Cast.as(other.union(Cast.as(this)));
      }
      final AbstractLocalMPairStream<K, V> almOther = Cast.as(other);
      return create(asMStream().union(almOther.asMStream()));
   }

   @Override
   public MStream<V> values() {
      return asMStream().map(Map.Entry::getValue);
   }

   @Override
   public void close() throws Exception {
      asMStream().close();
   }

}//END OF AbstractLocalMPairStream
