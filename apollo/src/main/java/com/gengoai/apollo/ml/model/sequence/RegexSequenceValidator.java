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
import java.util.regex.Pattern;

/**
 * <p>
 * A {@link SequenceValidator} that determines if a transition is valid using a regular expression over the previous and
 * current labels. Patterns are defined using standard Java Regex.
 * </p>
 *
 * @author David B. Bracewell
 */
public class RegexSequenceValidator implements SequenceValidator, Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * A simple validator for IOB annotation schemes.
    */
   public static final SequenceValidator BASIC_IOB_VALIDATOR = new RegexSequenceValidator(true, "(O|__BOS__)", "I-.*");
   private final Pattern pattern;
   private final boolean negated;

   /**
    * Instantiates a new Regex validator.
    *
    * @param negated          True - negate the regex, False - match the regex as is
    * @param prevLabelPattern the pattern to match the previous label
    * @param curLabelPattern  the pattern to match the current label
    */
   public RegexSequenceValidator(boolean negated, String prevLabelPattern, String curLabelPattern) {
      this.pattern = Pattern.compile(prevLabelPattern + "\0" + curLabelPattern);
      this.negated = negated;
   }

   @Override
   public boolean isValid(String currentLabel, String previousLabel, Observation instance) {
      previousLabel = (previousLabel == null)
                      ? "__BOS__"
                      : previousLabel;
      String lbl = previousLabel + "\0" + currentLabel;
      boolean found = pattern.matcher(lbl).matches();
      if(negated) {
         return !found;
      }
      return found;
   }
}//END OF RegexSequenceValidator
