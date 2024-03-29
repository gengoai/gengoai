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

package com.gengoai.hermes.ml.model;

import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.evaluation.Evaluation;
import com.gengoai.apollo.model.Model;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.IOB;
import com.gengoai.hermes.ml.CoNLLEvaluation;
import lombok.Getter;
import lombok.NonNull;

/**
 * <p>
 * Creates annotations based on the IOB tag output of an underlying model.
 * </p>
 *
 * @author David B. Bracewell
 */
public class IOBTagger extends BaseHStringMLModel {
   private static final long serialVersionUID = 1L;
   /**
    * The Annotation type.
    */
   @Getter
   final AnnotationType annotationType;

   /**
    * Instantiates a new IOBTagger
    *
    * @param inputGenerator the generator to convert HString into input for the model
    * @param annotationType the type of annotation to add during tagging.
    * @param labeler        the model to use to perform the IOB tagging
    */
   public IOBTagger(@NonNull HStringDataSetGenerator inputGenerator,
                    @NonNull AnnotationType annotationType,
                    @NonNull Model labeler) {
      super(labeler, inputGenerator);
      this.annotationType = annotationType;
   }

   @Override
   public HString apply(@NonNull HString hString) {
      for (Annotation sentence : hString.sentences()) {
         onEstimate(sentence, transform(getDataGenerator().apply(sentence)));
      }
      return hString;
   }

   @Override
   public Evaluation getEvaluator() {
      return new CoNLLEvaluation(getOutput());
   }

   @Override
   protected void onEstimate(HString sentence, Datum datum) {
      IOB.decode(sentence, datum.get(getOutput()).asSequence(), annotationType);
   }

}// END OF IOBTagger
