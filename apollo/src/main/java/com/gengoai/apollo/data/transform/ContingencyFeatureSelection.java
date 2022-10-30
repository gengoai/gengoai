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

package com.gengoai.apollo.data.transform;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.math.measure.Association;
import com.gengoai.apollo.math.measure.ContingencyTable;
import com.gengoai.collection.counter.HashMapMultiCounter;
import com.gengoai.collection.counter.MultiCounter;
import com.gengoai.stream.MCounterAccumulator;
import com.gengoai.stream.MMultiCounterAccumulator;
import com.gengoai.stream.MStream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Uses a {@link Association} to perform feature selection by taking the top N features per label
 * based on the Association measure.</p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ContingencyFeatureSelection extends AbstractSingleSourceTransform<ContingencyFeatureSelection> {
   private static final long serialVersionUID = 1L;
   @NonNull
   @JsonProperty
   private final String labelSource;
   @JsonProperty
   private final int numFeaturesPerClass;
   @JsonProperty
   private final double threshold;
   @NonNull
   @JsonProperty
   private final Association calculator;


   /**
    * Instantiates a new ContingencyFeatureSelection.
    *
    * @param labelSource         the label source
    * @param numFeaturesPerClass the num features per label
    * @param threshold           the minimum value from the calculator to accept
    * @param calculator          the calculator to use to generate statistics about features and labels
    */
   public ContingencyFeatureSelection(@NonNull String labelSource,
                                      int numFeaturesPerClass,
                                      double threshold,
                                      @NonNull Association calculator) {
      this.labelSource = labelSource;
      this.numFeaturesPerClass = numFeaturesPerClass;
      this.threshold = threshold;
      this.calculator = calculator;
   }



   @Override
   public DataSet fitAndTransform(DataSet dataset) {
      final Set<String> features = new HashSet<>();
      MCounterAccumulator<String> labelCounts = dataset.getType().getStreamingContext().counterAccumulator();
      MMultiCounterAccumulator<String, String> featureLabelCounts = dataset.getType()
                                                                           .getStreamingContext()
                                                                           .multiCounterAccumulator();
      // Calculate Label-Feature Coccurrences
      dataset.parallelStream().forEach(d -> {
         String label = d.get(labelSource).asVariable().getName();
         labelCounts.increment(label, 1);
         MultiCounter<String, String> localCounts = new HashMapMultiCounter<>();
         d.get(input).getVariableSpace()
          .forEach(f -> localCounts.increment(f.getName(), label));
         featureLabelCounts.merge(localCounts);
      });

      double totalCount = labelCounts.value().sum();
      for (String label : labelCounts.value().items()) {
         double labelCount = labelCounts.value().get(label);
         Map<String, Double> featureScores = new HashMap<>();

         for (String feature : featureLabelCounts.value().firstKeys()) {
            double featureLabelCount = featureLabelCounts.value().get(feature, label);
            double featureSum = featureLabelCounts.value().get(feature).sum();
            if (featureLabelCount > 0) {
               double score = calculator.calculate(ContingencyTable.create2X2(featureLabelCount,
                                                                              labelCount,
                                                                              featureSum,
                                                                              totalCount));
               featureScores.put(feature, score);
            }
         }

         List<Map.Entry<String, Double>> entryList = featureScores.entrySet()
                                                                  .stream()
                                                                  .sorted(Map.Entry.<String, Double>comparingByValue()
                                                                                .reversed())
                                                                  .filter(e -> e.getValue() >= threshold)
                                                                  .collect(Collectors.toList());

         if (entryList.size() > 0) {
            entryList.subList(0, Math.min(numFeaturesPerClass, entryList.size()))
                     .forEach(e -> features.add(e.getKey()));
         }
      }

      return dataset.map(d -> {
         Observation o = d.get(input).copy();
         o.removeVariables(v -> !features.contains(v.getName()));
         d.put(output, o);
         return d;
      });
   }

   @Override
   protected void fit(@NonNull MStream<Observation> observations) {
   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      return observation;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {

   }

   @Override
   public String toString() {
      return "ContingencyFeatureSelection{input='"
            + input
            + "', output='"
            + output
            + "', labelSource='"
            + labelSource
            +"', numFeaturesPerClass="
            + numFeaturesPerClass
            +", threshold="
            + threshold
            + ", calculator="
            + calculator
            +"}";

   }
}//END OF ContingencyFeatureSelection
