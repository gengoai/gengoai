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

package com.gengoai.apollo.ml.model.embedding;

import com.gengoai.apollo.math.linalg.DenseMatrix;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.Params;
import lombok.NonNull;
import org.apache.spark.mllib.feature.Word2VecModel;
import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import java.util.Date;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gengoai.function.Functional.with;
import static scala.collection.JavaConversions.mapAsJavaMap;

/**
 * <p>Wrapper around Spark's Word2Vec embedding model.</p>
 *
 * @author David B. Bracewell
 */
public class Word2Vec extends TrainableWordEmbedding<Word2Vec.Parameters, Word2Vec> {
   private static final long serialVersionUID = 1L;

   public Word2Vec() {
      super(new Parameters());
   }

   public Word2Vec(@NonNull Word2Vec.Parameters parameters) {
      super(parameters);
   }

   public Word2Vec(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      org.apache.spark.mllib.feature.Word2Vec w2v = new org.apache.spark.mllib.feature.Word2Vec();
      w2v.setMinCount(1);
      w2v.setVectorSize(parameters.dimension.value());
      w2v.setLearningRate(parameters.learningRate.value());
      w2v.setNumIterations(parameters.maxIterations.value());
      w2v.setWindowSize(parameters.windowSize.value());
      w2v.setSeed(new Date().getTime());

      Word2VecModel model = w2v.fit(dataset.stream()
                                           .toDistributedStream()
                                           .flatMap(d -> d.stream(parameters.inputs.value()))
                                           .map(o -> o.getVariableSpace()
                                                      .map(this::getVariableName)
                                                      .collect(Collectors.toList()))
                                           .getRDD());

      vectorStore = new InMemoryVectorStore(parameters.dimension.value(),
                                            parameters.unknownWord.value(),
                                            parameters.specialWords.value());
      mapAsJavaMap(model.getVectors()).forEach((k, v) -> {
         int index = vectorStore.addOrGetIndex(k);
         vectorStore.updateVector(index,
                                  new DenseMatrix(MatrixFunctions.floatToDouble(new FloatMatrix(1, v.length, v)))
                                        .setLabel(k));
      });
   }

   /**
    * FitParameters for Word2Vec
    */
   public static class Parameters extends WordEmbeddingFitParameters<Parameters> {
      /**
       * The Learning rate.
       */
      public final Parameter<Double> learningRate = parameter(Params.Optimizable.learningRate, 0.025);
      /**
       * The Max iterations.
       */
      public final Parameter<Integer> maxIterations = parameter(Params.Optimizable.maxIterations, 1);
   }

}//END OF Word2Vec
