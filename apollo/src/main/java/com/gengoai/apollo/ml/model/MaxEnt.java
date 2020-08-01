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
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.util.MalletLogger;
import com.gengoai.ParameterDef;
import lombok.NonNull;

import java.util.function.Consumer;
import java.util.logging.Level;

import static com.gengoai.function.Functional.with;

/**
 * <p>A wrapper around Mallet's MaxEnt classifier.</p>
 *
 * @author David B. Bracewell
 */
public class MaxEnt extends MalletClassifier<MaxEnt.Parameters> {
   public static final ParameterDef<Double> gaussianPriorVariance = ParameterDef.doubleParam("gaussianPriorVariance");

   /**
    * Instantiates a new MaxEnt with default parameters.
    */
   public MaxEnt() {
      super(new Parameters());
   }

   /**
    * Instantiates a new MaxEnt with the given parameters.
    *
    * @param parameters the parameters
    */
   public MaxEnt(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new MaxEnt with the given parameter updater.
    *
    * @param updater the updater
    */
   public MaxEnt(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   @Override
   protected ClassifierTrainer<?> getTrainer() {
      MaxEntTrainer trainer = new MaxEntTrainer();
      trainer.setNumIterations(parameters.maxIterations.value());
      trainer.setL1Weight(parameters.l1.value());
      trainer.setGaussianPriorVariance(parameters.gaussianPriorVariance.value());
      if(parameters.verbose.value()) {
         MalletLogger.getLogger(MaxEntTrainer.class.getName()).setLevel(Level.INFO);
         MalletLogger.getLogger(MaxEntTrainer.class.getName() + "-pl").setLevel(Level.INFO);
      } else {
         MalletLogger.getLogger(MaxEntTrainer.class.getName()).setLevel(Level.OFF);
         MalletLogger.getLogger(MaxEntTrainer.class.getName() + "-pl").setLevel(Level.OFF);
      }
      return trainer;
   }

   /**
    * MaxEnt Fit Parameters
    */
   public static class Parameters extends SingleSourceFitParameters<Parameters> {
      /**
       * Parameter denoting the maximum number of iterations to train the model (default Integer.MAX_VALUE);
       */
      public final Parameter<Integer> maxIterations = parameter(Params.Optimizable.maxIterations, Integer.MAX_VALUE);
      /**
       * Parameter denoting the L1 regularization parameter (default 0.0)
       */
      public final Parameter<Double> l1 = parameter(Params.Optimizable.l1, 0.0);
      /**
       * Parameter denoting the Gaussian prior variance (default 1.0)
       */
      public final Parameter<Double> gaussianPriorVariance = parameter(MaxEnt.gaussianPriorVariance, 1.0);
   }

}//END OF MaxEnt
