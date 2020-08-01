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

import cc.mallet.fst.*;
import cc.mallet.optimize.LimitedMemoryBFGS;
import cc.mallet.optimize.Optimizable;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.types.*;
import cc.mallet.util.MalletLogger;
import com.gengoai.ParameterDef;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.model.SingleSourceFitParameters;
import com.gengoai.apollo.ml.model.SingleSourceModel;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.gengoai.apollo.ml.model.sequence.Order.FIRST;
import static com.gengoai.collection.Arrays2.arrayOfInt;
import static com.gengoai.function.Functional.with;

/**
 * <p>A wrapper around Mallet's CRF implementation</p>
 *
 * @author David B. Bracewell
 */
public class MalletCrf extends SingleSourceModel<MalletCrf.Parameters, MalletCrf> {
   private static final long serialVersionUID = 1L;
   public static final ParameterDef<Boolean> FULLY_CONNECTED = ParameterDef.boolParam("fullyConnected");
   public static final ParameterDef<Order> ORDER = ParameterDef.param("order", Order.class);
   public static final ParameterDef<String> START_STATE = ParameterDef.strParam("startState");
   public static final ParameterDef<Integer> THREADS = ParameterDef.intParam("numThreads");
   private SerialPipes pipes;
   private CRF model;
   private String startState;

   /**
    * Instantiates a new MalletCrf with default parameters.
    */
   public MalletCrf() {
      super(new Parameters());
   }

