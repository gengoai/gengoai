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

package com.gengoai.apollo.ml.model;

import com.gengoai.collection.Sets;
import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * <p>A {@link Model} that can combine its output into a single observation as follow:</p>
 * <b>Not combined</b>
 * <p>Each input represents a single example when building the model and during transformation each input is feed
 * through the model generating its own output.</p>
 * <pre>
 *  Input 1 ---|      |-----> Input 1 + outputSuffix
 *  Input 2 ---+>Model+-----> Input 2 + outputSuffix
 *  Input 3 ---|      |-----> Input 3 + outputSuffix
 * </pre>
 * <b>Combined</b>
 * <p>Each input represents a single example when building the model, but during transformation inputs are merged and
 * feed through the model generating one output.</p>
 * <pre>
 *  Input 1 ---|
 *  Input 2 ---+>Model-----> output
 *  Input 3 ---|
 * </pre>
 *
 * @author David B. Bracewell
 */
public interface CombinableOutputModel<F extends CombinableOutputFitParameters<F>, T extends CombinableOutputModel<F, T>> extends Model {

   /**
    * Sets whether or not the model produces one output from merging the inputs or multiple outputs by passing each
    * input through the model.
    *
    * @param combineOutput true - produce single output, false - produce multiple outputs.
    * @return this model
    */
   default T combineOutput(boolean combineOutput) {
      getFitParameters().combineOutputs.set(combineOutput);
      return Cast.as(this);
   }

   @Override
   F getFitParameters();

   @Override
   default LabelType getLabelType(@NonNull String name) {
      if(getFitParameters().combineOutputs.value() && getFitParameters().output.value().equals(name)) {
         return getOutputType();
      } else if(!getFitParameters().combineOutputs.value()) {
         if(getInputs().stream()
                       .anyMatch(i -> name.equals(i + getFitParameters().outputSuffix.value()))) {
            return getOutputType();
         }
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   LabelType getOutputType();

   @Override
   default Set<String> getOutputs() {
      if(getFitParameters().combineOutputs.value()) {
         return Collections.singleton(getFitParameters().output.value());
      }
      return Sets.transform(getInputs(),
                            s -> s + Strings.nullToEmpty(getFitParameters().outputSuffix.value()));
   }

   /**
    * Sets the name of the output source when outputs are combined
    *
    * @param output the output source name
    * @return this model
    */
   default T output(@NonNull String output) {
      getFitParameters().output.set(output);
      return Cast.as(this);
   }

   /**
    * Sets the output suffix to append to the input names when outputs are not combined
    *
    * @param outputSuffix the output suffix
    * @return this model
    */
   default T outputSuffix(@NonNull String outputSuffix) {
      getFitParameters().outputSuffix.set(outputSuffix);
      return Cast.as(this);
   }

}//END OF CombinableOutputModel
