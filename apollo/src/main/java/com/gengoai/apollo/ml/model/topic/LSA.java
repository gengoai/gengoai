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

package com.gengoai.apollo.ml.model.topic;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.stream.spark.SparkStream;
import lombok.NonNull;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.gengoai.apollo.math.linalg.SparkLinearAlgebra.sparkSVD;
import static com.gengoai.apollo.math.linalg.SparkLinearAlgebra.toMatrix;
import static com.gengoai.apollo.ml.observation.VariableCollection.mergeVariableSpace;
import static com.gengoai.function.Functional.with;

/**
 * <p>Distributed version of <a href="https://en.wikipedia.org/wiki/Latent_semantic_analysis">Latent Semantic
 * Analysis</a> using Apache Spark. Documents are represented by examples and words are by features in the Example.</p>
 *
 * @author David B. Bracewell
 */
public class LSA extends BaseVectorTopicModel {
   private static final long serialVersionUID = 1L;
   private final Parameters parameters;
   private List<NDArray> topicVectors = new ArrayList<>();

   /**
    * Instantiates a new LSA model with default parameters.
    */
   public LSA() {
      this(new Parameters());
   }

   /**
    * Instantiates a new LSA model with the given parameters.
    *
    * @param parameters the parameters
    */
   public LSA(@NonNull Parameters parameters) {
      this.parameters = parameters;
   }

   /**
    * Instantiates a new LSA model with the given model updater.
    *
    * @param updater the updater
    */
   public LSA(@NonNull Consumer<Parameters> updater) {
      this.parameters = with(new Parameters(), updater);
   }

   private Stream<NDArray> encode(Datum d) {
      if(parameters.combineInputs.value()) {
         return mergeVariableSpace(d.stream(getInputs()))
               .getVariableSpace()
               .map(o -> toCountVector(o, parameters.namingPattern.value()));
      }
      return d.stream(getInputs())
              .map(o -> toCountVector(o, parameters.namingPattern.value()));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      encoderFit(dataset, getInputs(), parameters.namingPattern.value());
      SparkStream<Vector> stream = new SparkStream<Vector>(dataset.parallelStream().toDistributedStream()
                                                                  .flatMap(this::encode)
                                                                  .map(o -> new DenseVector(o.toDoubleArray())))
            .cache();
      RowMatrix mat = new RowMatrix(stream.getRDD().rdd());
      //since we have document x word, V is the word x component matrix
      // U = document x component, E = singular components, V = word x component
      // Transpose V to get component (topics) x words
      NDArray topicMatrix = toMatrix(sparkSVD(mat, parameters.K.value()).V().transpose());
      for(int i = 0; i < parameters.K.value(); i++) {
         Counter<String> featureDist = Counters.newCounter();
         NDArray dist = NDArrayFactory.ND.columnVector(topicMatrix.getRow(i).toDoubleArray());
         dist.forEachSparse((index, v) -> featureDist.set(encoder.decode(index), v));
         topics.add(new Topic(i, featureDist));
         topicVectors.add(dist);
      }
   }

   @Override
   public Parameters getFitParameters() {
      return parameters;
   }

   @Override
   public NDArray getTopicDistribution(String feature) {
      int i = encoder.encode(feature);
      if(i == -1) {
         return NDArrayFactory.ND.rowVector(new double[topics.size()]);
      }
      double[] dist = new double[topics.size()];
      for(int i1 = 0; i1 < topics.size(); i1++) {
         dist[i1] = topicVectors.get(i1).get(i);
      }
      return NDArrayFactory.ND.rowVector(dist);
   }

   @Override
   protected NDArray inference(NDArray vector) {
      double[] scores = new double[topics.size()];
      for(int i = 0; i < topics.size(); i++) {
         double score = vector.dot(topicVectors.get(i));
         scores[i] = score;
      }
      return NDArrayFactory.ND.rowVector(scores);
   }

   /**
    * LSA Fit Parameters.
    */
   public static class Parameters extends TopicModelFitParameters {
      /**
       * The number of topics to discover (default 100).
       */
      public final Parameter<Integer> K = parameter(Params.Clustering.K, 100);
   }

}//END OF LSA
