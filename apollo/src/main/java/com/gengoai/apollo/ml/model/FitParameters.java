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

import com.gengoai.ParamMap;

/**
 * <p>
 * Fit parameters define the parameters used when fitting a model to a dataset. Each model will have its own
 * implementation that defines the unique parameters it needs to build a model.
 * </p>
 *
 * @author David B. Bracewell
 */
public class FitParameters<T extends FitParameters<T>> extends ParamMap<T> {
   private static final long serialVersionUID = 1L;
   /**
    * Whether or not to be verbose when fitting and transforming with the model
    */
   public final Parameter<Boolean> verbose = parameter(Params.verbose, true);

}//END OF FitParameters
