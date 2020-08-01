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

package com.gengoai.apollo.ml.feature;

import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableCollection;
import com.gengoai.apollo.ml.observation.VariableCollectionSequence;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
class ContextFeaturizerImpl<I> extends ContextFeaturizer<I> {
   private static final long serialVersionUID = 1L;
   private final String featurePrefix;
   private final boolean ignoreEmptyContext;
   private final List<FeatureGetter> patterns;

   ContextFeaturizerImpl(boolean ignoreEmptyContext, List<FeatureGetter> patterns) {
      this.featurePrefix = patterns.stream()
                                   .map(g -> g.getPrefix() + "[" + g.getOffset() + "]")
                                   .collect(Collectors.joining("|"));
      this.ignoreEmptyContext = ignoreEmptyContext;
      this.patterns = patterns;
   }

   @Override
   public VariableCollectionSequence contextualize(VariableCollectionSequence sequence) {
      for(ContextualIterator itr = sequence.contextualIterator(); itr.hasNext(); ) {
         VariableCollection instance = itr.next();
         StringBuilder fName = new StringBuilder();
         int i = 0;
         for(FeatureGetter pattern : patterns) {
            Variable f = pattern.get(itr);
            if(ignoreEmptyContext) {
               if(f.getSuffix().startsWith("__BOS-") || f.getSuffix().startsWith("__EOS-")) {
                  fName = null;
                  break;
               }
            }
            if(i > 0) {
               fName.append("|");
            }
            fName.append(f.getSuffix());
            i++;
         }
         if(fName != null) {
            instance.add(Variable.binary(featurePrefix, fName.toString()));
         }
      }
      return sequence;
   }

   @Override
   public String toString() {
      String out = featurePrefix;
      if(ignoreEmptyContext) {
         out = "~" + out;
      }
      return out;
   }

}//END OF ContextFeaturizerImpl
