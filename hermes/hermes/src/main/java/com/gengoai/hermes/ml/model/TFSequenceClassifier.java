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
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.model.LabelType;
import com.gengoai.apollo.model.Model;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFModel;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ml.HStringMLModel;
import lombok.NonNull;

import java.util.List;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public abstract class TFSequenceClassifier<T> extends TFModel implements HStringMLModel {
   private static final long serialVersionUID = 1L;
   private final AnnotationType sequenceAnnotation;
   private final LabelType labelType;


   public TFSequenceClassifier(@NonNull List<TFInputVar> inputVars,
                               @NonNull List<TFOutputVar> outputVars,
                               @NonNull AnnotationType sequenceAnnotation,
                               @NonNull LabelType labelType) {
      super(inputVars, outputVars);
      this.labelType = labelType;
      this.sequenceAnnotation = sequenceAnnotation;
   }

   @Override
   public HString apply(@NonNull HString hString) {
      List<Annotation> sequences = hString.annotations(sequenceAnnotation);
      List<Datum> output = processBatch(getDataGenerator().generate(sequences));
      for (int i = 0; i < sequences.size(); i++) {
         putLabel(output.get(i).get(getOutput()), sequences.get(i));
      }
      return hString;
   }

   @Override
   public Model delegate() {
      return this;
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if (name.equals(getOutput())) {
         return labelType;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   protected abstract void putLabel(Observation observation, HString hString);


}//END OF TFSequenceClassifier
