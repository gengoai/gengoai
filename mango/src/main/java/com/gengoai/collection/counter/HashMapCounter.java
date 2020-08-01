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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a Counter using a HashMap
 *
 * @param <T> the component type of the counter
 * @author David B. Bracewell
 */
public class HashMapCounter<T> extends BaseMapCounter<T> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Hash map counter.
    */
   public HashMapCounter() {
      super(new HashMap<>());
   }

   @JsonCreator
   public HashMapCounter(@JsonProperty @NonNull Map<T, Double> map) {
      super(new HashMap<>(map));
   }

   @Override
   protected <R> Counter<R> newInstance() {
      return new HashMapCounter<>();
   }

}//END OF HashMapCounter
