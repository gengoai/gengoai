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

import com.gengoai.LogUtils;
import com.gengoai.MultithreadedStopwatch;
import com.gengoai.ParameterDef;
import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.math.Math2;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.gengoai.function.Functional.with;

/**
 * <p>Naive Bayes model specifically designed for text classification problems. Allows for training the following type
 * of models:</p>
 * <ul>
 * <li>Multinomial</li>
 * <li>Bernoulli</li>
 * <li>Complementary</li>
 * </ul>
 *
 * @author David B. Bracewell
 */
@Log
public class NaiveBayes extends SingleSourceModel<NaiveBayes.Parameters, NaiveBayes> {
   private static final long serialVersionUID = 1L;
   public static final ParameterDef<ModelType> modelTypeParam = ParameterDef.param("modelType", ModelType.class);
   /**
    * The conditional probabilities
    */
   protected double[][] conditionals;
   /**
    * The prior probabilities
    */
   protected double[] priors;
   /**
    * The model type
    */
   protected ModelType modelType;

   /**
    * Instantiates a new LibLinear
    */
   public NaiveBayes() {
      this(new Parameters());
   }

   /**
    * Instantiates a new LibLinear model
    *
    * @param parameters the model parameters
    */
   public NaiveBayes(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new LibLinear model
    *
    * @param updater the model parameter updater
    */
   public NaiveBayes(@NonNull Consumer<Parameters> updater) {
      this(with(new Parameters(), updater));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      dataset = dataset.cache();
      Validation.checkArgument(dataset.size() > 0, "Must have at least 1 training example.");
      int numFeatures = (int) dataset.getMetadata(parameters.input.value()).getDimension();
      int numLabels = (int) dataset.getMetadata(parameters.output.value()).getDimension();

      final Level old = log.getLevel();
      if(!parameters.verbose.value()) {
         log.setLevel(Level.OFF);
      }

      MultithreadedStopwatch sw = new MultithreadedStopwatch(getClass().getName());
      sw.start();
      LogUtils.logInfo(log,
                       "Beginning training of Naive Bayes Classifier over {2} examples with {0} features and {1} labels.",
                       numFeatures,
                       numLabels,
                       dataset.size());

      conditionals = new double[numFeatures][numLabels];
      priors = new double[numLabels];
      modelType = parameters.modelType.value();

      double[] totalTargetFeatureValues = new double[numLabels];

      double N = 0;
      for(Datum instance : dataset) {
         N++;
         NDArray vector = instance.get(parameters.input.value()).asNDArray();
         NDArray label = instance.get(parameters.output.value()).asNDArray();
         int ci;
         if(label.shape().isScalar()) {
            ci = (int) label.get(0);
         } else {
            ci = (int) label.argmax();
         }
         priors[ci]++;
         vector.forEachSparse((index, value) -> {
            totalTargetFeatureValues[ci] += value;
            conditionals[(int) index][ci] += modelType.convertValue(value);
         });

      }

      for(int featureIndex = 0; featureIndex < conditionals.length; featureIndex++) {
         double[] tmp = Arrays.copyOf(conditionals[featureIndex], conditionals[featureIndex].length);
         for(int labelIndex = 0; labelIndex < priors.length; labelIndex++) {
            if(modelType == ModelType.Complementary) {
               double nCi = 0;
               double nC = 0;
               for(int j = 0; j < priors.length; j++) {
                  if(j != labelIndex) {
                     nCi += tmp[j];
                     nC += totalTargetFeatureValues[j];
                  }
               }
               conditionals[featureIndex][labelIndex] = Math2.safeLog(modelType.normalize(nCi,
                                                                                          priors[labelIndex],
                                                                                          nC,
                                                                                          numFeatures));
            } else {
               conditionals[featureIndex][labelIndex] = Math2.safeLog(modelType.normalize(conditionals[featureIndex][labelIndex],
                                                                                          priors[labelIndex],
                                                                                          totalTargetFeatureValues[labelIndex],
                                                                                          numFeatures));
            }
         }

      }

      for(int i = 0; i < priors.length; i++) {
         priors[i] = Math2.safeLog(priors[i] / N);
      }

      sw.stop();
      LogUtils.logInfo(log, "Completed training in {0}", sw);
      log.setLevel(old);
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if(name.equals(parameters.output.value())) {
         return LabelType.classificationType(priors.length);
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this ");
   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      return modelType.distribution(observation.asNDArray(), priors, conditionals);
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      data.updateMetadata(parameters.output.value(), m -> {
         m.setType(NDArray.class);
         m.setDimension(priors.length);
      });
   }

   /**
    * Three types of Naive Bayes  are supported each of which have their own potential pros and cons and may work better
    * or worse for different types of data.
    */
   public enum ModelType {
      /**
       * Multinomial Naive Bayes using Laplace Smoothing
       */
      Multinomial,
      /**
       * Bernoulli Naive Bayes where each feature is treated as being binary
       */
      Bernoulli {
         @Override
         double convertValue(double value) {
            return value > 0
                   ? 1.0
                   : 0.0;
         }

         @Override
         double normalize(double conditionalCount, double labelCount, double totalLabelFeatureValue, double V) {
            return (conditionalCount + 1) / (labelCount + 2);
         }

         @Override
         NDArray distribution(NDArray instance, double[] priors, double[][] conditionals) {
            NDArray distribution = NDArrayFactory.ND.columnVector(priors);
            for(int i = 0; i < priors.length; i++) {
               for(int f = 0; f < conditionals.length; f++) {
                  double value = distribution.get(i);
                  if(instance.get(f) != 0) {
                     distribution.set(i, value + Math2.safeLog(conditionals[f][i]));
                  } else {
                     distribution.set(i, value + Math2.safeLog(1 - conditionals[f][i]));
                  }
               }
            }
            distribution.mapi(FastMath::exp);
            return distribution;
         }
      },
      /**
       * Complementary Naive Bayes which works similarly to the Multinomial version, but is trained differently to
       * better handle label imbalance.
       */
      Complementary;

      /**
       * Converts a features value.
       *
       * @param value the value
       * @return the converted value
       */
      double convertValue(double value) {
         return value;
      }

      /**
       * Calculates a distribution of probabilities over the labels given a vector instance and the priors and
       * conditionals.
       *
       * @param instance     the instance to calculate the distribution for
       * @param priors       the label priors
       * @param conditionals the feature-label conditional probabilities
       * @return the distribution as an array
       */
      NDArray distribution(NDArray instance, double[] priors, double[][] conditionals) {
         NDArray distribution = NDArrayFactory.ND.columnVector(priors);
         instance.forEachSparse((index, value) -> {
            for(int i = 0; i < priors.length; i++) {
               distribution.set(i, distribution.get(i) + value * conditionals[(int) index][i]);
            }

         });
         return distribution.mapi(Math::exp);
      }

      /**
       * Normalizes (smooths) the conditional probability given the conditional count, prior count, total label count,
       * and vocabulary size.
       *
       * @param conditionalCount       the conditional count
       * @param labelCount             the prior count
       * @param totalLabelFeatureValue the total label count
       * @param V                      the vocabulary size
       * @return the normalized  (smoothed) conditional probability
       */
      double normalize(double conditionalCount, double labelCount, double totalLabelFeatureValue, double V) {
         return (conditionalCount + 1) / (totalLabelFeatureValue + V);
      }

   }

   /**
    * Custom {@link FitParameters} for Naive Bayes.
    */
   public static class Parameters extends SingleSourceFitParameters<Parameters> {
      /**
       * The type of Naive Bayes model to train.
       */
      public final Parameter<ModelType> modelType = parameter(modelTypeParam, ModelType.Bernoulli);

   }

}//END OF NaiveBayes
