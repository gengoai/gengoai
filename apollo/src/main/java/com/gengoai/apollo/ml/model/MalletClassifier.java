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

import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.encoder.MalletEncoder;
import com.gengoai.apollo.ml.observation.Classification;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.Arrays;

/**
 * <p>Abstract base class implementation of a {@link SingleSourceModel} that wraps a Mallet Classifier.</p>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public abstract class MalletClassifier<T extends SingleSourceFitParameters<T>> extends SingleSourceModel<T, MalletClassifier<T>> {
   private static final long serialVersionUID = 1L;
   protected Alphabet featureAlphabet;
   protected Alphabet labelAlphabet;
   protected cc.mallet.classify.Classifier model;

   protected MalletClassifier(@NonNull T parameters) {
      super(parameters);
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      featureAlphabet = new Alphabet();
      Pipe pipe = new SerialPipes(Arrays.asList(new Target2Label(),
                                                new VectorToTokensPipe(featureAlphabet)));
      pipe.setDataAlphabet(featureAlphabet);
      InstanceList trainingData = new InstanceList(pipe);
      for(Datum datum : dataset) {
         Observation input = datum.get(parameters.input.value());
         Observation output = datum.get(parameters.output.value());
         Validation.notNull(input, "Null Input Observation");
         Validation.notNull(output, "Null Output Observation");
         trainingData.addThruPipe(new cc.mallet.types.Instance(
               input,
               output.getVariableSpace().findFirst().map(Variable::getName).orElseThrow(),
               null,
               null
         ));
      }

      ClassifierTrainer<?> trainer = getTrainer();
      model = trainer.train(trainingData);
      labelAlphabet = model.getInstancePipe().getTargetAlphabet();
   }

   @Override
   public T getFitParameters() {
      return parameters;
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if(name.equals(parameters.output.value())) {
         return LabelType.classificationType(labelAlphabet.size());
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   protected abstract ClassifierTrainer<?> getTrainer();

   @Override
   protected Observation transform(@NonNull Observation observation) {
      Labeling labeling = model.classify(model.getInstancePipe()
                                              .instanceFrom(new cc.mallet.types.Instance(observation,
                                                                                         Strings.EMPTY,
                                                                                         null,
                                                                                         null)))
                               .getLabeling();
      double[] result = new double[labelAlphabet.size()];
      for(int i = 0; i < labelAlphabet.size(); i++) {
         result[i] = labeling.value(i);
      }
      return new Classification(NDArrayFactory.ND.rowVector(result), new MalletEncoder(labelAlphabet));
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      data.updateMetadata(parameters.output.value(), m -> {
         m.setDimension(-1);
         m.setEncoder(null);
         m.setType(Classification.class);
      });
   }

}//END OF MalletClassifier
