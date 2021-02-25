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

import com.gengoai.SystemInfo;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.statistics.measure.Similarity;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.DataSetGenerator;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.InMemoryDataSet;
import com.gengoai.apollo.ml.encoder.HuffmanEncoder;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.collection.Lists;
import com.gengoai.concurrent.Threads;
import com.gengoai.math.Math2;
import com.gengoai.stream.StreamingContext;
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

   public static void main(String[] args) {
      String[] text = StreamingContext.local()
                                      .textFile("/Volumes/ikdata/corpora/en/Raw/news_1m_sentences.txt")
                                      .limit(50000)
                                      .map(s -> s.replaceAll("\\p{P}+", "").toLowerCase())
                                      .javaStream().toArray(String[]::new);
      DataSetGenerator<String> generator = DataSetGenerator.<String>builder()
            .defaultInput(s -> VariableSequence.from(s.toString().split("\\s+")))
            .build();
      DataSet data = generator.generate(Arrays.asList(text)).cache();
      LocalWord2Vec word2Vec = new LocalWord2Vec();
      word2Vec.getFitParameters().dimension.set(50);
      word2Vec.getFitParameters().maxIterations.set(10);
      word2Vec.fitAndTransform(data);
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
      Map<Integer, NDArray> syn0 = new HashMap<>();
      Map<Integer, NDArray> syn1 = new HashMap<>();

      for (int i = 0; i < huffmanEncoder.size(); i++) {
         syn0.put(i, NDArrayFactory.ND.rand(1, parameters.dimension.value()));
         syn0.get(i).setLabel(huffmanEncoder.decode(i));
         syn1.put(i, NDArrayFactory.ND.rand(1, parameters.dimension.value()));
         syn1.get(i).setLabel(huffmanEncoder.decode(i));
      }

      int batchSize = (int) Math.min(dataset.size() / SystemInfo.NUMBER_OF_PROCESSORS, 1000);
      //Train
      ExecutorService executorService = Executors.newFixedThreadPool(SystemInfo.NUMBER_OF_PROCESSORS);
      dataset.batchIterator(batchSize)
             .forEachRemaining(d -> executorService.submit(new WorkerThread(huffmanEncoder, d, syn0, syn1)));
      executorService.shutdown();
      while (!executorService.isTerminated()) {
         Threads.sleep(3000);
      }

      var src = "villain";
      var srcid = huffmanEncoder.encode(src);
      var a = syn0.get(huffmanEncoder.encode("villain"));
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


   private class WorkerThread extends Thread {
      private final HuffmanEncoder huffmanEncoder;
      private final DataSet batch;
      private NDArray tempNeu = NDArrayFactory.ND.array(1, parameters.dimension.value());
      private final Random random = new Random();
      private final Map<Integer, NDArray> syn0;
      private final Map<Integer, NDArray> syn1;
      int wordCount = 0;

      private WorkerThread(HuffmanEncoder huffmanEncoder, DataSet batch, Map<Integer, NDArray> syn0, Map<Integer, NDArray> syn1) {
         this.huffmanEncoder = huffmanEncoder;
         this.batch = batch;
         this.syn0 = syn0;
         this.syn1 = syn1;
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
                  var neu1e = NDArrayFactory.DENSE.array(1, parameters.dimension.value());

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

      @Override
      public void run() {
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
            System.out.println(Thread.currentThread().getId() + " : iteration=" + (i + 1));
         }
         super.run();
      }
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

