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

import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.Sequence;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.data.observation.VariableCollection;
import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.conversion.Cast;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Abstract {@link Vectorizer} implementation that handles {@link VariableCollection} and {@link Sequence} with the
 * conversion of {@link Variable} implemented by child class. The resulting NDArray have  a dimension of 1 x
 * <code>Encoder.size</code> for non-sequences and
 * <code>Sequence.length</code> x <code>Encoder.size</code>  for sequences.
 * </p>
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractVariableVectorizer<T extends AbstractVariableVectorizer<T>> extends Vectorizer<T> {

   protected AbstractVariableVectorizer() {
   }

   protected AbstractVariableVectorizer(@NonNull Encoder encoder) {
      super(encoder);
   }

   /**
    * Encodes the given variable adding the result to given NDArray
    *
    * @param v       the variable
    * @param ndArray the NDArray
    */
   protected abstract void encodeVariableInto(Variable v, NumericNDArray ndArray);

   @Override
   protected final NumericNDArray transform(Observation observation) {
      if (observation instanceof Variable) {
         NumericNDArray n = ndArrayFactory.asNumeric().zeros(1, encoder.size());
         encodeVariableInto(Cast.as(observation), n);
         return n;
      } else if (observation instanceof VariableCollection) {
         NumericNDArray n = ndArrayFactory.asNumeric().zeros(1, encoder.size());
         observation.asVariableCollection().forEach(v -> encodeVariableInto(v, n));
         return n;
      } else if (observation instanceof Sequence) {
         Sequence<? extends Observation> sequence = Cast.as(observation);
         List<NumericNDArray> rows = new ArrayList<>();
         sequence.forEach(o -> rows.add(transform(o)));
         return nd.vstack(Cast.cast(rows));
      }
      throw new IllegalArgumentException("Unsupported Observation: " + observation.getClass());
   }

}//END OF AbstractVariableVectorizer
