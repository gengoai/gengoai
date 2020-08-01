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

package com.gengoai.apollo.ml.transform;

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableNameSpace;
import com.gengoai.stream.MStream;
import com.gengoai.stream.Streams;
import lombok.NonNull;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public class StandardScalar extends AbstractSingleSourceTransform<StandardScalar> {
   private static final long serialVersionUID = 1L;
   private final Map<String, Double> means = new HashMap<>();
   private final Map<String, Double> standardDeviations = new HashMap<>();
   private final VariableNameSpace namingPattern;

   public StandardScalar(@NonNull VariableNameSpace namingPattern) {
      this.namingPattern = namingPattern;
   }

   @Override
   protected void fit(@NonNull MStream<Observation> observations) {
      means.clear();
      standardDeviations.clear();
      observations.flatMap(Observation::getVariableSpace)
                  .groupBy(namingPattern::getName)
                  .forEachLocal((key, vars) -> {
                     StatisticalSummary summary = new DescriptiveStatistics(Streams.asStream(vars)
                                                                                   .mapToDouble(Variable::getValue)
                                                                                   .toArray());
                     means.put(key, summary.getMean());
                     standardDeviations.put(key, summary.getStandardDeviation());
                  });
   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      if(observation.isVariable()) {
         return updateVariable(observation.asVariable());
      }
      observation.mapVariables(this::updateVariable);
      return observation;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {

   }

   private Variable updateVariable(Variable v) {
      String prefix = namingPattern.getName(v);
      double mean = means.getOrDefault(prefix, 0.0);
      double std = standardDeviations.getOrDefault(prefix, 1.0) + 1e-4;
      double value = (v.getValue() - mean) / std;
      v.setValue(value);
      return v;
   }
}//END OF StandardScalar
