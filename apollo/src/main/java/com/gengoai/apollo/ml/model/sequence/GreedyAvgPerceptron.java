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

package com.gengoai.apollo.ml.model.sequence;

import com.gengoai.Stopwatch;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.encoder.IndexEncoder;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.model.SingleSourceFitParameters;
import com.gengoai.apollo.ml.model.SingleSourceModel;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.apollo.ml.model.StoppingCriteria;
import com.gengoai.collection.HashBasedTable;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.Table;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.collection.counter.MultiCounter;
import com.gengoai.collection.counter.MultiCounters;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Consumer;

import static com.gengoai.LogUtils.logInfo;
import static com.gengoai.function.Functional.with;

/**
 * <p>A greedy sequence labeler that uses an Averaged Perceptron for classification. First-ordered transitions are
 * included as features.</p>
 *
 * @author David B. Bracewell
 */
@Log
public class GreedyAvgPerceptron extends SingleSourceModel<GreedyAvgPerceptron.Parameters, GreedyAvgPerceptron> {
   private static final long serialVersionUID = 1L;
   private static final Variable BIAS_FEATURE = Variable.binary("******BIAS******");
   private final MultiCounter<String, String> featureWeights = MultiCounters.newMultiCounter();
   private final MultiCounter<String, String> transitionWeights = MultiCounters.newMultiCounter();

   /**
    * Instantiates a new GreedyAvgPerceptron with default parameters.
    */
   public GreedyAvgPerceptron() {
      super(new Parameters());
   }

