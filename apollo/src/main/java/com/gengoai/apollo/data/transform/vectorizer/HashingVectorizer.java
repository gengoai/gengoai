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

package com.gengoai.apollo.data.transform.vectorizer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.encoder.HashingEncoder;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 * A {@link Vectorizer} that uses the hashing trick to lower the number of dimensions.The resulting NDArray have a
 * dimension of 1 x <code>Encoder.size</code> for non-sequences and <code>Sequence.length</code> x
 * <code>Encoder.size</code>  for sequences.</p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
public class HashingVectorizer extends AbstractVariableVectorizer<HashingVectorizer> {
   private static final long serialVersionUID = 1L;
   @JsonProperty
   private final boolean isBinary;


   /**
    * Instantiates a new HashingVectorizer
    *
    * @param numFeatures the number of features (must be > 0)
    * @param isBinary    True - the output is treated as binary where hash collisions are ignored, False - output is
    *                    treated as continuous where collisions add their value to the ndarray position.
    */
   public HashingVectorizer(int numFeatures, boolean isBinary) {
      super(new HashingEncoder(numFeatures));
      this.isBinary = isBinary;
   }


   @Override
   public String toString() {
      return "HashingVectorizer{" +
            "input='" + input + '\'' +
            ", output='" + output + '\'' +
            ", isBinary=" + isBinary +
            ", numFeatures=" + encoder.size() +
            '}';
   }

   @Override
   protected void encodeVariableInto(Variable v, NumericNDArray ndArray) {
      int index = encoder.encode(v.getName());
      if (index >= 0) {
         if (isBinary) {
            ndArray.set(index, 1.0);
         } else {
            ndArray.set(index, ndArray.getDouble(index) + v.getValue());
         }
      }
   }

}//END OF HashingVectorizer
