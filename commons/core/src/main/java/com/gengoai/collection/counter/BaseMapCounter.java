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

import com.gengoai.math.Math2;
import com.gengoai.math.Operator;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract implementation of a counter backed by a {@link Map}.
 *
 * @param <T> the component type of the counter
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = false)
public abstract class BaseMapCounter<T> implements Counter<T>, Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<T, Double> map;

   /**
    * Instantiates a new map counter.
    *
    * @param backingMap the backing map
    */
   protected BaseMapCounter(Map<T, Double> backingMap) {
      this.map = backingMap;
   }

   @Override
   public Counter<T> adjustValues(DoubleUnaryOperator function) {
      Counter<T> newCounter = newInstance();
      map.forEach((key, value) -> newCounter.increment(key, function.applyAsDouble(value)));
      return newCounter;
   }

   @Override
   public Counter<T> adjustValuesSelf(DoubleUnaryOperator function) {
      map.replaceAll((key, value) -> function.applyAsDouble(value));
      return this;
   }

   @Override
   public Map<T, Double> asMap() {
      return map;
   }

   @Override
   public Counter<T> bottomN(int n) {
      Counter<T> cprime = newInstance();
      itemsByCount(true).stream()
                        .limit(n)
                        .forEach(t -> cprime.set(t, get(t)));
      return cprime;
   }

   @Override
   public void clear() {
      map.clear();
   }

   @Override
   public boolean contains(T item) {
      return map.containsKey(item);
   }

   @Override
   public Counter<T> copy() {
      return this.<T>newInstance().merge(this);
   }

   @Override
   public Counter<T> divideBySum() {
      if(map.isEmpty()) {
         return this;
      }
      final double tmpSum = sum();
      map.replaceAll((key, value) -> value / tmpSum);
      return this;
   }

   @Override
   public Set<Map.Entry<T, Double>> entries() {
      return map.entrySet();
   }

   @Override
   public Counter<T> filterByKey(Predicate<? super T> predicate) {
      Counter<T> counter = newInstance();
      map.entrySet()
         .stream()
         .filter(e -> predicate.test(e.getKey()))
         .forEach(e -> counter.set(e.getKey(), e.getValue()));
      return counter;
   }

   @Override
   public Counter<T> filterByValue(DoublePredicate doublePredicate) {
      Counter<T> counter = newInstance();
      map.entrySet()
         .stream()
         .filter(e -> doublePredicate.test(e.getValue()))
         .forEach(e -> counter.set(e.getKey(), e.getValue()));
      return counter;
   }

   @Override
   public double get(T item) {
      return map.getOrDefault(item, 0d);
   }

   @Override
   public Counter<T> increment(T item, double amount) {
      if(amount == 0) {
         return this;
      }
      map.merge(item, amount, Operator::add);
      if(map.get(item) == 0) {
         map.remove(item);
      }
      return this;
   }

   @Override
   public boolean isEmpty() {
      return map.isEmpty();
   }

   @Override
   public Set<T> items() {
      return map.keySet();
   }

   @Override
   public List<T> itemsByCount(boolean ascending) {
      return map.entrySet()
                .stream()
                .sorted((e1, e2) -> (ascending
                                     ? 1
                                     : -1) * Double.compare(e1.getValue(), e2.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
   }

   @Override
   public <R> Counter<R> mapKeys(Function<? super T, ? extends R> function) {
      Counter<R> result = newInstance();
      map.forEach((key, value) -> result.increment(function.apply(key), value));
      return result;
   }

   @Override
   public Counter<T> merge(Counter<? extends T> other) {
      if(other != null) {
         other.forEach(this::increment);
      }
      return this;
   }

   @Override
   public Counter<T> merge(Map<? extends T, ? extends Number> other) {
      if(other != null) {
         for(Map.Entry<? extends T, ? extends Number> entry : other.entrySet()) {
            increment(entry.getKey(), entry.getValue().doubleValue());
         }
      }
      return this;
   }

   /**
    * New instance counter.
    *
    * @param <R> the type parameter
    * @return the counter
    */
   protected abstract <R> Counter<R> newInstance();

   @Override
   public double remove(T item) {
      if(item == null) {
         return 0d;
      }
      double value = map.getOrDefault(item, 0d);
      map.remove(item);
      return value;
   }

   @Override
   public Counter<T> removeAll(Iterable<T> items) {
      if(items != null) {
         items.forEach(this::remove);
      }
      return this;
   }

   @Override
   public Counter<T> set(T item, double count) {
      if(count == 0) {
         remove(item);
         return this;
      }
      map.put(item, count);
      return this;
   }

   @Override
   public int size() {
      return map.size();
   }

   @Override
   public double sum() {
      return Math2.sum(values());
   }

   @Override
   public String toString() {
      return map.toString();
   }

   @Override
   public Counter<T> topN(int n) {
      Counter<T> cprime = newInstance();
      itemsByCount(false).stream()
                         .limit(n)
                         .forEach(t -> cprime.set(t, get(t)));
      return cprime;
   }

   @Override
   public Collection<Double> values() {
      return map.values();
   }

}//END OF BaseMapCounter
