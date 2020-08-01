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

import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.stream.MStream;
import lombok.NonNull;

import static com.gengoai.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
public abstract class PerPrefixTransform<T extends PerPrefixTransform<T>> extends AbstractSingleSourceTransform<T> {

   @Override
   protected final void fit(@NonNull MStream<Observation> observations) {
      reset();
      observations.flatMapToPair(o -> o.getVariableSpace()
                                       .map(v -> $(v.getPrefix(), v)))
                  .groupByKey()
                  .forEachLocal(this::fit);
   }

   protected abstract void fit(@NonNull String prefix, @NonNull Iterable<Variable> variables);

   protected abstract void reset();

   protected abstract Variable transform(@NonNull Variable variable);

   @Override
   protected Observation transform(@NonNull Observation observation) {
      if(observation instanceof Variable) {
         return transform(observation.asVariable());
      }
      observation.mapVariables(this::transform);
      return observation;
   }

}//END OF PerPrefixTransform
