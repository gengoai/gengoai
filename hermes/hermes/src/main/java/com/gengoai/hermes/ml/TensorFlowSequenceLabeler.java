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

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.model.LabelType;
import com.gengoai.apollo.model.Model;
import com.gengoai.apollo.model.TFVarSpec;
import com.gengoai.apollo.model.TensorFlowModel;
import com.gengoai.apollo.model.sequence.SequenceValidator;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public abstract class TensorFlowSequenceLabeler extends TensorFlowModel implements HStringMLModel {
   private static final long serialVersionUID = 1L;
   private final TagDecoder tagDecoder;
   private final SequenceValidator validator;

   public TensorFlowSequenceLabeler(@NonNull Map<String, TFVarSpec> inputs,
                                    @NonNull LinkedHashMap<String, TFVarSpec> outputs,
                                    @NonNull SequenceValidator validator,
                                    @NonNull TagDecoder tagDecoder) {
      super(inputs, outputs);
      this.validator = validator;
      this.tagDecoder = tagDecoder;
   }

   @Override
   public HString apply(HString hString) {
      DataSet dataSet = getDataGenerator().generate(Collections.singleton(hString));
      List<Datum> tensors = processBatch(dataSet);
      for (int i = 0; i < tensors.size(); i++) {
         Annotation sentence = hString.sentences().get(i);
         tagDecoder.decode(sentence, tensors.get(i).get(getOutput()).asSequence());
      }
      return hString;
   }

   @Override
   protected Observation decodeNDArray(String name, NumericNDArray ndArray) {
      return ndArray.decodeSequence(outputs.get(name).getEncoder(), validator);
   }

   @Override
   public Model delegate() {
      return this;
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if (name.equals(getOutput())) {
         return LabelType.Sequence;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   @Override
   public void setVersion(String version) {
      throw new UnsupportedOperationException();
   }
}//END OF TensorFlowSequenceLabeler