   /**
    * Instantiates a new GreedyAvgPerceptron with the given parameters.
    *
    * @param parameters the parameters
    */
   public GreedyAvgPerceptron(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new GreedyAvgPerceptron with the given parameter updater.
    *
    * @param updater the updater
    */
   public GreedyAvgPerceptron(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   private void average(int finalIteration,
                        MultiCounter<String, String> weights,
                        Table<String, String, Integer> timeStamp,
                        MultiCounter<String, String> totals
                       ) {
      for(String feature : new HashSet<>(weights.firstKeys())) {
         Counter<String> newWeights = Counters.newCounter();
         weights.get(feature)
                .forEach((cls, value) -> {
                   double total = totals.get(feature, cls);
                   total += (finalIteration - timeStamp.getOrDefault(feature, cls, 0)) * value;
                   double v = total / finalIteration;
                   if(Math.abs(v) >= 0.001) {
                      newWeights.set(cls, v);
                   }
                });
         weights.set(feature, newWeights);
      }
   }

   private Counter<String> distribution(Observation example, String pLabel) {
      Counter<String> scores = Counters.newCounter(transitionWeights.get(pLabel));
      for(Variable feature : expandFeatures(example)) {
         scores.merge(featureWeights.get(feature.getName())
                                    .adjustValues(v -> v * feature.getValue()));
      }
      return scores;
   }

   @Override
   public void estimate(DataSet preprocessed) {
      IndexEncoder encoder = new IndexEncoder();
      encoder.fit(preprocessed.stream()
                              .flatMap(d -> d.stream(parameters.output.value()))
                              .flatMap(Observation::getVariableSpace));
      featureWeights.clear();
      transitionWeights.clear();
      final MultiCounter<String, String> fTotals = MultiCounters.newMultiCounter();
      final MultiCounter<String, String> tTotals = MultiCounters.newMultiCounter();
      final Table<String, String, Integer> fTimestamps = new HashBasedTable<>();
      final Table<String, String, Integer> tTimestamps = new HashBasedTable<>();
      final String defaultLabel = encoder.decode(0);
      int instances = 0;
      StoppingCriteria stoppingCriteria = StoppingCriteria.create("pct_error", parameters);
      for(int i = 0; i < stoppingCriteria.maxIterations(); i++) {
         Stopwatch sw = Stopwatch.createStarted();
         double total = 0;
         double correct = 0;

         for(Datum datum : preprocessed.shuffle().stream()) {
            String pLabel = "<BOS>";
            Sequence<?> sequence = datum.get(parameters.input.value()).asSequence();
            Sequence<?> labels = datum.get(parameters.output.value()).asSequence();

            for(int j = 0; j < sequence.size(); j++) {
               total++;
               instances++;
               Observation instance = sequence.get(j);
               String y = labels.get(j).asVariable().getName();
               String predicted = distribution(instance, pLabel).max();
               if(predicted == null) {
                  predicted = defaultLabel;
               }
               if(!y.equals(predicted)) {
                  for(Variable feature : expandFeatures(instance)) {
                     update(y, feature.getName(), 1.0, instances, featureWeights, fTimestamps, fTotals);
                     update(predicted, feature.getName(), -1.0, instances, featureWeights, fTimestamps, fTotals);
                  }
                  update(y, pLabel, 1.0, instances, transitionWeights, fTimestamps, fTotals);
                  update(predicted, pLabel, -1.0, instances, transitionWeights, fTimestamps, fTotals);
               } else {
                  correct++;
               }
               pLabel = y;
            }
         }
         double error = 1d - (correct / total);

         sw.stop();
         if(parameters.verbose.value()) {
            logInfo(log, "Iteration {0}: Accuracy={1,number,#.####}, time to complete={2}", i + 1, (1d - error), sw);
         }

         if(stoppingCriteria.check(error)) {
            break;
         }
      }

      average(instances, featureWeights, fTimestamps, fTotals);
      average(instances, transitionWeights, tTimestamps, tTotals);
   }

   private Iterable<Variable> expandFeatures(Observation example) {
      if(example.isVariableCollection()) {
         return Iterables.concat(example.asVariableCollection(), Collections.singleton(BIAS_FEATURE));
      }
      return Arrays.asList(example.asVariable(), BIAS_FEATURE);
   }

   @Override
   public Parameters getFitParameters() {
      return parameters;
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if(parameters.output.value().equals(name)) {
         return LabelType.Sequence;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      Sequence<?> sequence = observation.asSequence();
      VariableSequence out = new VariableSequence();
      String pLabel = "<BOS>";
      for(Observation instance : sequence) {
         Counter<String> distribution = distribution(instance, pLabel);
         String cLabel = distribution.max();
         double score = distribution.get(cLabel);
         distribution.remove(cLabel);
         while(!parameters.validator.value().isValid(cLabel, pLabel, instance)) {
            cLabel = distribution.max();
            score = distribution.get(cLabel);
            distribution.remove(cLabel);
         }
         pLabel = cLabel;
         out.add(Variable.real(cLabel, score));
      }
      return out;
   }

   private void update(String cls, String feature, double value, int iteration,
                       MultiCounter<String, String> weights,
                       Table<String, String, Integer> timeStamp,
                       MultiCounter<String, String> totals
                      ) {
      int iterAt = iteration - timeStamp.getOrDefault(feature, cls, 0);
      totals.increment(feature, cls, iterAt * weights.get(feature, cls));
      weights.increment(feature, cls, value);
      timeStamp.put(feature, cls, iteration);
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      data.updateMetadata(parameters.output.value(), m -> {
         m.setEncoder(null);
         m.setType(VariableSequence.class);
         m.setDimension(-1);
      });
   }

   /**
    * Custom fit parameters for the GreedyAveragePerceptron
    */
   public static class Parameters extends SingleSourceFitParameters<Parameters> {
      private static final long serialVersionUID = 1L;
      /**
       * The sequence validator to user during inference (default {@link SequenceValidator#ALWAYS_TRUE})
       */
      public final Parameter<SequenceValidator> validator = parameter(Params.Sequence.validator,
                                                                      SequenceValidator.ALWAYS_TRUE);
      /**
       * The epsilon to use for checking for convergence (default 1e-3).
       */
      public final Parameter<Double> tolerance = parameter(Params.Optimizable.tolerance, 1e-3);
      /**
       * The number of iterations to use for determining convergence (default 3).
       */
      public final Parameter<Integer> historySize = parameter(Params.Optimizable.historySize, 3);
      /**
       * The maximum number of iterations to run for (default 100).
       */
      public final Parameter<Integer> maxIterations = parameter(Params.Optimizable.maxIterations, 100);
   }

}//END OF WindowSequenceLabeler
