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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.Copyable;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.HashMapCounter;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;

/**
 * <p>
 * An {@link Observation} that is a collection of multiple variables. Variable collections represent a collection of
 * features that make up an "Instance" in standard machine learning language.
 * </p>
 *
 * @author David B. Bracewell
 */
@JsonDeserialize(as = VariableList.class)
public interface VariableCollection extends Observation, Collection<Variable> {

   /**
    * Merges the variable space across a stream of {@link Observation} into a VariableCollection.
    *
    * @param stream the stream of Observation to merge
    * @return the VariableCollection
    */
   static VariableCollection mergeVariableSpace(@NonNull Stream<Observation> stream) {
      Counter<Tuple2<String, String>> counter = new HashMapCounter<>();
      stream.forEach(o -> {
         o.getVariableSpace()
          .forEach(v -> counter.increment($(v.getPrefix(), v.getSuffix()), v.getValue()));

      });
      VariableCollection vc = new VariableList();
      counter.forEach((k, v) -> vc.add(Variable.real(k.v1, k.v2, v)));
      return vc;
   }

   /**
    * Merges the variable space across a stream of {@link Observation} into a VariableCollection. During the merging
    * process the variables can be merged using full name (<code>includePrefix=true</code>) or with just the suffix
    * (<code>includePrefix=false</code>).
    *
    * @param stream    the stream of Observation to merge
    * @param nameSpace the {@link VariableNameSpace} to use when merging.
    * @return the VariableCollection
    */
   static VariableCollection mergeVariableSpace(@NonNull Stream<Observation> stream,
                                                @NonNull VariableNameSpace nameSpace) {
      if(nameSpace == VariableNameSpace.Full) {
         return mergeVariableSpace(stream);
      }
      Counter<String> counter = new HashMapCounter<>();
      stream.forEach(o -> {
         o.getVariableSpace()
          .forEach(v -> counter.increment(nameSpace.getName(v), v.getValue()));

      });
      VariableCollection vc = new VariableList();
      counter.forEach((k, v) -> {
         if(nameSpace == VariableNameSpace.Suffix) {
            vc.add(Variable.real(k, v));
         } else {
            vc.add(Variable.real(k, Strings.EMPTY, v));
         }
      });
      return vc;
   }

   @Override
   default VariableCollection asVariableCollection() {
      return this;
   }

   @Override
   default VariableCollection copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Gets a feature from the instance matching the prefix.
    *
    * @param prefix the prefix of the feature we are looking for
    * @return the first found feature with the given prefix or null if none.
    */
   default Variable getVariableByPrefix(String prefix) {
      return parallelStream().filter(f -> Strings.safeEquals(prefix, f.getPrefix(), true))
                             .findFirst()
                             .orElse(Variable.binary(prefix, "false"));
   }

   @Override
   default Stream<Variable> getVariableSpace() {
      return stream();
   }

   @Override
   default boolean isVariableCollection() {
      return true;
   }

   @Override
   default void removeVariables(@NonNull Predicate<Variable> filter) {
      removeIf(filter);
   }

   @Override
   default void updateVariables(@NonNull Consumer<Variable> updater) {
      forEach(updater);
   }

}//END OF VariableCollection
