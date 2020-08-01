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

package com.gengoai.apollo.ml.model;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableList;
import com.gengoai.conversion.Cast;

import java.io.Serializable;

/**
 * Specialized Mallet pipe to create feature vectors from examples.
 *
 * @author David B. Bracewell
 */
class VectorToTokensPipe extends Pipe implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Alphabet alphabet;

   /**
    * Instantiates a new VectorToTokensPipe.
    *
    * @param encoder the encoder
    */
   public VectorToTokensPipe(Alphabet encoder) {
      this.alphabet = encoder;
   }

   @Override
   public Instance pipe(Instance inst) {
      VariableList vector = Cast.as(inst.getData());
      String[] names = new String[vector.size()];
      double[] values = new double[vector.size()];
      for(int i = 0; i < vector.size(); i++) {
         Variable f = vector.get(i);
         names[i] = f.getName();
         values[i] = f.getValue();
      }
      inst.setData(new FeatureVector(alphabet, names, values));
      return inst;
   }

}//END OF VectorToTokensPipe
