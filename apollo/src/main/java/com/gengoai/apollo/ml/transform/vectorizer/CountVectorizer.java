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
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.IndexEncoder;
import com.gengoai.apollo.ml.observation.Variable;
import lombok.NonNull;

/**
 * <p>
 * Encodes a collection of {@link Variable} into an {@link NDArray} by summing their values. The resulting NDArray have
 * a dimension of 1 x <code>Encoder.size</code> for non-sequences and <code>Sequence.length</code> x
 * <code>Encoder.size</code>  for sequences.
 * </p>
 *
 * @author David B. Bracewell
 */
public class CountVectorizer extends AbstractVariableVectorizer<CountVectorizer> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new CountVectorizer with a specified {@link Encoder}
    *
    * @param encoder the encoder
    */
   public CountVectorizer(@NonNull Encoder encoder) {
      super(encoder);
   }

   /**
    * Instantiates a new CountVectorizer with an {@link IndexEncoder}.
    */
   public CountVectorizer() {
      super(new IndexEncoder());
   }

   /**
    * Instantiates a new CountVectorizer with an {@link IndexEncoder} with the unknown name set to the given value.
    *
    * @param unknownName the unknown name
    */
   public CountVectorizer(String unknownName) {
      super(new IndexEncoder(unknownName));
   }

   @Override
   protected void encodeVariableInto(Variable v, NDArray ndArray) {
      int index = encoder.encode(v.getName());
      if(index >= 0) {
         ndArray.set(index, ndArray.get(index) + v.getValue());
      }
   }

}//END OF CountVectorizer
