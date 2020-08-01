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

package com.gengoai.apollo.ml.evaluation;

import com.gengoai.LogUtils;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Defines common methods and metrics when evaluating classification models.</p>
 *
 * @author David B. Bracewell
 */
@Log
public abstract class ClassifierEvaluation implements Evaluation, Serializable {
   private static final long serialVersionUID = 1L;
   protected final String outputName;

   protected ClassifierEvaluation(String outputName) {
      this.outputName = outputName;
   }

   /**
    * <p>Calculates the accuracy, which is the percentage of items correctly classified.</p>
    *
    * @return the accuracy
    */
   public abstract double accuracy();

   /**
    * <p>Calculate the diagnostic odds ratio which is <code> positive likelihood ration / negative likelihood
    * ratio</code>. The diagnostic odds ratio is taken from the medical field and measures the effectiveness of a
    * medical tests. The measure works for binary classifications and provides the odds of being classified true when
    * the correct classification is false.</p>
    *
    * @return the diagnostic odds ratio
    */
   public double diagnosticOddsRatio() {
      return positiveLikelihoodRatio() / negativeLikelihoodRatio();
   }

   /**
    * Adds an evaluation entry.
    *
    * @param gold      the gold value
    * @param predicted the predicted value
    */
   public abstract void entry(double gold, @NonNull NDArray predicted);

   /**
    * Calculates the false negative rate, which is calculated as <code>False Positives / (True Positives + False
    * Positives)</code>
    *
    * @return the false negative rate
    */
   public double falseNegativeRate() {
      double fn = falseNegatives();
      double tp = truePositives();
      if(tp + fn == 0) {
         return 0.0;
      }
      return fn / (fn + tp);
   }

   /**
    * Calculates the number of false negatives
    *
    * @return the number of false negatives
    */
   public abstract double falseNegatives();

   /**
    * Calculates the false omission rate (or Negative Predictive Value), which is calculated as <code>False Negatives /
    * (False Negatives + True Negatives)</code>
    *
    * @return the false omission rate
    */
   public double falseOmissionRate() {
      double fn = falseNegatives();
      double tn = trueNegatives();
      if(tn + fn == 0) {
         return 0.0;
      }
      return fn / (fn + tn);
   }

   /**
    * Calculates the false positive rate which is calculated as <code>False Positives / (True Negatives + False
    * Positives)</code>
    *
    * @return the false positive rate
    */
   public double falsePositiveRate() {
      double tn = trueNegatives();
      double fp = falsePositives();
      if(tn + fp == 0) {
         return 0.0;
      }
      return fp / (tn + fp);
   }

   /**
    * Calculates the number of false positives
    *
    * @return the number of false positives
    */
   public abstract double falsePositives();

   protected int getIntegerLabelFor(Observation output, DataSet dataset) {
      if(output.isNDArray() || output.isClassification()) {
         NDArray y = output.asNDArray();
         if(y.shape().isScalar()) {
            return (int) y.get(0);
         } else {
            return (int) y.argmax();
         }
      }
      if(output.isVariable()) {
         return dataset.getMetadata(outputName).getEncoder().encode(output.asVariable().getName());
      }
      throw new IllegalArgumentException("Unable to process output of type: " + output.getClass());
   }

   /**
    * Merge this evaluation with another combining the results.
    *
    * @param evaluation the other evaluation to combine
    */
   public abstract void merge(ClassifierEvaluation evaluation);

   /**
    * Calculates the negative likelihood ratio, which is <code>False Positive Rate / Specificity</code>
    *
    * @return the negative likelihood ratio
    */
   public double negativeLikelihoodRatio() {
      return falseNegativeRate() / specificity();
   }

   /**
    * Proportion of negative results that are true negative.
    *
    * @return the double
    */
   public double negativePredictiveValue() {
      double tn = trueNegatives();
      double fn = falseNegatives();
      if(tn + fn == 0) {
         return 0;
      }
      return tn / (tn + fn);
   }

   @Override
   public void output(@NonNull PrintStream printStream) {
      output(printStream, false);
   }

   /**
    * Outputs the results of the classification to the given <code>PrintStream</code>
    *
    * @param printStream          the print stream to write to
    * @param printConfusionMatrix True print the confusion matrix, False do not print the confusion matrix.
    */
   public abstract void output(@NonNull PrintStream printStream, boolean printConfusionMatrix);

   /**
    * Outputs the evaluation results to standard out.
    *
    * @param printConfusionMatrix True print the confusion matrix, False do not print the confusion matrix.
    */
   public void output(boolean printConfusionMatrix) {
      output(System.out, printConfusionMatrix);
   }

   /**
    * Outputs the evaluation results to standard out.
    */
   public void output() {
      output(System.out, false);
   }

   /**
    * Calculates the positive likelihood ratio, which is <code>True Positive Rate / False Positive Rate</code>
    *
    * @return the positive likelihood ratio
    */
   public double positiveLikelihoodRatio() {
      return truePositiveRate() / falsePositiveRate();
   }

   /**
    * Outputs the results of the classification to the given logger.
    *
    * @param logger               the logger to use for logging
    * @param logLevel             The level to log at
    * @param printConfusionMatrix True print the confusion matrix, False do not print the confusion matrix.
    */
   public void report(@NonNull Logger logger, @NonNull Level logLevel, boolean printConfusionMatrix) {
      Resource r = new StringResource();
      try(PrintStream ps = new PrintStream(r.outputStream())) {
         output(ps, printConfusionMatrix);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      try {
         LogUtils.log(logger, logLevel, r.readToString());
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Calculates the sensitivity (same as the micro-averaged recall)
    *
    * @return the sensitivity
    */
   public double sensitivity() {
      double tp = truePositives();
      double fn = falseNegatives();
      if(tp + fn == 0) {
         return 0.0;
      }
      return tp / (tp + fn);
   }

   /**
    * Calculates the specificity, which is <code>True Negatives / (True Negatives + False Positives)</code>
    *
    * @return the specificity
    */
   public double specificity() {
      double tn = trueNegatives();
      double fp = falsePositives();
      if(tn + fp == 0) {
         return 1.0;
      }
      return tn / (tn + fp);
   }

   /**
    * Calculates the true negative rate (or specificity)
    *
    * @return the true negative rate
    */
   public double trueNegativeRate() {
      return specificity();
   }

   /**
    * Counts the number of true negatives
    *
    * @return the number of true negatives
    */
   public abstract double trueNegatives();

   /**
    * Calculates the true positive rate (same as micro recall).
    *
    * @return the true positive rate
    */
   public double truePositiveRate() {
      return sensitivity();
   }

   /**
    * Calculates the number of true positives.
    *
    * @return the number of true positive
    */
   public abstract double truePositives();

}//END OF ClassifierEvaluation
