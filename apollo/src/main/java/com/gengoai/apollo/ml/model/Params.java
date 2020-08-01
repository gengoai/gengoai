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

import com.gengoai.ParameterDef;
import com.gengoai.apollo.math.linalg.VectorComposition;
import com.gengoai.apollo.math.statistics.measure.Measure;
import com.gengoai.apollo.ml.model.clustering.Linkage;
import com.gengoai.apollo.ml.model.sequence.SequenceValidator;
import com.gengoai.apollo.ml.observation.VariableNameSpace;

import java.util.Set;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * Common parameters for machine learning algorithms
 *
 * @author David B. Bracewell
 */
public final class Params {
   /**
    * The constant combineInput.
    */
   public static final ParameterDef<Boolean> combineInput = ParameterDef.boolParam("combineInput");
   /**
    * The constant combineOutput.
    */
   public static final ParameterDef<Boolean> combineOutput = ParameterDef.boolParam("combineOutput");
   /**
    * The constant input.
    */
   public static final ParameterDef<String> input = ParameterDef.strParam("input");
   /**
    * The constant inputs.
    */
   public static final ParameterDef<Set<String>> inputs = ParameterDef.param("inputs",
                                                                             parameterizedType(Set.class,
                                                                                               String.class));
   /**
    * The constant output.
    */
   public static final ParameterDef<String> output = ParameterDef.strParam("output");
   /**
    * The constant outputSuffix.
    */
   public static final ParameterDef<String> outputSuffix = ParameterDef.strParam("outputSuffix");
   /**
    * The constant sources.
    */
   public static final ParameterDef<Set<String>> sources = ParameterDef.param("sources",
                                                                              parameterizedType(Set.class,
                                                                                                String.class));
   /**
    * True - Verbose output during training
    */
   public static final ParameterDef<Boolean> verbose = ParameterDef.boolParam("verbose");

   /**
    * The type Sequence.
    */
   public static final class Sequence {
      /**
       * The constant validator.
       */
      public static final ParameterDef<SequenceValidator> validator = ParameterDef.param("validator",
                                                                                         SequenceValidator.class);
   }

   /**
    * Common parameters for machine learning algorithms that use the Optimization package
    */
   public static final class Optimizable {
      /**
       * /** The number of examples to use in the batch for mini-batch optimization
       */
      public static final ParameterDef<Integer> batchSize = ParameterDef.intParam("batchSize");
      /**
       * True - cache data
       */
      public static final ParameterDef<Boolean> cacheData = ParameterDef.boolParam("cacheData");
      /**
       * The number of iterations to record metrics to determine convergence.
       */
      public static final ParameterDef<Integer> historySize = ParameterDef.intParam("historySize");
      /**
       * The constant l1.
       */
      public static final ParameterDef<Double> l1 = ParameterDef.doubleParam("l1");
      /**
       * The learning rate
       */
      public static final ParameterDef<Double> learningRate = ParameterDef.doubleParam("learningRate");
      /**
       * Maximum number of iterations to optimize for
       */
      public static final ParameterDef<Integer> maxIterations = ParameterDef.intParam("maxIterations");
      /**
       * The number iterations to report progress
       */
      public static final ParameterDef<Integer> reportInterval = ParameterDef.intParam("reportInterval");
      /**
       * The tolerance in change of metric from last iteration to this iteration for determining convergence.
       */
      public static final ParameterDef<Double> tolerance = ParameterDef.doubleParam("tolerance");
   }

   /**
    * The type Tree.
    */
   public static final class Tree {
      /**
       * The constant depthLimited.
       */
      public static final ParameterDef<Boolean> depthLimited = ParameterDef.boolParam("depthLimited");
      /**
       * The constant maxDepth.
       */
      public static final ParameterDef<Integer> maxDepth = ParameterDef.intParam("maxDepth");
      /**
       * The constant minInstances.
       */
      public static final ParameterDef<Integer> minInstances = ParameterDef.intParam("minInstances");
      /**
       * The constant prune.
       */
      public static final ParameterDef<Boolean> prune = ParameterDef.boolParam("prune");
   }

   /**
    * The type Clustering.
    */
   public static final class Clustering {
      /**
       * The constant K.
       */
      public static final ParameterDef<Integer> minPoints = ParameterDef.intParam("minPoints");
      /**
       * The constant K.
       */
      public static final ParameterDef<Integer> K = ParameterDef.intParam("K");
      /**
       * The constant linkage.
       */
      public static final ParameterDef<Linkage> linkage = ParameterDef.param("linkage", Linkage.class);
      /**
       * The constant measure.
       */
      public static final ParameterDef<Measure> measure = ParameterDef.param("measure", Measure.class);
   }

   /**
    * The type Embedding.
    */
   public static final class Embedding {
      /**
       * The constant aggregationFunction.
       */
      public static final ParameterDef<VectorComposition> aggregationFunction = ParameterDef.param("aggregationFunction",
                                                                                                   VectorComposition.class);
      /**
       * The constant dimension.
       */
      public static final ParameterDef<Integer> dimension = ParameterDef.intParam("dimension");
      /**
       * The constant fullFeatureName.
       */
      public static final ParameterDef<VariableNameSpace> nameSpace = ParameterDef.param("namingPattern",
                                                                                         VariableNameSpace.class);
      /**
       * The constant specialWords.
       */
      public static final ParameterDef<String[]> specialWords = ParameterDef.param("specialWords", String[].class);
      /**
       * The constant unknownWord.
       */
      public static final ParameterDef<String> unknownWord = ParameterDef.strParam("unknownWord");
      /**
       * The constant windowSize.
       */
      public static final ParameterDef<Integer> windowSize = ParameterDef.intParam("windowSize");
   }

}//END OF Params
