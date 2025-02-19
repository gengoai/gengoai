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

package com.gengoai.apollo.model.topic;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.TokenSequence;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.conversion.Cast;

import java.io.Serializable;

/**
 * Converts an Instance which has an Apollo Example as the data into a TokenSequence
 *
 * @author David B. Bracewell
 */
class InstanceToTokenSequence extends Pipe implements Serializable {
   private static final long serialVersionUID = 1L;

   @Override
   public Instance pipe(Instance inst) {
      Observation vector = Cast.as(inst.getData());
      TokenSequence sequence = new TokenSequence();
      vector.getVariableSpace().forEach(v -> sequence.add(v.getName()));
      inst.setData(sequence);
      return inst;
   }
}//END OF InstanceToTokenSequence
