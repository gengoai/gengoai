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

package com.gengoai.hermes.ml;

import com.gengoai.apollo.model.sequence.SequenceValidator;
import com.gengoai.apollo.data.observation.Observation;

/**
 * <p>Sequence validator ensuring correct IOB tag output</p>
 *
 * @author David B. Bracewell
 */
public class IOBValidator implements SequenceValidator {
   private static final long serialVersionUID = 1L;
   public static final IOBValidator INSTANCE = new IOBValidator();

   @Override
   public boolean isValid(String label, String previousLabel, Observation instance) {
      if(label.startsWith("I-")) {
         if(previousLabel == null) {
            return false;
         }
         if(previousLabel.startsWith("O")) {
            return false;
         }
         if(previousLabel.startsWith("I-") && !label.equals(previousLabel)) {
            return false;
         }
         return !previousLabel.startsWith("B-") || label.substring(2).equals(previousLabel.substring(2));
      }
      return true;
   }
}// END OF IOBValidator
