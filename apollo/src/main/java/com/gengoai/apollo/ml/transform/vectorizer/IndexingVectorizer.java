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
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.IndexEncoder;
import com.gengoai.apollo.ml.observation.*;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.apache.mahout.math.list.DoubleArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A {@link Vectorizer} that outputs the index (id) of the encoded values. Works with:
 * </p>
 * <p><ul>
 * <li>{@link Variable}: variables as encoded as a scalar value representing the index of the
 * variable name</li>
 * <li>{@link VariableCollection}: encoded into a sorted NDArray of indexed variable names </li>
 * <li>{@link Sequence}: encoded into a matrix where each row is an observation in the sequence
 * and the column is the index of encoded observation at the sequence timestamp</li>
 * </ul></p>
 *
 * @author David B. Bracewell
 */
public class IndexingVectorizer extends Vectorizer<IndexingVectorizer> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new IndexingVectorizer with a specified {@link Encoder}
    *
    * @param encoder the encoder
    */
   public IndexingVectorizer(@NonNull Encoder encoder) {
      super(encoder);
   }

   /**
    * Instantiates a new IndexingVectorizer with an {@link IndexEncoder}.
    */
   public IndexingVectorizer() {
      super(new IndexEncoder());
   }

   /**
    * Instantiates a new IndexingVectorizer with an {@link IndexEncoder} with the unknown name set to the given value.
    *
    * @param unknownName the unknown name
    */
   public IndexingVectorizer(String unknownName) {
      super(new IndexEncoder(unknownName));
   }

   @Override
   public NDArray transform(Observation observation) {
      if(observation instanceof Variable) {
         return transform(observation, null);
      } else if(observation instanceof VariableCollection) {
         return transform(observation, null);
      } else if(observation instanceof Sequence) {
         Sequence<? extends Observation> sequence = Cast.as(observation);
         if(sequence instanceof VariableSequence) {
            List<NDArray> vars = new ArrayList<>();
            for(Observation v : sequence) {
               vars.add(transform(v, null));
            }
            return NDArrayFactory.ND.vstack(vars);
         }
         int maxSize = sequence.stream().mapToInt(o -> (int) o.getVariableSpace().count()).max().orElse(1);
         NDArray n = ndArrayFactory.array(sequence.size(), maxSize);
         for(int i = 0; i < sequence.size(); i++) {
            NDArray o = transform(sequence.get(i), NDArrayFactory.ND.array(1, maxSize));
            n.setRow(i, o);
         }
         return n;
      }
      throw new IllegalArgumentException("Unsupported Observation: " + observation.getClass());
   }

   public NDArray transform(Observation observation, NDArray out) {
      if(observation.isVariable()) {
         int index = encoder.encode(((Variable) observation).getName());
         if(index >= 0) {
            return ndArrayFactory.scalar(index);
         }
         return ndArrayFactory.scalar(0);
      }
      VariableCollection mvo = observation.asVariableCollection();
      DoubleArrayList list = new DoubleArrayList();
      mvo.forEach(v -> {
         int index = encoder.encode(v.getName());
         if(index >= 0) {
            list.add(index);
         }
      });
      list.sort();
      if(out == null) {
         out = NDArrayFactory.ND.array(1, list.size());
      }
      for(int i = 0; i < list.size(); i++) {
         out.set(i, list.get(i));
      }
      return out;
   }
}//END OF IndexingVectorizer
