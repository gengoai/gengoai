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
import com.gengoai.apollo.ml.encoder.HashingEncoder;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableList;
import com.gengoai.stream.MStream;
import lombok.NonNull;

/**
 * <p>
 * Hashes the namespace of a given observation using the Hashing trick output an Instance of binary features with the
 * pre-defined prefix and the suffix as the index of the hashed name.
 * </p>
 *
 * @author David B. Bracewell
 */
public class HashingTransform extends AbstractSingleSourceTransform<HashingTransform> {
   private final HashingEncoder encoder;
   private final String prefix;

   /**
    * Instantiates a new HashingTransform
    *
    * @param prefix           the prefix to use for the Variables that are created.
    * @param numberOfFeatures the number of features (i.e. the number of possible Variable suffixes).
    */
   public HashingTransform(@NonNull String prefix, int numberOfFeatures) {
      this.encoder = new HashingEncoder(numberOfFeatures);
      this.prefix = prefix;
   }

   @Override
   protected void fit(@NonNull MStream<Observation> observations) {

   }

   @Override
   public Observation transform(@NonNull Observation observation) {
      return new VariableList(observation.getVariableSpace()
                                         .map(n -> Variable.binary(prefix,
                                                                   Integer.toString(encoder.encode(n.getName())))));

   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      data.updateMetadata(output, m -> {
         m.setDimension(encoder.size());
         m.setEncoder(encoder);
         m.setType(VariableList.class);
      });
   }

}//END OF HashingTransform
