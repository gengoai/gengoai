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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.apollo.data.transform.Transform;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.collection.Lists;
import com.gengoai.function.SerializableFunction;
import com.gengoai.io.SaveMode;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;

/**
 * <p>
 * A dataset is a collection of {@link Datum} and is used to represent the training, testing, and development data for
 * machine learning models. Each dataset keeps track of metadata for the {@link com.gengoai.apollo.data.observation.Observation}s
 * in its datum which define the dimension (number of possible values), type (Sequence, Variable, etc.), and any
 * associated {@link com.gengoai.apollo.encoder.Encoder}. It is the responsibility of individual {@link Transform} to
 * ensure that the metadata is kept updated.
 * </p>
 * <p>
 * Note: Many of the machine learning algorithms rely on the metadata to determine the dimensions of the input and
 * output variables. Thus, it is important that if you are defining a dataset where the sources are {@link
 * com.gengoai.apollo.math.linalg.NDArray} that you manually set the metadata on the Dataset. For example:
 * </p>
 * <pre>
 * {@code
 *      List<Datum> data = Arrays.asList(
 *         Datum.of($("input", <NDARRAY>),
 *                  $("output", <NDARRAY>))
 *                  ...
 *      );
 *      DataSet dataset = new InMemoryDataset(data);
 *      dataset.updateMetadata("input", m -> m.setDimension(100));
 *      dataset.updateMetadata("output", m -> m.setDimension(20));
 * }*
 * </pre>
 *
 * @author David B. Bracewell
 */
