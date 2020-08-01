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
import com.gengoai.function.SerializableFunction;
import com.gengoai.stream.MStream;
import lombok.NonNull;

/**
 * <p>A {@link SingleSourceTransform} that maps {@link Variable} into new values.</p>
 *
 * @author David B. Bracewell
 */
public class MappingTransform extends AbstractSingleSourceTransform<MappingTransform> {
   private final SerializableFunction<Variable, Variable> mapper;

   public MappingTransform(@NonNull SerializableFunction<Variable, Variable> mapper) {
      this.mapper = mapper;
   }

   @Override
   protected void fit(@NonNull MStream<Observation> observations) {

   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      observation.mapVariables(mapper);
      return observation;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {

   }
}//END OF MappingTransform
