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
import com.gengoai.stream.MStream;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * A {@link Transform} that filters out {@link Variable}s whose name occurs less than the given count.
 * </p>
 */
public class MinCountFilter extends AbstractSingleSourceTransform<MinCountFilter> {
   private static final long serialVersionUID = 1L;
   private final int minCount;
   private final Set<String> vocab = new HashSet<>();
   private final String unknown;

   /**
    * Instantiates a new MinCountFilter.
    *
    * @param minCount the minimum number of Datum a Variable must appear in inorder to be kept.
    */
   public MinCountFilter(int minCount) {
      this(minCount, null);
   }

   /**
    * Instantiates a new MinCountFilter.
    *
    * @param minCount the minimum number of Datum a Variable must appear in inorder to be kept.
    * @param unknown  the dummy name to replace Variable names with that are not in the vocabulary.
    */
   public MinCountFilter(int minCount, String unknown) {
      this.minCount = minCount;
      this.unknown = Strings.emptyToNull(unknown);
   }

   @Override
   protected void fit(@NonNull MStream<Observation> observations) {
      Map<String, Long> m = observations.flatMap(Observation::getVariableSpace)
                                        .map(Variable::getName)
                                        .collect(Collectors.groupingBy(i -> i, Collectors.counting()));
      m.values().removeIf(v -> v < minCount);
      vocab.clear();
      vocab.addAll(m.keySet());
   }

   @Override
   public Observation transform(Observation o) {
      if(unknown == null) {
         o.removeVariables(v -> !vocab.contains(v.getName()));
      } else {
         o.mapVariables(v -> {
            if(vocab.contains(v.getName())) {
               return v;
            }
            return Variable.real(unknown, v.getValue());
         });
      }
      return o;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {

   }

}//END OF MinCountFilter
