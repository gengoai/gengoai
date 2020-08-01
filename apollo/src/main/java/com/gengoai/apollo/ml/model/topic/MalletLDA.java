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

package com.gengoai.apollo.ml.model.topic;

import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TargetStringToFeatures;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.gengoai.ParameterDef;
import com.gengoai.SystemInfo;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableList;
import com.gengoai.apollo.ml.observation.VariableNameSpace;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.gengoai.apollo.ml.observation.VariableCollection.mergeVariableSpace;
import static com.gengoai.function.Functional.with;

/**
 * <p>
 * Latent Dirichlet Allocation implementation using Mallet's ParallelTopicModel.
 * </p>
 *
 * @author David B. Bracewell
 */
public class MalletLDA extends TopicModel {
   private static final long serialVersionUID = 1L;
   public static final ParameterDef<Integer> burnIn = ParameterDef.intParam("burnIn");
   public static final ParameterDef<Integer> optimizationInterval = ParameterDef.intParam("optimizationInterval");
   public static final ParameterDef<Boolean> symmetricAlpha = ParameterDef.boolParam("symmetricAlpha");
   private final Parameters parameters;
   private volatile transient TopicInferencer inferencer;
   private SerialPipes pipes;
   private ParallelTopicModel topicModel;

   /**
    * Instantiates a new MalletLDA with default parameters.
    */
   public MalletLDA() {
      this(new Parameters());
   }

   /**
    * Instantiates a new MalletLDA with the given parameters.
    *
    * @param parameters the parameters
    */
   public MalletLDA(@NonNull Parameters parameters) {
      this.parameters = parameters;
   }

   /**
    * Instantiates a new MalletLDA with the given updater.
    *
    * @param updater the updater
    */
   public MalletLDA(@NonNull Consumer<Parameters> updater) {
      this.parameters = with(new Parameters(), updater);
   }

   private Topic createTopic(int topic) {
      final Alphabet alphabet = pipes.getDataAlphabet();
      final ArrayList<TreeSet<IDSorter>> topicWords = topicModel.getSortedWords();
      double[][] termScores = topicModel.getTopicWords(true, true);
      Iterator<?> iterator = topicWords.get(topic).iterator();
      IDSorter info;
      Counter<String> topicWordScores = Counters.newCounter();
      while(iterator.hasNext()) {
         info = (IDSorter) iterator.next();
         topicWordScores.set(alphabet.lookupObject(info.getID()).toString(), termScores[topic][info.getID()]);
      }
      return new Topic(topic, topicWordScores);
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      if(parameters.verbose.value()) {
         ParallelTopicModel.logger.setLevel(Level.INFO);
      } else {
         ParallelTopicModel.logger.setLevel(Level.OFF);
      }
      topics.clear();
      pipes = new SerialPipes(Arrays.asList(new TargetStringToFeatures(),
                                            new InstanceToTokenSequence(),
                                            new TokenSequence2FeatureSequence()));
      InstanceList trainingData = new InstanceList(pipes);
      for(Datum datum : dataset) {
         if(parameters.combineInputs.value()) {
            Observation input = mergeVariableSpace(datum.stream(parameters.inputs.value()),
                                                   parameters.namingPattern.value());
            trainingData.addThruPipe(new Instance(input, Strings.EMPTY, null, null));
         } else {
            for(String inputName : parameters.inputs.value()) {
               Observation input = datum.get(inputName);
               if(parameters.namingPattern.value() != VariableNameSpace.Full) {
                  input = new VariableList(input.getVariableSpace()
                                                .map(v -> Variable.real(parameters.namingPattern.value().getName(v),
                                                                        v.getValue())));
               }
               trainingData.addThruPipe(new Instance(input, Strings.EMPTY, null, null));
            }
         }
      }
      topicModel = new ParallelTopicModel(parameters.K.value());
      topicModel.addInstances(trainingData);
      topicModel.setNumIterations(parameters.maxIterations.value());
      topicModel.setNumThreads(SystemInfo.NUMBER_OF_PROCESSORS - 1);
      topicModel.setBurninPeriod(parameters.burnIn.value());
      topicModel.setOptimizeInterval(parameters.optimizationInterval.value());
      topicModel.setSymmetricAlpha(parameters.symmetricAlpha.value());
      try {
         topicModel.estimate();
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      for(int i = 0; i < parameters.K.value(); i++) {
         topics.add(createTopic(i));
      }
   }

   @Override
   public Parameters getFitParameters() {
      return parameters;
   }

   private TopicInferencer getInferencer() {
      if(inferencer == null) {
         synchronized(this) {
            if(inferencer == null) {
               TopicInferencer ti = topicModel.getInferencer();
               ti.setRandomSeed(1234);
               inferencer = ti;
            }
         }
      }
      return inferencer;
   }

   @Override
   public NDArray getTopicDistribution(String feature) {
      final Alphabet alphabet = pipes.getDataAlphabet();
      int index = alphabet.lookupIndex(feature, false);
      if(index == -1) {
         return NDArrayFactory.ND.array(topicModel.numTopics);
      }
      double[] dist = new double[topicModel.numTopics];
      double[][] termScores = topicModel.getTopicWords(true, true);
      for(int i = 0; i < topicModel.numTopics; i++) {
         dist[i] = termScores[i][index];
      }
      return NDArrayFactory.ND.rowVector(dist);
   }

   private NDArray inference(Observation observation) {
      InstanceList instances = new InstanceList(pipes);
      instances.addThruPipe(new cc.mallet.types.Instance(observation,
                                                         Strings.EMPTY,
                                                         null,
                                                         null));
      return NDArrayFactory.ND.rowVector(getInferencer().getSampledDistribution(instances.get(0),
                                                                                800,
                                                                                5,
                                                                                100));
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      if(parameters.combineOutputs.value()) {
         datum.put(parameters.output.value(), inference(mergeVariableSpace(datum.stream(parameters.inputs.value()),
                                                                           parameters.namingPattern.value())));
      } else {
         for(String inputName : parameters.inputs.value()) {
            Observation input = datum.get(inputName);
            if(parameters.namingPattern.value() != VariableNameSpace.Full) {
               input = new VariableList(input.getVariableSpace()
                                             .map(v -> Variable.real(parameters.namingPattern.value().getName(v),
                                                                     v.getValue())));
            }
            datum.put(inputName + Strings.nullToEmpty(parameters.outputSuffix.value()), inference(input));
         }
      }
      return datum;
   }

   /**
    * MalletLDA Firt Parameters
    */
   public static class Parameters extends TopicModelFitParameters {
      /**
       * The number of topics to discover (default 100).
       */
      public final Parameter<Integer> K = parameter(Params.Clustering.K, 100);
      /**
       * The number of iterations before hyperparameter optimization begins (default 500)
       */
      public final Parameter<Integer> burnIn = parameter(MalletLDA.burnIn, 500);
      /**
       * The maximum number of iterations to train the model (default 2000).
       */
      public final Parameter<Integer> maxIterations = parameter(Params.Optimizable.maxIterations, 2000);
      /**
       * The interval in iterations in which to optimize the hyperparaemters (default 10)
       */
      public final Parameter<Integer> optimizationInterval = parameter(MalletLDA.optimizationInterval, 10);
      /**
       * Determines whether a symmetric alpha is used (true) or an asymmetric alpha is used (false) (default false).
       */
      public final Parameter<Boolean> symmetricAlpha = parameter(MalletLDA.symmetricAlpha, false);
   }
}//END OF MalletLDA
