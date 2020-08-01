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

/**
 * <p>
 * Base class defining an <code>input</code> and <code>output</code> parameter for {@link Model}s with a a single input
 * and output source.
 * </p>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public abstract class SingleSourceFitParameters<T extends SingleSourceFitParameters<T>> extends FitParameters<T> {
   /**
    * The name of the input source
    */
   public final Parameter<String> input = parameter(Params.input, Datum.DEFAULT_INPUT);
   /**
    * The name of the output source
    */
   public final Parameter<String> output = parameter(Params.output, Datum.DEFAULT_OUTPUT);
}//END OF SingleSourceFitParameters
