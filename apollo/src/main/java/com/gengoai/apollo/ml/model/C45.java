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

import cc.mallet.classify.C45Trainer;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.types.GainRatio;
import cc.mallet.util.MalletLogger;
import lombok.NonNull;

import java.util.function.Consumer;
import java.util.logging.Level;

import static com.gengoai.function.Functional.with;

/**
 * <p>
 * A classifier wrapper around Mallet's C4.5 decision tree. The C4.5 algorithm constructs a decision tree by selecting
 * the feature that best splits the dataset into sub-samples that support the labels using information gain at each node
 * in the tree. The feature with the highest normalized gain is chosen as the decision for that node.
 * </p>
 *
 * @author David B. Bracewell
 */
public class C45 extends MalletClassifier<C45.Parameters> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new C45 with default parameters.
    */
   public C45() {
      super(new Parameters());
   }

   /**
    * Instantiates a new C45 with the given parameters.
    *
    * @param parameters the parameters
    */
   public C45(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new C45 with the given parameter updater.
    *
    * @param updater the updater
    */
   public C45(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   @Override
   protected ClassifierTrainer<?> getTrainer() {
      C45Trainer trainer = new C45Trainer();
      if(parameters.verbose.value()) {
         MalletLogger.getLogger(C45Trainer.class.getName()).setLevel(Level.INFO);
         MalletLogger.getLogger(GainRatio.class.getName()).setLevel(Level.INFO);
         MalletLogger.getLogger(cc.mallet.classify.C45.class.getName()).setLevel(Level.INFO);
      } else {
         MalletLogger.getLogger(C45Trainer.class.getName()).setLevel(Level.OFF);
         MalletLogger.getLogger(GainRatio.class.getName()).setLevel(Level.OFF);
         MalletLogger.getLogger(cc.mallet.classify.C45.class.getName()).setLevel(Level.OFF);
      }
      trainer.setDepthLimited(parameters.depthLimited.value());
      trainer.setDoPruning(parameters.doPruning.value());
      trainer.setMaxDepth(parameters.maxDepth.value());
      trainer.setMinNumInsts(parameters.minInstances.value());
      return trainer;
   }

   /**
    * Fit parameters for C45
    */
   public static class Parameters extends SingleSourceFitParameters<Parameters> {
      private static final long serialVersionUID = 1L;
      /**
       * Parameter denoting whether training limits the depth of the learned tree (default false).
       */
      public final Parameter<Boolean> depthLimited = parameter(Params.Tree.depthLimited, false);
      /**
       * Parameter denoting whether the tree is pruned or not (default true).
       */
      public final Parameter<Boolean> doPruning = parameter(Params.Tree.prune, true);
      /**
       * Parameter denoting whether the maximum depth of the tree when the tree is depth limited (default 4).
       */
      public final Parameter<Integer> maxDepth = parameter(Params.Tree.maxDepth, 4);
      /**
       * The minimum number of instances for a leaf node.
       */
      public final Parameter<Integer> minInstances = parameter(Params.Tree.minInstances, 2);
   }

}//END OF C45
