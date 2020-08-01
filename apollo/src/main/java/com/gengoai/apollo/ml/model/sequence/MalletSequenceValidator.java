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
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.regex.Pattern;

/**
 * Specialized {@link SequenceValidator} for Mallet-based sequence labelers
 */
@Value
@AllArgsConstructor
public class MalletSequenceValidator implements SequenceValidator {
   public static final MalletSequenceValidator BASIC_IOB_VALIDATOR = new MalletSequenceValidator(null, "O,I-*");
   Pattern allowed;
   Pattern forbidden;

   /**
    * Instantiates a new MalletSequenceValidator.
    *
    * @param allowed   the regular expression defining the allowed pattern
    * @param forbidden the regular expression defining the forbidden pattern
    */
   public MalletSequenceValidator(String allowed, String forbidden) {
      this(allowed == null
           ? null
           : Pattern.compile(allowed, Pattern.CASE_INSENSITIVE),
           forbidden == null
           ? null
           : Pattern.compile(forbidden, Pattern.CASE_INSENSITIVE));
   }

   @Override
   public boolean isValid(String currentLabel, String previousLabel, Observation instance) {
      throw new UnsupportedOperationException();
   }

}//END OF MalletSequenceValidator
