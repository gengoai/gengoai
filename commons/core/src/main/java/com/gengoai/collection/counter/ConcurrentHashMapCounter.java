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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of a Counter using a ConcurrentHashMap
 *
 * @param <T> the component type of the counter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = ConcurrentHashMapCounter.class)
public class ConcurrentHashMapCounter<T> extends BaseMapCounter<T> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Concurrent hash map counter.
    */
   public ConcurrentHashMapCounter() {
      super(new ConcurrentHashMap<>());
   }

   @JsonCreator
   public ConcurrentHashMapCounter(@JsonProperty @NonNull Map<T, Double> map) {
      super(new ConcurrentHashMap<>(map));
   }

   @Override
   protected <R> Counter<R> newInstance() {
      return new ConcurrentHashMapCounter<>();
   }

}//END OF ConcurrentHashMapCounter
