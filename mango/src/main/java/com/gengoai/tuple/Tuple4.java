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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.conversion.Cast;
import lombok.Getter;

/**
 * The type Tuple 4.
 *
 * @param <A> the type parameter
 * @param <B> the type parameter
 * @param <C> the type parameter
 * @param <D> the type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = Tuple4.class)
@Getter
public class Tuple4<A, B, C, D> extends Tuple {
   private static final long serialVersionUID = 1L;
   /**
    * the first value
    */
   public final A v1;
   /**
    * The second value
    */
   public final B v2;
   /**
    * The third value
    */
   public final C v3;
   /**
    * The fourth value
    */
   public final D v4;

   private Tuple4(A a, B b, C c, D d) {
      this.v1 = a;
      this.v2 = b;
      this.v3 = c;
      this.v4 = d;
   }

   @JsonCreator
   protected Tuple4(@JsonProperty Object[] array) {
      this.v1 = Cast.as(array[0]);
      this.v2 = Cast.as(array[1]);
      this.v3 = Cast.as(array[2]);
      this.v4 = Cast.as(array[3]);
   }

   /**
    * Static constructor
    *
    * @param <A> the type parameter
    * @param <B> the type parameter
    * @param <C> the type parameter
    * @param <D> the type parameter
    * @param a   the a
    * @param b   the b
    * @param c   the c
    * @param d   the d
    * @return the tuple 4
    */
   public static <A, B, C, D> Tuple4<A, B, C, D> of(A a, B b, C c, D d) {
      return new Tuple4<>(a, b, c, d);
   }

   @Override
   @JsonValue
   public Object[] array() {
      return new Object[]{v1, v2, v3, v4};
   }

   @Override
   public Tuple4<A, B, C, D> copy() {
      return new Tuple4<>(this.v1, this.v2, this.v3, this.v4);
   }

   @Override
   public int degree() {
      return 4;
   }

   @Override
   public <T> T get(int i) {
      switch (i) {
         case 0:
            return Cast.as(v1);
         case 1:
            return Cast.as(v2);
         case 2:
            return Cast.as(v3);
         case 3:
            return Cast.as(v4);
         default:
            throw new ArrayIndexOutOfBoundsException();
      }
   }

   @Override
   public Tuple3<B, C, D> shiftLeft() {
      return Tuple3.of(v2, v3, v4);
   }

   @Override
   public Tuple3<A, B, C> shiftRight() {
      return Tuple3.of(v1, v2, v3);
   }

   @Override
   public String toString() {
      return "(" + v1 + ", " + v2 + ", " + v3 + ", " + v4 + ")";
   }

}//END OF Tuple2
