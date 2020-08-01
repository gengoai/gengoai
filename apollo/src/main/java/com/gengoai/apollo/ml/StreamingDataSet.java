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

import com.gengoai.Copyable;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.function.SerializableFunction;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StorageLevel;
import lombok.NonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * <p>A {@link DataSet} backed by an MStream. </p>
 *
 * @author David B. Bracewell
 */
public class StreamingDataSet extends DataSet {
   private MStream<Datum> stream;

   /**
    * Instantiates a new Streaming data set.
    *
    * @param stream the stream
    */
   public StreamingDataSet(@NonNull MStream<Datum> stream) {
      this.stream = stream;
   }

   public StreamingDataSet(@NonNull MStream<Datum> stream,
                           @NonNull Map<String, ObservationMetadata> metadataMap,
                           @NonNull NDArrayFactory factory) {
      this.stream = stream;
      this.metadata.putAll(metadataMap);
      this.ndArrayFactory = factory;
   }

   @Override
   public Iterator<DataSet> batchIterator(int batchSize) {
      return stream.partition(batchSize)
                   .map(batch -> (DataSet) datasetOf(getType().getStreamingContext().stream(batch).map(Datum::copy)))
                   .iterator();
   }

   @Override
   public DataSet cache() {
      InMemoryDataSet out = new InMemoryDataSet(stream.collect());
      out.metadata.putAll(Copyable.deepCopy(metadata));
      return out;
   }

   protected StreamingDataSet datasetOf(MStream<Datum> examples) {
      return new StreamingDataSet(examples, getMetadata(), getNDArrayFactory());
   }

   @Override
   public DataSetType getType() {
      if(stream.getContext().isDistributed()) {
         return DataSetType.Distributed;
      }
      return DataSetType.LocalStreaming;
   }

   @Override
   public Iterator<Datum> iterator() {
      return stream.iterator();
   }

   @Override
   public DataSet map(@NonNull SerializableFunction<? super Datum, ? extends Datum> function) {
      StreamingDataSet out = new StreamingDataSet(stream.map(function));
      out.metadata.putAll(Copyable.deepCopy(metadata));
      return out;
   }

   @Override
   public MStream<Datum> parallelStream() {
      return stream.parallel();
   }

   @Override
   public DataSet persist() {
      if(stream.isDistributed()) {
         stream.persist(StorageLevel.OnDisk);
         return this;
      }
      return super.persist();
   }

   @Override
   public DataSet shuffle(Random random) {
      stream = stream.shuffle(random);
      return this;
   }

   @Override
   public long size() {
      return stream.count();
   }

   @Override
   public MStream<Datum> stream() {
      return stream;
   }

}//END OF StreamingDataSet
