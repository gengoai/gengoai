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

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.VariableCollection;
import com.gengoai.apollo.data.observation.VariableList;
import com.gengoai.stream.MStream;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = true)
public class Sequence2Instance extends AbstractSingleSourceTransform<Sequence2Instance> {
   private static final long serialVersionUID = 1L;


   @Override
   public String toString() {
      return "Sequence2Instance{" +
            "input='" + input + '\'' +
            ", output='" + output + '\'' +
            '}';
   }

   @Override
   protected void fit(@NonNull MStream<Observation> observations) {

   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      return new VariableList(observation.getVariableSpace());
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      data.updateMetadata(output, m -> {
         m.setDimension(-1);
         m.setEncoder(null);
         m.setType(VariableCollection.class);
      });
   }
}//END OF Sequence2Instance
