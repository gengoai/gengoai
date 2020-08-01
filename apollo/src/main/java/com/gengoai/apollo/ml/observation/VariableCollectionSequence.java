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

import com.gengoai.apollo.ml.feature.ContextualIterator;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p>A {@link Sequence} of {@link VariableCollection} observations. A VariableCollectionSequence is used when multiple
 * variables are possible at each timestamp in the sequence. For example, part-of-speech tagging would represent its
 * input as a VariableCollectionSequence where each timestamp (Token) is defined by one or more features, e.g surface
 * form, prefixes/suffixes, etc.</p>
 */
public class VariableCollectionSequence extends ArrayList<VariableCollection> implements Sequence<VariableCollection> {
   private static final long serialVersionUID = 1L;

   /**
    * Creates a special VariableCollection denoting the begin of sequence (or sentence), which has a label and single
    * True feature named <code>__BOS-INDEX__</code>, where <code>INDEX</code> is the offset from the <code>0</code>
    * index. This instance will return the single feature name with given prefix on all calls to {@link
    * VariableCollection#getVariableByPrefix(String)}**.
    *
    * @param distanceFromBegin the offset from the beginning of the sequence (i.e. index 0, e.g. -1)
    * @return the special beginning of sequence example at the given offset
    */
   public static VariableList BEGIN_OF_SEQUENCE(int distanceFromBegin) {
      String name = "__BOS-" + Math.abs(distanceFromBegin) + "__";
      return new VariableList(Collections.singleton(Variable.binary(name))) {
         @Override
         public Variable getVariableByPrefix(String prefix) {
            return Variable.binary(prefix, name);
         }
      };
   }

   /**
    * Creates a special VariableList denoting the end of sequence (or sentence), which has a label and single True
    * feature named <code>__EOS-INDEX+1__</code>, where <code>INDEX</code> is the offset from the size of the example in
    * the sequence. This instance will return the single feature name with given prefix on all calls to {@link
    * VariableCollection#getVariableByPrefix(String)}**.
    *
    * @param distanceFromEnd the offset from the size of the example. (e.g. if the size is 4 an offset could be 0 when
    *                        the index is at 4, 1 when the index is at 5, etc.)
    * @return the special end of sequence example at the given offset
    */
   public static VariableList END_OF_SEQUENCE(int distanceFromEnd) {
      String name = "__EOS-" + (distanceFromEnd + 1) + "__";
      return new VariableList(Collections.singleton(Variable.binary(name))) {
         @Override
         public Variable getVariableByPrefix(String prefix) {
            return Variable.binary(Strings.appendIfNotPresent(prefix, "=") + name);
         }
      };
   }

   /**
    * Instantiates a new InstanceSequence.
    */
   public VariableCollectionSequence() {

   }

   /**
    * Instantiates a new InstanceSequence.
    *
    * @param instances the instances
    */
   public VariableCollectionSequence(@NonNull Collection<? extends VariableCollection> instances) {
      super(instances);
   }

   /**
    * Instantiates a new InstanceSequence.
    *
    * @param instances the instances
    */
   public VariableCollectionSequence(@NonNull Stream<? extends VariableCollection> instances) {
      instances.forEach(this::add);
   }

   /**
    * Returns a {@link ContextualIterator} over this sequence.
    *
    * @return the contextual iterator
    */
   public ContextualIterator contextualIterator() {
      return new ContextualIterator(this);
   }

   @Override
   public Observation copy() {
      return new VariableCollectionSequence(stream().map(VariableCollection::copy));
   }

   @Override
   public void mapVariables(@NonNull Function<Variable, Variable> mapper) {
      forEach(i -> i.mapVariables(mapper));
   }

   @Override
   public void removeVariables(@NonNull Predicate<Variable> filter) {
      forEach(i -> i.removeVariables(filter));
   }

   @Override
   public void updateVariables(@NonNull Consumer<Variable> updater) {
      forEach(i -> i.updateVariables(updater));
   }
}//END OF VariableCollectionSequence
