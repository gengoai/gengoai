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

package com.gengoai.apollo.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.function.SerializableFunction;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.util.*;
import java.util.stream.IntStream;

/**
 * <p>
 * A {@link DataSet} implementation where data is stored in memory. All operations modify the underlying collection of
 * datum.
 * </p>
 */
public class InMemoryDataSet extends DataSet {
   private static final long serialVersionUID = 1L;
   @JsonProperty("data")
   private final List<Datum> data = new ArrayList<>();

   /**
    * Instantiates a new In memory data set.
    *
    * @param data the data
    */
   public InMemoryDataSet(@NonNull Collection<? extends Datum> data) {
      this.data.addAll(data);
   }

   @JsonCreator
   public InMemoryDataSet(@JsonProperty("data") @NonNull Collection<? extends Datum> data,
                          @JsonProperty("metadata") @NonNull Map<String, ObservationMetadata> metadataMap,
                          @JsonProperty("ndarrayFactory") @NonNull NDArrayFactory factory
                         ) {
      this.data.addAll(data);
      this.metadata.putAll(metadataMap);
      this.ndArrayFactory = factory;
   }

   @Override
   public Iterator<DataSet> batchIterator(int batchSize) {
      Validation.checkArgument(batchSize > 0, "Batch size must be > 0");
      return new Iterator<>() {
         int index = 0;

         @Override
         public boolean hasNext() {
            return index < data.size();
         }

         @Override
         public DataSet next() {
            DataSet next = new InMemoryDataSet(data.subList(index, Math.min(index + batchSize, data.size())),
                                               getMetadata(),
                                               getNDArrayFactory());
            index = index + batchSize;
            return next;
         }
      };
   }

   @Override
   public DataSet cache() {
      return this;
   }

   @Override
   public DataSetType getType() {
      return DataSetType.InMemory;
   }

   @Override
   public Iterator<Datum> iterator() {
      return data.iterator();
   }

   @Override
   public DataSet map(SerializableFunction<? super Datum, ? extends Datum> function) {
      IntStream.range(0, data.size())
               .forEach(i -> data.set(i, function.apply(data.get(i))));
      return this;
   }

   @Override
   public MStream<Datum> parallelStream() {
      return StreamingContext.local().stream(this).parallel();
   }

   @Override
   public DataSet shuffle() {
      Collections.shuffle(data);
      return this;
   }

   @Override
   public DataSet shuffle(Random random) {
      Collections.shuffle(data, random);
      return this;
   }

   @Override
   public long size() {
      return data.size();
   }

   @Override
   public MStream<Datum> stream() {
      return StreamingContext.local().stream(this);
   }

}//END OF InMemoryDataset
