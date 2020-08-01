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

package com.gengoai.apollo.ml.data.sampling;

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.collection.counter.Counter;
import com.gengoai.stream.MCounterAccumulator;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
public abstract class BaseObservationDataSetSampler implements DataSetSampler, Serializable {
   private static final long serialVersionUID = 1L;
   @NonNull
   private String observationName;

   public BaseObservationDataSetSampler(@NonNull String observationName) {
      this.observationName = observationName;
   }

   /**
    * Calculates the distribution of classes in the data set
    *
    * @return A counter containing the classes (labels) and their counts in the dataset
    */
   protected Counter<String> calculateClassDistribution(@NonNull DataSet dataSet) {
      MCounterAccumulator<String> accumulator = dataSet.getStreamingContext().counterAccumulator();
      dataSet.stream().flatMap(e -> e.get(observationName)
                                     .getVariableSpace()
                                     .map(Variable::getName)).forEach(accumulator::add);
      return accumulator.value();
   }

}//END OF BaseDataSetSampler
