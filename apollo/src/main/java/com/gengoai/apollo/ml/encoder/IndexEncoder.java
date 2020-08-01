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

package com.gengoai.apollo.ml.encoder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.collection.HashMapIndex;
import com.gengoai.collection.Index;
import com.gengoai.stream.MStream;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * A basic indexed {@link Encoder} implementation that can optionally encode out-of-vocabulary items to an index for a
 * pre-defined unknown name.
 * </p>
 */
public class IndexEncoder implements Encoder {
   private static final long serialVersionUID = 1L;
   @JsonProperty("alphabet")
   private final Index<String> alphabet = new HashMapIndex<>();
   @JsonProperty("unknown")
   private String unknownName;

   /**
    * Instantiates a new IndexEncoder
    */
   public IndexEncoder() {
      this(null);
   }

   /**
    * Instantiates a new IndexEncoder with the given unknown name (always will have index 0) which will be used when we
    * try to encode a string not in the alphabet.
    *
    * @param unknownName the unknown name
    */
   public IndexEncoder(String unknownName) {
      this.unknownName = Strings.isNullOrBlank(unknownName)
                         ? null
                         : unknownName;
   }

   @JsonCreator
   private IndexEncoder(@JsonProperty("alphabet") Iterable<String> alphabet,
                        @JsonProperty("unknown") String unknown) {
      this.alphabet.addAll(alphabet);
      this.unknownName = Strings.emptyToNull(unknown);
   }

   @Override
   public String decode(double index) {
      return alphabet.get((int) index);
   }

   @Override
   public int encode(String variableName) {
      int index = alphabet.getId(variableName);
      if(index < 0 && unknownName != null) {
         return 0;
      }
      return index;
   }

   @Override
   public void fit(@NonNull MStream<Observation> stream) {
      alphabet.clear();
      if(unknownName != null) {
         alphabet.add(unknownName);
      }
      alphabet.addAll(stream.parallel()
                            .flatMap(Observation::getVariableSpace)
                            .map(Variable::getName)
                            .distinct()
                            .collect());
   }

   @Override
   public Set<String> getAlphabet() {
      return Collections.unmodifiableSet(alphabet.itemSet());
   }

   @Override
   public boolean isFixed() {
      return false;
   }

   @Override
   public int size() {
      return alphabet.size();
   }
}//END OF IndexEncoder
