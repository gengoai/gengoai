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

package com.gengoai.apollo.ml.transform.vectorizer;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.encoder.IndexEncoder;
import com.gengoai.apollo.ml.observation.Variable;

/**
 * <p>
 * Standard OneHot Encoding. The resulting NDArray have a dimension of 1 x <code>Encoder.size</code> for non-sequences
 * and <code>Sequence.length</code> x <code>Encoder.size</code>  for sequences.
 * </p>
 *
 * @author David B. Bracewell
 */
public class OneHotVectorizer extends AbstractVariableVectorizer<OneHotVectorizer> {

   /**
    * Instantiates a new OneHotVectorizer.
    */
   public OneHotVectorizer() {
      super(new IndexEncoder());
   }

   /**
    * Instantiates a new OneHotVectorizer which will map unknown (out-of-vocabulary) feature names  to the given unknown
    * name.
    *
    * @param unknownName The name to replace out-of-vocabulary feature names with.
    */
   public OneHotVectorizer(String unknownName) {
      super(new IndexEncoder(unknownName));
   }

   @Override
   protected void encodeVariableInto(Variable v, NDArray ndArray) {
      int index = encoder.encode(v.getName());
      if(index >= 0) {
         ndArray.set(index, 1.0);
      }
   }
}//END OF OneHotVectorizer
