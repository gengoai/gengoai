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

package com.gengoai.apollo.ml.model.topic;

import com.gengoai.apollo.ml.model.MultiInputFitParameters;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.observation.VariableNameSpace;

/**
 * <p>Specialized {@link com.gengoai.apollo.ml.model.FitParameters} for {@link TopicModel}s.</p>
 *
 * @author David B. Bracewell
 */
public class TopicModelFitParameters extends MultiInputFitParameters<TopicModelFitParameters> {
   /**
    * Determines the naming strategy for converting Variables into words (default Full).
    */
   public final Parameter<VariableNameSpace> namingPattern = parameter(Params.Embedding.nameSpace, VariableNameSpace.Full);

}//END OF TopicModelFitParameters
