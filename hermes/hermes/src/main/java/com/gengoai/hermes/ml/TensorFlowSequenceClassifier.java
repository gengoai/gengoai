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

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.TFVarSpec;
import com.gengoai.apollo.ml.model.TensorFlowModel;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.HString;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class TensorFlowSequenceClassifier<T> extends TensorFlowModel implements HStringMLModel {
   private static final long serialVersionUID = 1L;
   private final AttributeType<T> attributeType;
   private final LabelType labelType;

   public TensorFlowSequenceClassifier(@NonNull Map<String, TFVarSpec> inputs,
                                       @NonNull LinkedHashMap<String, TFVarSpec> outputs,
                                       AttributeType<T> attributeType,
                                       LabelType labelType) {
      super(inputs, outputs);
      this.attributeType = attributeType;
      this.labelType = labelType;
   }

   @Override
   public HString apply(HString hString) {
      DataSet dataSet = getDataGenerator().generate(Collections.singleton(hString));
      putAttribute(processBatch(dataSet).get(0).get(getOutput()), hString);
      return hString;
   }

   protected abstract void putAttribute(Observation observation, HString hString);


   @Override
   public LabelType getLabelType(@NonNull String name) {
      if (name.equals(getOutput())) {
         return labelType;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   @Override
   public Model delegate() {
      return this;
   }


}
