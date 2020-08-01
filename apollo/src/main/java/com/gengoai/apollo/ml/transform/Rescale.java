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

import com.gengoai.Validation;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.math.Math2;
import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Transforms features values to a new minimum and maximum based on the current minimum and maximum of the values in
 * the dataset.</p>
 *
 * @author David B. Bracewell
 */
public class Rescale extends PerPrefixTransform<Rescale> {
   private static final long serialVersionUID = 1L;
   private final double newMin;
   private final double newMaX;
   private final Map<String, Double> mins = new HashMap<>();
   private final Map<String, Double> maxs = new HashMap<>();

   /**
    * Instantiates a new Rescale.
    *
    * @param newMin the new min
    * @param newMaX the new ma x
    */
   public Rescale(double newMin, double newMaX) {
      Validation.checkArgument(newMaX > newMaX, "Max must be > min");
      this.newMin = newMin;
      this.newMaX = newMaX;
   }

   @Override
   protected void fit(@NonNull String prefix, @NonNull Iterable<Variable> variables) {
      DoubleSummaryStatistics stats = Streams.asStream(variables)
                                             .collect(Collectors.summarizingDouble(Variable::getValue));
      mins.put(prefix, stats.getMin());
      maxs.put(prefix, stats.getMax());
   }

   @Override
   protected void reset() {
      mins.clear();
      maxs.clear();
   }

   @Override
   protected Variable transform(@NonNull Variable variable) {
      String name = variable.getPrefix();
      variable.setValue(Math2.rescale(variable.getValue(), mins.get(name), maxs.get(name), newMin, newMaX));
      return variable;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {

   }

}//END OF StandardScalar
