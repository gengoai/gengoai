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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.Variable;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collections;
import java.util.Set;

/**
 * <p>Replaces non-finite values in Variables with a given replacement value</p>
 *
 * @author David B. Bracewell
 */
@ToString
@EqualsAndHashCode
public class ReplaceNonFinite implements Transform {
   @JsonProperty
   private final double replacement;

   @JsonCreator
   public ReplaceNonFinite(@JsonProperty("replacement") double replacement) {
      this.replacement = replacement;
   }

   @Override
   public DataSet fitAndTransform(@NonNull DataSet dataset) {
      return transform(dataset);
   }

   @Override
   public Set<String> getInputs() {
      return Collections.emptySet();
   }

   @Override
   public Set<String> getOutputs() {
      return Collections.emptySet();
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      Datum out = datum.copy();
      for (String s : out.keySet()) {
         Observation o = out.get(s);
         if (o instanceof Variable) {
            if (!Double.isFinite(o.asVariable().getValue())) {
               o.asVariable().setValue(replacement);
            }
         }
      }
      return out;
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      return dataset.map(this::transform);
   }


}//END OF ReplaceNonFinite
