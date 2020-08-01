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
import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Converts a real valued feature into a number of binary features by creating a <code>bin</code> number of new
 * binary features. Creates <code>bin</code> number of equal sized bins that the feature values can fall into.</p>
 *
 * @author David B. Bracewell
 */
public class BinTransform extends PerPrefixTransform<BinTransform> {
   private static final long serialVersionUID = 1L;
   private final int numberOfBins;
   private final Map<String, double[]> prefixBinMap = new HashMap<>();
   private final boolean includeSuffix;

   /**
    * Instantiates a new BinTransform with no restriction
    *
    * @param numberOfBins the number of bins to convert the feature into
    */
   public BinTransform(int numberOfBins, boolean includeSuffix) {
      Validation.checkArgument(numberOfBins > 0, "Number of bins must be > 0.");
      this.numberOfBins = numberOfBins;
      this.includeSuffix = includeSuffix;
   }

   @Override
   protected void fit(@NonNull String prefix, @NonNull Iterable<Variable> variables) {
      double[] bins = new double[numberOfBins];
      DoubleSummaryStatistics statistics = Streams.asStream(variables)
                                                  .collect(Collectors.summarizingDouble(Variable::getValue));
      double max = statistics.getMax();
      double min = statistics.getMin();
      double binSize = ((max - min) / numberOfBins);
      double sum = min;
      for(int i = 0; i < bins.length; i++) {
         sum += binSize;
         bins[i] = sum;
      }
      prefixBinMap.put(prefix, bins);
   }

   private int getBin(String prefix, double value) {
      int bin = 0;
      double[] bins = prefixBinMap.get(prefix);
      for(; bin < bins.length - 1; bin++) {
         if(value < bins[bin]) {
            break;
         }
      }
      return bin;
   }

   @Override
   protected void reset() {
      prefixBinMap.clear();
   }

   @Override
   protected Variable transform(@NonNull Variable variable) {
      if(includeSuffix) {
         return Variable.binary(variable.getPrefix(),
                                variable.getSuffix() + "-Bin[" + getBin(variable.getPrefix(),
                                                                        variable.getValue()) + "]");
      }
      return Variable.binary(variable.getPrefix(), "Bin[" + getBin(variable.getPrefix(), variable.getValue()) + "]");
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {

   }
}//END OF BinTransform
