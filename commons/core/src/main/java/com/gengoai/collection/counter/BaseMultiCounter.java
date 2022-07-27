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

package com.gengoai.collection.counter;

import com.gengoai.collection.Iterators;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.Streams;
import com.gengoai.tuple.Tuple2;
import com.gengoai.tuple.Tuple3;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.*;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.gengoai.tuple.Tuples.$;

/**
 * Implementation of a MultiCounter using a HashMaps.
 *
 * @param <K> the first key V
 * @param <V> the second V
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = false)
public abstract class BaseMultiCounter<K, V> implements MultiCounter<K, V>, Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<K, Counter<V>> map;

   /**
    * Instantiates a new Base multi counter.
    *
    * @param backingMap the backing map
    */
   protected BaseMultiCounter(Map<K, Counter<V>> backingMap) {
      this.map = backingMap;
   }

   @Override
   public MultiCounter<K, V> adjustValues(DoubleUnaryOperator function) {
      MultiCounter<K, V> tmp = newInstance();
      firstKeys().forEach(key -> tmp.set(key, get(key).adjustValues(function)));
      return tmp;
   }

   @Override
   public MultiCounter<K, V> adjustValuesSelf(DoubleUnaryOperator function) {
      firstKeys().forEach(key -> get(key).adjustValuesSelf(function));
      return this;
   }

   @Override
   public void clear() {
      map.clear();
   }

   @Override
   public boolean contains(K item) {
      return map.containsKey(item);
   }

   @Override
   public boolean contains(K item1, V item2) {
      return map.containsKey(item1) && map.get(item1).contains(item2);
   }

   /**
    * Creates a new counter.
    *
    * @return the counter
    */
   protected abstract Counter<V> createCounter();

   @Override
   public Set<Tuple3<K, V, Double>> entries() {
      return new AbstractSet<Tuple3<K, V, Double>>() {

         @Override
         public Iterator<Tuple3<K, V, Double>> iterator() {
            return new KeyKeyValueIterator();
         }

         @Override
         public int size() {
            return BaseMultiCounter.this.size();
         }
      };
   }

   @Override
   public MultiCounter<K, V> filterByFirstKey(Predicate<K> predicate) {
      MultiCounter<K, V> tmp = newInstance();
      Streams.asStream(new KeyKeyValueIterator())
             .filter(t -> predicate.test(t.getV1()))
             .forEach(t -> tmp.set(t.getV1(), t.getV2(), t.getV3()));
      return tmp;
   }

   @Override
   public MultiCounter<K, V> filterBySecondKey(Predicate<V> predicate) {
      MultiCounter<K, V> tmp = newInstance();
      Streams.asStream(new KeyKeyValueIterator())
             .filter(t -> predicate.test(t.getV2()))
             .forEach(t -> tmp.set(t.getV1(), t.getV2(), t.getV3()));
      return tmp;
   }

   @Override
   public MultiCounter<K, V> filterByValue(DoublePredicate predicate) {
      MultiCounter<K, V> tmp = newInstance();
      Streams.asStream(new KeyKeyValueIterator())
             .filter(t -> predicate.test(t.getV3()))
             .forEach(t -> tmp.set(t.getV1(), t.getV2(), t.getV3()));
      return tmp;
   }

   @Override
   public Set<K> firstKeys() {
      return map.keySet();
   }

   @Override
   public Counter<V> get(K firstKey) {
      return new ForwardingCounter(firstKey);
   }

   @Override
   public boolean isEmpty() {
      return map.isEmpty();
   }

   @Override
   public List<Map.Entry<K, V>> itemsByCount(boolean ascending) {
      return Streams.asStream(new KeyKeyValueIterator())
                    .sorted((c1, c2) -> (ascending
                                         ? 1
                                         : -1) * Double.compare(c1.getV3(), c2.getV3()))
                    .map(t -> Cast.<Map.Entry<K, V>>as(Tuple2.of(t.getV1(), t.getV2())))
                    .collect(Collectors.toList());
   }

   @Override
   public Set<Map.Entry<K, V>> keyPairs() {
      return new AbstractSet<Map.Entry<K, V>>() {

         @Override
         public boolean contains(Object o) {
            if(o instanceof Map.Entry) {
               Map.Entry<K, V> e = Cast.as(o);
               return BaseMultiCounter.this.contains(e.getKey(), e.getValue());
            }
            return false;
         }

         @Override
         public Iterator<Map.Entry<K, V>> iterator() {
            return Iterators.transform(new KeyKeyValueIterator(),
                                       t -> $(t.v1, t.v2));
         }

         @Override
         public int size() {
            return BaseMultiCounter.this.size();
         }
      };
   }

   @Override
   public MultiCounter<K, V> merge(MultiCounter<K, V> other) {
      if(other != null) {
         other.entries().forEach(e -> increment(e.v1, e.v2, e.v3));
      }
      return this;
   }

   /**
    * New instance.
    *
    * @return the multi counter
    */
   protected abstract MultiCounter<K, V> newInstance();

   @Override
   public Counter<V> remove(K item) {
      if(map.containsKey(item)) {
         return map.remove(item);
      }
      return createCounter();
   }

   @Override
   public double remove(K item1, V item2) {
      return get(item1).remove(item2);
   }

   @Override
   public MultiCounter<K, V> set(K item1, V item2, double amount) {
      get(item1).set(item2, amount);
      return this;
   }

   @Override
   public MultiCounter<K, V> set(K item, Counter<V> counter) {
      if(counter == null || counter.isEmpty()) {
         map.remove(item);
      } else {
         map.put(item, counter);
      }
      return this;
   }

   @Override
   public int size() {
      return map.values().parallelStream().mapToInt(Counter::size).sum();
   }

   @Override
   public String toString() {
      return map.toString();
   }

   @Override
   public Collection<Double> values() {
      return new AbstractCollection<Double>() {
         @Override
         public Iterator<Double> iterator() {
            return Iterators.transform(new KeyKeyValueIterator(), Tuple3::getV3);
         }

         @Override
         public int size() {
            return BaseMultiCounter.this.size();
         }
      };
   }

   private class KeyKeyValueIterator implements Iterator<Tuple3<K, V, Double>> {

      private Iterator<Map.Entry<K, Counter<V>>> entryIterator = map.entrySet().iterator();
      private Map.Entry<K, Counter<V>> entry = null;
      private Iterator<V> key2Iterator = null;

      private boolean advance() {
         while(key2Iterator == null || !key2Iterator.hasNext()) {
            if(entryIterator.hasNext()) {
               entry = entryIterator.next();
               key2Iterator = entry.getValue().items().iterator();
            } else {
               return false;
            }
         }
         return true;
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public Tuple3<K, V, Double> next() {
         if(!advance()) {
            throw new NoSuchElementException();
         }
         V key2 = key2Iterator.next();
         return Tuple3.of(entry.getKey(), key2, entry.getValue().get(key2));
      }

      @Override
      public void remove() {
         key2Iterator.remove();
         if(entry.getValue().isEmpty()) {
            entryIterator.remove();
         }
      }
   }

   class ForwardingCounter implements Counter<V>, Serializable {
      private static final long serialVersionUID = 1L;
      private final K key;

      ForwardingCounter(K key) {
         this.key = key;
      }

      @Override
      public Counter<V> adjustValues(DoubleUnaryOperator function) {
         if(map.containsKey(key)) {
            return map.get(key).adjustValues(function);
         }
         return createCounter();
      }

      @Override
      public Counter<V> adjustValuesSelf(DoubleUnaryOperator function) {
         createIfNeeded().adjustValuesSelf(function);
         removeIfEmpty();
         return this;
      }

      @Override
      public Map<V, Double> asMap() {
         return delegate() == null
                ? Collections.emptyMap()
                : delegate().asMap();
      }

      @Override
      public double average() {
         return delegate() == null
                ? 0d
                : delegate().average();
      }

      @Override
      public Counter<V> bottomN(int n) {
         if(map.containsKey(key)) {
            return map.get(key).bottomN(n);
         }
         return createCounter();
      }

      @Override
      public void clear() {
         map.remove(key);
      }

      @Override
      public boolean contains(V item) {
         return delegate() != null && delegate().contains(item);
      }

      @Override
      public Counter<V> copy() {
         Counter<V> toReturn = createIfNeeded().copy();
         removeIfEmpty();
         return toReturn;
      }

      protected Counter<V> createIfNeeded() {
         return map.computeIfAbsent(key, k -> createCounter());
      }

      @Override
      public Counter<V> decrement(V item) {
         return decrement(item, 1);
      }

      @Override
      public Counter<V> decrement(V item, double amount) {
         return increment(item, -amount);
      }

      @Override
      public Counter<V> decrementAll(Iterable<? extends V> iterable) {
         return decrementAll(iterable, 1);
      }

      @Override
      public Counter<V> decrementAll(Iterable<? extends V> iterable, double amount) {
         if(iterable != null) {
            iterable.forEach(i -> decrement(i, amount));
            removeIfEmpty();
         }
         return this;
      }

      protected Counter<V> delegate() {
         return map.get(key);
      }

      @Override
      public Counter<V> divideBySum() {
         createIfNeeded().divideBySum();
         removeIfEmpty();
         return this;
      }

      @Override
      public Set<Map.Entry<V, Double>> entries() {
         return delegate() == null
                ? Collections.emptySet()
                : delegate().entries();
      }

      @Override
      public Counter<V> filterByKey(Predicate<? super V> predicate) {
         Counter<V> toReturn = createIfNeeded().filterByKey(predicate);
         removeIfEmpty();
         return toReturn;
      }

      @Override
      public Counter<V> filterByValue(DoublePredicate doublePredicate) {
         Counter<V> toReturn = createIfNeeded().filterByValue(doublePredicate);
         removeIfEmpty();
         return toReturn;
      }

      @Override
      public double get(V item) {
         return delegate() == null
                ? 0d
                : delegate().get(item);
      }

      @Override
      public Counter<V> increment(V item) {
         return increment(item, 1);
      }

      @Override
      public Counter<V> increment(V item, double amount) {
         createIfNeeded().increment(item, amount);
         removeIfEmpty();
         return this;
      }

      @Override
      public Counter<V> incrementAll(Iterable<? extends V> iterable) {
         return incrementAll(iterable, 1);
      }

      @Override
      public Counter<V> incrementAll(Iterable<? extends V> iterable, double amount) {
         if(iterable != null) {
            iterable.forEach(i -> increment(i, amount));
            removeIfEmpty();
         }
         return this;
      }

      @Override
      public boolean isEmpty() {
         return delegate() == null || delegate().isEmpty();
      }

      @Override
      public Set<V> items() {
         return delegate() == null
                ? Collections.emptySet()
                : delegate().items();
      }

      @Override
      public List<V> itemsByCount(boolean ascending) {
         return delegate() == null
                ? Collections.emptyList()
                : delegate().itemsByCount(ascending);
      }

      @Override
      public double magnitude() {
         return delegate() == null
                ? 0d
                : delegate().magnitude();
      }

      @Override
      public <R> Counter<R> mapKeys(Function<? super V, ? extends R> function) {
         Counter<R> toReturn = createIfNeeded().mapKeys(function);
         removeIfEmpty();
         return toReturn;
      }

      @Override
      public V max() {
         return delegate() == null
                ? null
                : delegate().max();
      }

      @Override
      public double maximumCount() {
         return delegate() == null
                ? 0d
                : delegate().maximumCount();
      }

      @Override
      public Counter<V> merge(Counter<? extends V> other) {
         createIfNeeded().merge(other);
         removeIfEmpty();
         return this;
      }

      @Override
      public Counter<V> merge(Map<? extends V, ? extends Number> other) {
         createIfNeeded().merge(other);
         removeIfEmpty();
         return this;
      }

      @Override
      public V min() {
         return delegate() == null
                ? null
                : delegate().min();
      }

      @Override
      public double minimumCount() {
         return delegate() == null
                ? 0d
                : delegate().minimumCount();
      }

      @Override
      public double remove(V item) {
         double d = delegate() == null
                    ? 0d
                    : delegate().remove(item);
         removeIfEmpty();
         return d;
      }

      @Override
      public Counter<V> removeAll(Iterable<V> items) {
         Counter<V> toReturn = createIfNeeded().removeAll(items);
         removeIfEmpty();
         return toReturn;
      }

      private void removeIfEmpty() {
         if(delegate() == null || delegate().isEmpty()) {
            map.remove(key);
         }
      }

      @Override
      public V sample() {
         return delegate() == null
                ? null
                : delegate().sample();
      }

      @Override
      public Counter<V> set(V item, double count) {
         createIfNeeded().set(item, count);
         removeIfEmpty();
         return this;
      }

      @Override
      public int size() {
         return delegate() == null
                ? 0
                : delegate().size();
      }

      @Override
      public double standardDeviation() {
         return delegate() == null
                ? Double.NaN
                : delegate().standardDeviation();
      }

      @Override
      public double sum() {
         return delegate() == null
                ? 0
                : delegate().sum();
      }

      @Override
      public String toString() {
         return delegate() == null
                ? "{}"
                : delegate().toString();
      }

      @Override
      public Counter<V> topN(int n) {
         Counter<V> toReturn = createIfNeeded().topN(n);
         removeIfEmpty();
         return toReturn;
      }

      @Override
      public Collection<Double> values() {
         return delegate() == null
                ? Collections.emptyList()
                : delegate().values();
      }

   }//END OF ForwardingCounter

}//END OF HashMapMultiCounter
