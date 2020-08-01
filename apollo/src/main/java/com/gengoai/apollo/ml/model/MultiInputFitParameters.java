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

import com.gengoai.apollo.ml.Datum;

import java.util.Collections;
import java.util.Set;

/**
 * <b>Combined Inputs and Outputs</b>
 * <p>Inputs are merged when building the model and during transformation inputs are merged and feed through the model
 * generating one output.</p>
 * <pre>
 *  Input 1 --\
 *  Input 2 ---->Model-----> output
 *  Input 3 --/
 * </pre>
 * <b>Combined Inputs only</b>
 * <p>Inputs are merged when building the model, but during transformation each input is feed through the model
 * generating its own output.</p>
 * <pre>
 *  Input 1 --\       |-----> Input 1 + outputSuffix
 *  Input 2 ---->Model+-----> Input 2 + outputSuffix
 *  Input 3 --/       |-----> Input 3 + outputSuffix
 * </pre>
 * <b>Nothing combined</b>
 * <p>Each input represents a single example when building the model and during transformation each input is feed
 * through the model generating its own output.</p>
 * <pre>
 *  Input 1 ---|      |-----> Input 1 + outputSuffix
 *  Input 2 ---+>Model+-----> Input 2 + outputSuffix
 *  Input 3 ---|      |-----> Input 3 + outputSuffix
 * </pre>
 * <b>Combined outputs</b>
 * <p>Each input represents a single example when building the model, but during transformation inputs are merged and
 * feed through the model generating one output.</p>
 * <pre>
 *  Input 1 ---|
 *  Input 2 ---+>Model-----> output
 *  Input 3 ---|
 * </pre>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public class MultiInputFitParameters<T extends MultiInputFitParameters<T>> extends CombinableOutputFitParameters<T> {
   /**
    * The set of inputs into the model. Note that if order is important use a LinkedHashSet. (default
    * Set(DEFAULT_INPUT))
    */
   public final Parameter<Set<String>> inputs = parameter(Params.inputs, Collections.singleton(Datum.DEFAULT_INPUT));
   /**
    * Parameter denoting whether inputs should be combined to fit the model (default true)
    */
   public final Parameter<Boolean> combineInputs = parameter(Params.combineInput, true);

}//END OF MultiInputFitParameters