   /**
    * Instantiates a new MalletCrf with the given parameters.
    *
    * @param parameters the parameters
    */
   public MalletCrf(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new MalletCrf with the given parameter updater.
    *
    * @param updater the updater
    */
   public MalletCrf(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   @Override
   public void estimate(DataSet preprocessed) {
      if(parameters.verbose.value()) {
         MalletLogger.getLogger(ThreadedOptimizable.class.getName())
                     .setLevel(Level.INFO);
         MalletLogger.getLogger(CRFTrainerByValueGradients.class.getName())
                     .setLevel(Level.INFO);
         MalletLogger.getLogger(CRF.class.getName())
                     .setLevel(Level.INFO);
         MalletLogger.getLogger(CRFOptimizableByBatchLabelLikelihood.class.getName())
                     .setLevel(Level.INFO);
         MalletLogger.getLogger(LimitedMemoryBFGS.class.getName())
                     .setLevel(Level.INFO);
      } else {
         MalletLogger.getLogger(ThreadedOptimizable.class.getName())
                     .setLevel(Level.OFF);
         MalletLogger.getLogger(CRFTrainerByValueGradients.class.getName())
                     .setLevel(Level.OFF);
         MalletLogger.getLogger(CRF.class.getName())
                     .setLevel(Level.OFF);
         MalletLogger.getLogger(CRFOptimizableByBatchLabelLikelihood.class.getName())
                     .setLevel(Level.OFF);
         MalletLogger.getLogger(LimitedMemoryBFGS.class.getName())
                     .setLevel(Level.OFF);

      }

      Alphabet dataAlphabet = new Alphabet();
      pipes = new SerialPipes(Arrays.asList(new SequenceToTokenSequence(),
                                            new TokenSequence2FeatureVectorSequence(dataAlphabet, false, true)));
      pipes.setDataAlphabet(dataAlphabet);
      pipes.setTargetAlphabet(new LabelAlphabet());

      InstanceList trainingData = new InstanceList(pipes);
      for(Datum datum : preprocessed) {
         com.gengoai.apollo.ml.observation.Sequence<?> x = datum.get(parameters.input.value()).asSequence();
         com.gengoai.apollo.ml.observation.Sequence<?> y = datum.get(parameters.output.value()).asSequence();
         Label[] target = new Label[x.size()];
         LabelAlphabet labelAlphabet = Cast.as(trainingData.getTargetAlphabet());
         for(int j = 0; j < target.length; j++) {
            target[j] = labelAlphabet.lookupLabel(y.get(j).asVariable().getName(), true);
         }
         trainingData.addThruPipe(new Instance(x, new LabelSequence(target), null, null));
      }
      model = new CRF(pipes, null);
      int[] order = {};
      switch(parameters.order.value()) {
         case FIRST:
            order = arrayOfInt(1);
            break;
         case SECOND:
            order = arrayOfInt(1, 2);
            break;
         case THIRD:
            order = arrayOfInt(1, 2, 3);
            break;
      }

      MalletSequenceValidator sv = Cast.as(parameters.validator.value() instanceof MalletSequenceValidator
                                           ? parameters.validator.value()
                                           : null);
      Pattern allowed = sv == null
                        ? null
                        : sv.getAllowed();
      Pattern forbidden = sv == null
                          ? null
                          : sv.getForbidden();
      model.addOrderNStates(trainingData,
                            order,
                            null,
                            parameters.startState.value(),
                            forbidden,
                            allowed,
                            parameters.fullyConnected.value());
      this.startState = parameters.startState.value();
      model.setWeightsDimensionAsIn(trainingData, false);

      CRFOptimizableByBatchLabelLikelihood batchOptLabel = new CRFOptimizableByBatchLabelLikelihood(model,
                                                                                                    trainingData,
                                                                                                    parameters.numberOfThreads
                                                                                                          .value());
      ThreadedOptimizable optLabel = new ThreadedOptimizable(batchOptLabel,
                                                             trainingData,
                                                             model.getParameters().getNumFactors(),
                                                             new CRFCacheStaleIndicator(model));
      Optimizable.ByGradientValue[] opts = {optLabel};
      CRFTrainerByValueGradients crfTrainer = new CRFTrainerByValueGradients(model, opts);
      crfTrainer.setMaxResets(0);
      crfTrainer.train(trainingData, parameters.maxIterations.value());
      optLabel.shutdown();
   }

   @Override
   public Parameters getFitParameters() {
      return new Parameters();
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if(parameters.output.value().equals(name)) {
         return LabelType.Sequence;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      int length = observation.asSequence().size();
      Sequence<?> sequence = Cast.as(model.getInputPipe()
                                          .instanceFrom(new Instance(observation, null, null, null)).getData());
      Sequence<?> bestOutput = model.transduce(sequence);
      SumLattice lattice = new SumLatticeDefault(model, sequence, true);
      Transducer.State sj = model.getState(startState);
      VariableSequence labeling = new VariableSequence();
      for(int i = 0; i < sequence.size(); i++) {
         Transducer.State si = model.getState((String) bestOutput.get(i));
         String label = (String) bestOutput.get(i);
         double pS = lattice.getGammaProbability(i, si);
         double PSjSi = lattice.getXiProbability(i, sj, si);
         double score = Math.max(pS, PSjSi);
         sj = si;
         labeling.add(Variable.real(label, score));
      }
      return labeling;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      data.updateMetadata(parameters.output.value(), m -> {
         m.setEncoder(null);
         m.setType(VariableSequence.class);
         m.setDimension(-1);
      });
   }

   /**
    * MalletCrf Fit Parameters
    */
   public static class Parameters extends SingleSourceFitParameters<Parameters> {
      private static final long serialVersionUID = 1L;
      /**
       * The sequence validator to user during inference (default {@link SequenceValidator#ALWAYS_TRUE})
       */
      public final Parameter<SequenceValidator> validator = parameter(Params.Sequence.validator,
                                                                      SequenceValidator.ALWAYS_TRUE);
      /**
       * The number of threads to use for training (default 20)
       */
      public final Parameter<Integer> numberOfThreads = parameter(THREADS, 20);
      /**
       * The order of the CRF (default {@link Order#FIRST})
       */
      public final Parameter<Order> order = parameter(ORDER, FIRST);
      /**
       * The maximum number of iterations to run for (default 250).
       */
      public final Parameter<Integer> maxIterations = parameter(Params.Optimizable.maxIterations, 250);
      /**
       * Parameter denoting whether the generated graphical model is fully connected (default true).
       */
      public final Parameter<Boolean> fullyConnected = parameter(FULLY_CONNECTED, true);
      /**
       * Parameter denoting the start state (default O).
       */
      public final Parameter<String> startState = parameter(START_STATE, "O");
   }
}//END OF MalletCRF
