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

package com.gengoai.tuple;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.conversion.Cast;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * The type Tuple 2.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(as = Tuple2.class)
public class Tuple2<K, V> extends Tuple implements Map.Entry<K, V> {

   private static final long serialVersionUID = 1L;
   /**
    * The first value
    */
   public final K v1;
   /**
    * The second value
    */
   public final V v2;

   /**
    * Of tuple 2.
    *
    * @param <K>   the type parameter
    * @param <V>   the type parameter
    * @param key   the key
    * @param value the value
    * @return the tuple 2
    */
   public static <K, V> Tuple2<K, V> of(K key, V value) {
      return new Tuple2<>(key, value);
   }

   @JsonCreator
   private Tuple2(@JsonProperty Object[] array) {
      this.v1 = Cast.as(array[0]);
      this.v2 = Cast.as(array[1]);
   }

   @Override
   public <T> Tuple3<T, K, V> appendLeft(T object) {
      return Tuple3.of(object, v1, v2);
   }

   @Override
   public <T> Tuple3<K, V, T> appendRight(T object) {
      return Tuple3.of(v1, v2, object);
   }

   @Override
   @JsonValue
   public Object[] array() {
      return new Object[]{v1, v2};
   }

   @Override
   public Tuple2<K, V> copy() {
      return new Tuple2<>(this.v1, this.v2);
   }

   @Override
   public int degree() {
      return 2;
   }

   @Override
   public <T> T get(int i) {
      switch(i) {
         case 0:
            return Cast.as(v1);
         case 1:
            return Cast.as(v2);
         default:
            throw new ArrayIndexOutOfBoundsException(i);
      }
   }

   @Override
   @JsonIgnore
   public K getKey() {
      return v1;
   }

   public K getV1() {
      return this.v1;
   }

   public V getV2() {
      return this.v2;
   }

   @Override
   @JsonIgnore
   public V getValue() {
      return v2;
   }

   @Override
   public V setValue(V value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Tuple1<V> shiftLeft() {
      return Tuple1.of(v2);
   }

   @Override
   public Tuple1<K> shiftRight() {
      return Tuple1.of(v1);
   }

   @Override
   public String toString() {
      return "(" + v1 + ", " + v2 + ")";
   }

}//END OF Tuple2
