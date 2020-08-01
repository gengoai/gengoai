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

import com.gengoai.ParameterDef;
import com.gengoai.Stopwatch;
import com.gengoai.apollo.math.linalg.DenseMatrix;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.collection.disk.DiskMap;
import com.gengoai.concurrent.AtomicDouble;
import com.gengoai.io.Resources;
import com.gengoai.tuple.IntPair;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.gengoai.LogUtils.logInfo;
import static com.gengoai.function.Functional.with;

/**
 * <p>Implementation of Glove as defined in:</p>
 * <pre>
 *  Jeffrey Pennington, Richard Socher, and Christopher D. Manning. 2014. GloVe: Global Vectors for Word Representation.
 * </pre>
 *
 * @author David B. Bracewell
 */
@Log
public class Glove extends TrainableWordEmbedding<Glove.Parameters, Glove> {
   private static final long serialVersionUID = 1L;
   public static final ParameterDef<Double> alpha = ParameterDef.doubleParam("alpha");
   public static final ParameterDef<Integer> xMax = ParameterDef.intParam("xMax");

   /**
    * Instantiates a new Glove with default Parameters.
    */
   public Glove() {
      super(new Parameters());
   }

   /**
    * Instantiates a new Glove model with the given Glove parameters.
    *
    * @param parameters the parameters
    */
   public Glove(@NonNull Glove.Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new Glove with the given Parameter updater.
    *
    * @param updater method to update the model parameters
    */
   public Glove(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      Stopwatch sw = Stopwatch.createStarted();
      //      dataset = dataset.cache();
      //      double size = dataset.size();
      final AtomicLong processed = new AtomicLong(0);
      //      Counter<IntPair> counts = Counters.newCounter();
      vectorStore = new InMemoryVectorStore(parameters.dimension.value(),
                                            parameters.unknownWord.value(),
                                            parameters.specialWords.value());

      //scan the input for the vocabulary
      AtomicDouble size = new AtomicDouble(0);
      DiskMap<IntPair, Double> counts = DiskMap.<IntPair, Double>builder()
            .file(Resources.temporaryFile())
            .namespace("counts")
            .build();
      dataset.stream()
             .forEach(datum -> {
                size.addAndGet(1);
                datum.stream(parameters.inputs.value()).forEach(source -> {
                   Sequence<?> input = source.asSequence();
                   List<Integer> ids = toIndices(input);
                   for(int i = 1; i < ids.size(); i++) {
                      int iW = ids.get(i);
                      for(int j = Math.max(0, i - parameters.windowSize.value()); j < i; j++) {
                         int jW = ids.get(j);
                         double incrementBy = 1.0 / (i - j);
                         double cnt = counts.getOrDefault(IntPair.of(iW, jW), 0d);
                         counts.put(IntPair.of(iW, jW), cnt + incrementBy);
                         counts.put(IntPair.of(jW, iW), cnt + incrementBy);
                      }
                   }
                });
             });

      //      for(Datum datum : dataset) {
      //         datum.stream(parameters.inputs.value()).forEach(source -> {
      //            Sequence<?> input = source.asSequence();
      //            List<Integer> ids = toIndices(input);
      //            for(int i = 1; i < ids.size(); i++) {
      //               int iW = ids.get(i);
      //               for(int j = Math.max(0, i - parameters.windowSize.value()); j < i; j++) {
      //                  int jW = ids.get(j);
      //                  double incrementBy = 1.0 / (i - j);
      //                  counts.increment(IntPair.of(iW, jW), incrementBy);
      //                  counts.increment(IntPair.of(jW, iW), incrementBy);
      //               }
      //            }
      //            double cnt = processed.incrementAndGet();
      //            if(cnt % 1000 == 0) {
      //               if(parameters.verbose.value()) {
      //                  logInfo(log, "processed {0}", (100 * cnt / size.get()));
      //               }
      //            }
      //         });
      //      }

      sw.stop();
      if(parameters.verbose.value()) {
         logInfo(log, "Cooccurrence Matrix computed in {0}", sw);
      }

      List<Cooccurrence> cooccurrences = new ArrayList<>();
      counts.forEach((e, v) -> {
         if(v >= 5) {
            cooccurrences.add(new Cooccurrence(e.v1, e.v2, v));
         }
      });
      counts.clear();

      DoubleMatrix[] W = new DoubleMatrix[vectorStore.size() * 2];
      DoubleMatrix[] gradSq = new DoubleMatrix[vectorStore.size() * 2];
      for(int i = 0; i < vectorStore.size() * 2; i++) {
         W[i] = DoubleMatrix.rand(parameters.dimension.value()).sub(0.5f).divi(parameters.dimension.value());
         gradSq[i] = DoubleMatrix.ones(parameters.dimension.value());
      }

      DoubleMatrix biases = DoubleMatrix.rand(vectorStore.size() * 2).sub(0.5f).divi(parameters.dimension.value());
      DoubleMatrix gradSqBiases = DoubleMatrix.ones(vectorStore.size() * 2);

      int vocabLength = vectorStore.size();

      for(int itr = 0; itr < parameters.maxIterations.value(); itr++) {
         double globalCost = 0d;
         Collections.shuffle(cooccurrences);

         for(Cooccurrence cooccurrence : cooccurrences) {
            int iWord = cooccurrence.word1;
            int iContext = cooccurrence.word2 + vocabLength;
            double count = cooccurrence.count;

            DoubleMatrix v_main = W[iWord];
            double b_main = biases.get(iWord);
            DoubleMatrix gradsq_W_main = gradSq[iWord];
            double gradsq_b_main = gradSqBiases.get(iWord);

            DoubleMatrix v_context = W[iContext];
            double b_context = biases.get(iContext);
            DoubleMatrix gradsq_W_context = gradSq[iContext];
            double gradsq_b_contenxt = gradSqBiases.get(iContext);

            double diff = v_main.dot(v_context) + b_main + b_context - Math.log(count);
            double fdiff = count > parameters.xMax.value()
                           ? diff
                           : Math.pow(count / parameters.xMax.value(), parameters.alpha.value()) * diff;

            globalCost += 0.5 * fdiff * diff;

            fdiff *= parameters.learningRate.value();
            //Gradients for word vector terms
            DoubleMatrix grad_main = v_context.mmul(fdiff);
            DoubleMatrix grad_context = v_main.mmul(fdiff);

            v_main.subi(grad_main.divi(MatrixFunctions.sqrt(gradsq_W_main)));
            v_context.subi(grad_context.divi(MatrixFunctions.sqrt(gradsq_W_context)));

            gradsq_W_main.addi(MatrixFunctions.pow(grad_context, 2));
            gradSq[iWord] = gradsq_W_main;

            gradsq_W_context.addi(MatrixFunctions.pow(grad_main, 2));
            gradSq[iContext] = gradsq_W_context;

            biases.put(iWord, b_main - fdiff / Math.sqrt(gradsq_b_main));
            biases.put(iContext, b_context - fdiff / Math.sqrt(gradsq_b_contenxt));
            fdiff *= fdiff;

            gradSqBiases.put(iWord, gradSqBiases.get(iWord) + fdiff);
            gradSqBiases.put(iContext, gradSqBiases.get(iContext) + fdiff);
         }

         if(parameters.verbose.value()) {
            logInfo(log, "Iteration: {0},  cost:{1}", (itr + 1), globalCost / cooccurrences.size());
         }

      }

      for(int i = 0; i < vocabLength; i++) {
         W[i].addi(W[i + vocabLength]);
         String k = vectorStore.decode(i);
         vectorStore.updateVector(i, new DenseMatrix(W[i]).T().setLabel(k));
      }

   }

   private List<Integer> toIndices(Sequence<? extends Observation> sequence) {
      List<Integer> out = new ArrayList<>();
      for(Observation example : sequence) {
         example.getVariableSpace()
                .forEach(v -> out.add(vectorStore.addOrGetIndex(parameters.nameSpace.value().getName(v))));
      }
      return out;
   }

   private static class Cooccurrence {
      public final double count;
      public final int word1;
      public final int word2;

      public Cooccurrence(int word1, int word2, double count) {
         this.word1 = word1;
         this.word2 = word2;
         this.count = count;
      }
   }//END OF Cooccurrence

   /**
    * Fit parameters for Glove models
    */
   public static class Parameters extends WordEmbeddingFitParameters<Parameters> {
      /**
       * Controls the the normalization of cooccurrence counts to probabilities (default 0.75).
       */
      public final Parameter<Double> alpha = parameter(Glove.alpha, 0.75);
      /**
       * The learning rate (default 0.05)
       */
      public final Parameter<Double> learningRate = parameter(Params.Optimizable.learningRate, 0.05);
      /**
       * The cutoff value for cooccurrence counts (default 100).
       */
      public final Parameter<Integer> xMax = parameter(Glove.xMax, 100);
      /**
       * The maximum number of iterations to train the model (default 25).
       */
      public final Parameter<Integer> maxIterations = parameter(Params.Optimizable.maxIterations, 25);

   }
}//END OF Glove
