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

package com.gengoai.apollo.model.embedding;

import com.gengoai.Stopwatch;
import com.gengoai.SystemInfo;
import com.gengoai.apollo.math.linalg.NDArrayInitializer;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.math.measure.Similarity;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.encoder.HuffmanEncoder;
import com.gengoai.apollo.model.Params;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.concurrent.Threads;
import com.gengoai.math.Math2;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static com.gengoai.function.Functional.with;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>Wrapper around Spark's Word2Vec embedding model.</p>
 *
 * @author David B. Bracewell
 */
public class LocalWord2Vec extends TrainableWordEmbedding<LocalWord2Vec.Parameters, LocalWord2Vec> {
   private static final long serialVersionUID = 1L;

   public LocalWord2Vec() {
      super(new Parameters());
   }

   public LocalWord2Vec(@NonNull LocalWord2Vec.Parameters parameters) {
      super(parameters);
   }

   public LocalWord2Vec(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }


   @Override
   public void estimate(@NonNull DataSet dataset) {
      HuffmanEncoder huffmanEncoder = new HuffmanEncoder();
      huffmanEncoder.fit(dataset.stream().flatMap(datum -> datum.stream(parameters.inputs.value())));
      //Need to fit the huffman encoder to the dataset to learn the tree

      //Setup the Neural Network
      Map<Integer, NumericNDArray> syn0 = new HashMap<>();
      Map<Integer, NumericNDArray> syn1 = new HashMap<>();

      for (int i = 0; i < huffmanEncoder.size(); i++) {
         syn0.put(i, nd.DFLOAT32.create(1, parameters.dimension.value(), NDArrayInitializer.gaussian));
         syn0.get(i).setLabel(huffmanEncoder.decode(i));
         syn1.put(i, nd.DFLOAT32.create(1, parameters.dimension.value(), NDArrayInitializer.gaussian));
         syn1.get(i).setLabel(huffmanEncoder.decode(i));
      }

      //Train
      ExecutorService executorService = Executors.newFixedThreadPool(SystemInfo.NUMBER_OF_PROCESSORS);
      dataset.batchIterator(100)
             .forEachRemaining(d -> executorService.submit(new WorkerThread(huffmanEncoder, d, syn0, syn1)));
      executorService.shutdown();
      while (!executorService.isTerminated()) {
         Threads.sleep(3000);
      }

      var src = "person";
      var srcid = huffmanEncoder.encode(src);
      var a = syn0.get(huffmanEncoder.encode("person"));
      PriorityQueue<Tuple2<String, Double>> scores = new PriorityQueue<>(Map.Entry.comparingByValue());

      syn0.forEach((key, value) -> {
         if (!key.equals(srcid)) {
            double sim = Similarity.Cosine.calculate(value, a);
            scores.add($(huffmanEncoder.decode(key), sim));
            if (scores.size() > 10) {
               scores.remove();
            }
         }
      });

      for (Tuple2<String, Double> score : scores) {
         System.out.println(score);
      }

      //Create Vector Store
      vectorStore = new InMemoryVectorStore(parameters.dimension.value(),
                                            parameters.unknownWord.value(),
                                            parameters.specialWords.value());

      syn0.forEach((idx, vector) -> vectorStore.updateVector(idx, vector));
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

   private class WorkerThread extends Thread {
      private final HuffmanEncoder huffmanEncoder;
      private final DataSet batch;
      private final Random random = new Random();
      private final Map<Integer, NumericNDArray> syn0;
      private final Map<Integer, NumericNDArray> syn1;
      int wordCount = 0;
      private NumericNDArray tempNeu = nd.DFLOAT32.zeros(parameters.dimension.value());

      private WorkerThread(HuffmanEncoder huffmanEncoder, DataSet batch, Map<Integer, NumericNDArray> syn0, Map<Integer, NumericNDArray> syn1) {
         this.huffmanEncoder = huffmanEncoder;
         this.batch = batch;
         this.syn0 = syn0;
         this.syn1 = syn1;
      }

      @Override
      public void run() {
         Stopwatch sw = Stopwatch.createStarted();
         for (int i = 0; i < parameters.maxIterations.value(); i++) {
            batch.forEach(datum -> {
               List<String> filtered = new ArrayList<>();
               datum.stream(parameters.inputs.value())
                    .flatMap(Observation::getVariableSpace)
                    .map(Variable::getName)
                    .forEach(word -> {
                       if (huffmanEncoder.getAlphabet().contains(word)) {
                          wordCount++;
                          //Downsampling??
                          filtered.add(word);
                       }
                    });
               wordCount++;
               trainOne(filtered);
            });
         }
         sw.stop();
         System.out.println(Thread.currentThread().getId() + " : " + sw);
         super.run();
      }

      private void trainOne(List<String> sentence) {
         int window = parameters.windowSize.value();
         for (int i = 0; i < sentence.size(); i++) {
            HuffmanEncoder.HuffmanNode word = huffmanEncoder.getNode(sentence.get(i));
            if (word == null) continue;

            int b = random.nextInt(window);
            for (int a = b; a < window * 2 + 1 - b; a++) {
               if (a == window) continue;

               int c = i - window + a;
               if (c >= 0 && c < sentence.size()) {
                  HuffmanEncoder.HuffmanNode lastWord = huffmanEncoder.getNode(sentence.get(c));
                  if (lastWord == null) continue;
                  var l1 = lastWord.getIndex();
                  var neu1e = nd.DFLOAT32.zeros(parameters.dimension.value());

                  for (int d = 0; d < word.getCode().length; d++) {
                     int l2 = word.getPoint()[d];
                     var f = Math2.exp(syn0.get(l1).dot(syn1.get(l2)));
                     var g = ((1.0 - word.getCode()[d]) - f) * parameters.learningRate.value();
                     neu1e.addi(syn1.get(l2).mul(g));
                     syn1.get(l2).addi(syn0.get(l1).mul(g));
                  }

                  syn0.get(l1).addi(neu1e);
               }

            }
         }
      }
   }

}//END OF Word2Vec

