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

package com.gengoai.apollo.ml.model.sequence;

import com.gengoai.apollo.ml.observation.Observation;

import java.io.Serializable;

/**
 * <p>
 * Checks if the transition from the previous to current label for the given instance is valid.
 * </p>
 *
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface SequenceValidator extends Serializable {

   /**
    * A sequence validator that returns always true
    */
   SequenceValidator ALWAYS_TRUE = (SequenceValidator) (currentLabel, previousLabel, instance) -> true;

   /**
    * Checks if the transition from the previous to current label for the given instance is valid
    *
    * @param currentLabel  the current label
    * @param previousLabel the previous label
    * @param instance      the instance
    * @return True if valid, False otherwise
    */
   boolean isValid(String currentLabel, String previousLabel, Observation instance);

}//END OF Validator
