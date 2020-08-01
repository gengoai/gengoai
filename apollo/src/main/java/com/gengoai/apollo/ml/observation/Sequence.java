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

package com.gengoai.apollo.ml.observation;

import com.gengoai.Validation;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * <p>
 * An {@link Observation} composed of an ordered sequence of other observations represented as a List. The two main
 * types are:
 * </p>
 * <p><ul>
 * <li>{@link VariableSequence} - A sequence of {@link Variable} observations.</li>
 * <li>{@link VariableCollectionSequence} - A sequence of {@link VariableCollection} observations.</li>
 * </ul></p>
 * <p>
 * The {@link VariableSequence} implementation works well for raw sequence input or sequence label output, but care must
 * be taken when filtering Variables as null values will replace the filtered values. {@link
 * VariableCollectionSequence}*s are used when multiple variables are possible at each timestamp in the sequence.
 * </p>
 *
 * @param <T> the type of observation this is a sequence over
 * @author David B. Bracewell
 */
public interface Sequence<T extends Observation> extends List<T>, Observation {

   /**
    * Merge variable collection sequence.
    *
    * @param sequences the sequences  to merge
    * @return the sequence
    */
   static VariableCollectionSequence merge(@NonNull List<? extends Sequence<?>> sequences) {
      return merge(sequences, VariableNameSpace.Full);
   }

   /**
    * Merge variable collection sequence.
    *
    * @param sequences the sequences to merge
    * @param nameSpace the {@link VariableNameSpace} to use when merging.
    * @return the sequence
    */
   static VariableCollectionSequence merge(@NonNull List<? extends Sequence<?>> sequences,
                                           @NonNull VariableNameSpace nameSpace) {
      if(sequences.isEmpty()) {
         return new VariableCollectionSequence();
      }
      Validation.checkArgument(sequences.stream().mapToInt(Sequence::size).distinct().count() == 1,
                               "Cannot merge sequences of different lengths");
      VariableCollectionSequence out = new VariableCollectionSequence();
      for(int i = 0; i < sequences.get(0).size(); i++) {
         final int index = i;
         out.add(VariableCollection.mergeVariableSpace(sequences.stream().map(s -> s.get(index)),
                                                       nameSpace));
      }
      return out;
   }

   @Override
   default Sequence<? extends Observation> asSequence() {
      return this;
   }

   @Override
   default Stream<Variable> getVariableSpace() {
      return stream().flatMap(Observation::getVariableSpace);
   }

   @Override
   default boolean isSequence() {
      return true;
   }

}//END OF Sequence
