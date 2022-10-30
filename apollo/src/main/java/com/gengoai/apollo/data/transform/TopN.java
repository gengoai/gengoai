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
import com.gengoai.Validation;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.collection.Sets;
import com.gengoai.collection.counter.Counters;
import com.gengoai.stream.MStream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Set;

/**
 * The type Top n.
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class TopN extends AbstractSingleSourceTransform<TopN> {
   @JsonProperty
   private final int topN;
   @JsonProperty
   private Set<String> selected;

   /**
    * Instantiates a new Top n.
    *
    * @param topN the top n
    */
   public TopN(int topN) {
      Validation.checkArgument(topN > 0, "TopN must be > 0");
      this.topN = topN;
   }


   @Override
   public String toString() {
      return "TopN{" +
            "input='" + input + '\'' +
            ", output='" + output + '\'' +
            ", topN=" + topN +
            '}';
   }

   @Override
   protected void fit(@NonNull MStream<Observation> observations) {
      selected = Sets.asHashSet(Counters.newCounter(observations.parallel()
                                                                .flatMap(Observation::getVariableSpace)
                                                                .map(Variable::getName)
                                                                .countByValue())
                                        .topN(topN)
                                        .items());
   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      observation.removeVariables(v -> !selected.contains(v.getName()));
      return observation;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {

   }
}//END OF TopN
