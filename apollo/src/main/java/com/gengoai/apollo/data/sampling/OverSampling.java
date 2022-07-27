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

package com.gengoai.apollo.data.sampling;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.collection.counter.Counter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.List;

/**
 * <p>Generates copies of classes in order to balance the distribution of labels in the data set.</p>
 */
public class OverSampling extends BaseObservationDataSetSampler implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new OverSampling.
    *
    * @param observationName the name of the label observation we want to balance.
    */
   public OverSampling(@NonNull String observationName) {
      super(observationName);
   }


   @Override
   public DataSet sample(@NonNull DataSet dataSet) {
      Counter<String> fCount = calculateClassDistribution(dataSet);
      int targetCount = (int) fCount.maximumCount();
      List<Datum> outputData = dataSet.collect();
      for (String label : fCount.items()) {
         if (fCount.get(label) < targetCount) {
            dataSet.stream()
                   .filter(e -> e.get(getObservationName())
                                 .getVariableSpace()
                                 .map(Variable::getName)
                                 .anyMatch(label::equals))
                   .map(Datum::copy)
                   .take((int) (targetCount - fCount.get(label)))
                   .forEach(d -> outputData.add(d.copy()));
         }
      }
      return DataSet.of(outputData)
                    .setNDArrayFactory(dataSet.getNDArrayFactory())
                    .putAllMetadata(dataSet.getMetadata());
   }
}//END OF OverSampling