@JsonDeserialize(as = InMemoryDataSet.class)
@Accessors(fluent = true)
public abstract class DataSet implements Iterable<Datum>, Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The number of items needed to probe the DataSet for Metadata.
    */
   public static int PROBE_LIMIT = 500;
   /**
    * Metadata about the observations in the DataSet including the type of observation and dimension.
    */
   protected final Map<String, ObservationMetadata> metadata = new ConcurrentHashMap<>();
   /**
    * The factory used for constructing NDArray
    */
   @NonNull
   protected NDArrayFactory ndArrayFactory = nd.DFLOAT32;

   /**
    * <p>Constructs an in-memory DataSet from the given array of examples</p>
    *
    * @param examples the examples
    * @return the DataSet
    */
   public static DataSet of(@NonNull Datum... examples) {
      return new InMemoryDataSet(Lists.arrayListOf(examples));
   }

   /**
    * <p>Constructs an in-memory DataSet from the given collection of examples</p>
    *
    * @param examples the examples
    * @return the DataSet
    */
   public static DataSet of(@NonNull Collection<Datum> examples) {
      return new InMemoryDataSet(Lists.asArrayList(examples));
   }

   /**
    * <p>Constructs an in-memory DataSet from the given stream of examples</p>
    *
    * @param examples the examples
    * @return the DataSet
    */
   public static DataSet of(@NonNull Stream<Datum> examples) {
      return new InMemoryDataSet(Lists.asArrayList(examples));
   }

   /**
    * <p>Constructs a streaming DataSet from the given stream of examples</p>
    *
    * @param examples the examples
    * @return the DataSet
    */
   public static DataSet of(@NonNull MStream<Datum> examples) {
      return new StreamingDataSet(examples);
   }


   /**
    * Generates an iterator of "batches" by partitioning the datum into groups of given batch size.
    *
    * @param batchSize the batch size
    * @return the iterator
    */
   public abstract Iterator<DataSet> batchIterator(int batchSize);

   /**
    * Caches the dataset into memory.
    *
    * @return the cached dataset
    */
   public abstract DataSet cache();

   /**
    * Collects all {@link Datum} in the data set into a list
    *
    * @return the list of datum
    */
   public List<Datum> collect() {
      return stream().collect();
   }

   /**
    * Gets the metadata for the observation sources on the datum in this dataset describing the dimension, type, and any
    * associated encoder.
    *
    * @param source the source
    * @return the metadata
    */
   public ObservationMetadata getMetadata(@NonNull String source) {
      return metadata.get(source);
   }

   /**
    * Gets the map of source name - {@link ObservationMetadata} for this dataset
    *
    * @return the map of source name - {@link ObservationMetadata}
    */
   public Map<String, ObservationMetadata> getMetadata() {
      return metadata;
   }

   /**
    * Gets the NDArrayFactory to use when creating NDArray
    *
    * @return the NDArrayFactory
    */
   public NDArrayFactory getNDArrayFactory() {
      return ndArrayFactory;
   }

   /**
    * Sets the {@link NDArrayFactory} to use when constructing NDArray.
    *
    * @param ndArrayFactory the NDArrayFactory
    * @return this DataSet
    */
   public DataSet setNDArrayFactory(@NonNull NDArrayFactory ndArrayFactory) {
      this.ndArrayFactory = ndArrayFactory;
      return this;
   }

   /**
    * Gets a streaming context compatible with this dataset
    *
    * @return the streaming context
    */
   @JsonIgnore
   public StreamingContext getStreamingContext() {
      return getType().getStreamingContext();
   }

   /**
    * Gets the type of this DataSet
    *
    * @return the DataSetType
    */
   @JsonIgnore
   public abstract DataSetType getType();

   /**
    * Maps the datum in the dataset constructing a new dataset. Depending on the underlying implementation this method
    * may be performed lazily.
    *
    * @param function the function to apply to  the datum in the dataset
    * @return this dataset
    */
   public abstract DataSet map(@NonNull SerializableFunction<? super Datum, ? extends Datum> function);

   /**
    * Generates a parallel MStream over the datum in this dataset
    *
    * @return parallel stream of data in the dataset
    */
   public abstract MStream<Datum> parallelStream();

   /**
    * Persists the DataSet to disk
    *
    * @param resource the resource location to persist the dataset to
    * @return the persisted version of the dataset
    */
   public DataSet persist(@NonNull Resource resource) throws IOException {
      return persist(resource, SaveMode.OVERWRITE);
   }

   /**
    * Persists the DataSet to disk
    *
    * @param resource the resource location to persist the dataset to
    * @return the persisted version of the dataset
    */
   public DataSet persist(@NonNull Resource resource, @NonNull SaveMode saveMode) throws IOException {
      saveMode.validate(resource);
      DataSet ds = new SQLiteDataSet(resource, stream().javaStream());
      ds.putAllMetadata(getMetadata());
      ds.setNDArrayFactory(getNDArrayFactory());
      return ds;
   }

   /**
    * Persists the DataSet to disk
    *
    * @return the persisted version of the dataset
    */
   public DataSet persist() throws IOException {
      DataSet ds = new SQLiteDataSet(stream().javaStream());
      ds.putAllMetadata(getMetadata());
      ds.setNDArrayFactory(getNDArrayFactory());
      return ds;
   }

   /**
    * Probes the data set to determine the types of its observations. This is only necessary if the metadata is needed
    * directly after constructing a dataset.
    *
    * @return this DataSet
    */
   public DataSet probe() {
      parallelStream().limit(PROBE_LIMIT)
                      .flatMap(d -> d.entrySet().stream())
                      .map(e -> {
                         if (e.getValue().isNDArray()) {
                            return $(e.getKey(), e.getValue().getClass(), e.getValue().asNDArray().length());
                         }
                         return $(e.getKey(), e.getValue().getClass(), 0L);
                      })
                      .distinct()
                      .collect(Collectors.toList())
                      .forEach(e -> updateMetadata(e.v1, m -> {
                         m.setType(e.v2);
                         m.setDimension(e.v3);
                      }));
      return this;
   }

   /**
    * Copies all metadata from the given map to this data set.
    *
    * @param metadata the metadata
    * @return this DataSet
    */
   public DataSet putAllMetadata(@NonNull Map<String, ObservationMetadata> metadata) {
      this.metadata.putAll(metadata);
      return this;
   }

   /**
    * Removes the metadata associated with a given observation source.
    *
    * @param source the observation source
    * @return this DataSet
    */
   public DataSet removeMetadata(@NonNull String source) {
      metadata.remove(source);
      return this;
   }

   /**
    * Shuffles the data in the dataset.
    *
    * @return This dataset with its data shuffled
    */
   public DataSet shuffle() {
      return shuffle(new Random(10));
   }

   /**
    * Shuffles the dataset creating a new one with the given random number generator.
    *
    * @param random the random number generator
    * @return the dataset
    */
   public abstract DataSet shuffle(Random random);

   /**
    * Returns the number of datum in the dataset
    *
    * @return The number of datum in the dataset
    */
   public abstract long size();

   /**
    * Generates an MStream over the datum in this dataset
    *
    * @return stream of data in the dataset
    */
   public abstract MStream<Datum> stream();

   /**
    * Takes the first n elements from the dataset
    *
    * @param n the number of items to take
    * @return the list of items
    */
   public List<Datum> take(int n) {
      return stream().take(n);
   }

   /**
    * Updates the metadata associated with a given observation source.
    *
    * @param source  the observation source
    * @param updater the update consumer
    * @return this DataSet
    */
   public DataSet updateMetadata(@NonNull String source, @NonNull Consumer<ObservationMetadata> updater) {
      metadata.compute(source, (s, md) -> {
         if (md == null) {
            md = new ObservationMetadata();
         }
         updater.accept(md);
         return md;
      });
      return this;
   }

}//END OF DataSet
