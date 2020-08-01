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

import java.io.ObjectStreamException;
import java.util.function.Function;

/**
 * A tuple of order 0, i.e. empty
 *
 * @author David B. Bracewell
 */
@JsonDeserialize(as = Tuple0.class)
public final class Tuple0 extends Tuple {
   private static final long serialVersionUID = 1L;
   /**
    * The constant INSTANCE.
    */
   public static Tuple0 INSTANCE = new Tuple0();

   @JsonCreator
   private static Tuple0 getInstance(@JsonProperty Object o) {
      return INSTANCE;
   }

   private Tuple0() {
   }

   @Override
   @JsonValue
   public Object[] array() {
      return new Object[0];
   }

   @Override
   public Tuple0 copy() {
      return new Tuple0();
   }

   @Override
   public int degree() {
      return 0;
   }

   @Override
   public int hashCode() {
      return 1;
   }

   @Override
   public Tuple mapValues(Function<Object, ?> function) {
      return this;
   }

   protected Object readResolve() throws ObjectStreamException {
      return INSTANCE;
   }

   @Override
   public String toString() {
      return "()";
   }

}//END OF Tuple0
