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

package com.gengoai.apollo.data;

import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;

import java.util.stream.Stream;

/**
 * Defines how the dataset is stored/processed.
 *
 * @author David B. Bracewell
 */
public enum DataSetType {
   /**
    * Distributed using Apache Spark
    */
   Distributed {
      @Override
      public StreamingContext getStreamingContext() {
         return StreamingContext.distributed();
      }

      @Override
      public DataSet create(@NonNull MStream<Datum> stream) {
         return new StreamingDataSet(stream.toDistributedStream()).probe();
      }
   },
   /**
    * All data is stored in-memory on local machine.
    */
   InMemory {
      @Override
      public DataSet create(@NonNull MStream<Datum> stream) {
         return new InMemoryDataSet(stream.collect());
      }

   },
   /**
    * Local Streaming-based dataset
    */
   LocalStreaming {
      @Override
      public DataSet create(@NonNull MStream<Datum> stream) {
         if (stream.isDistributed()) {
            return new StreamingDataSet(StreamingContext.local().stream(stream.collect())).probe();
         }
         return new StreamingDataSet(stream);
      }
   },
   /**
    * On-Disk DataSet
    */
   OnDisk {
      @Override
      public DataSet create(@NonNull MStream<Datum> stream) {
         return new SQLiteDataSet(stream.javaStream()).probe();
      }

   };

   /**
    * <p>Creates a new DataSet from the given MStream of data/</p>
    *
    * @param stream the stream of data
    * @return the DataSet
    */
   public abstract DataSet create(@NonNull MStream<Datum> stream);

   /**
    * <p>Creates a new DataSet from the given Stream of data/</p>
    *
    * @param stream the stream of data
    * @return the DataSet
    */
   public DataSet create(@NonNull Stream<Datum> stream) {
      return create(getStreamingContext().stream(stream));
   }

   /**
    * Gets the streaming context.
    *
    * @return the streaming context
    */
   public StreamingContext getStreamingContext() {
      return StreamingContext.local();
   }

}//END OF DataSetType
