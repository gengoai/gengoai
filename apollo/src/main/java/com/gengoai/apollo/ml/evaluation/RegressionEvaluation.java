/*
 * (c) 2005 David B. Bracewell
 *
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
 *
 */

package com.gengoai.apollo.ml.evaluation;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.Split;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.string.TableFormatter;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.mahout.math.list.DoubleArrayList;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Evaluation for regression models.</p>
 *
 * @author David B. Bracewell
 */
@Log
public class RegressionEvaluation implements Evaluation, Serializable {
   private static final long serialVersionUID = 1L;
   private final String inputSource;
   private final String predictedSource;
   private DoubleArrayList gold = new DoubleArrayList();
   private double p = 0;
   private DoubleArrayList predicted = new DoubleArrayList();

   /**
    * Performs an n-fold cross-validation of the given {@link Model} on the given {@link DataSet}
    *
    * @param dataset         the dataset to evaluate over
    * @param regression      the regression model to evaluate
    * @param inputSource     the input source to calculate p-values
    * @param predictedSource the predicted source to for outcomes
    * @param nFolds          the number of folds
    * @return the regression evaluation
    */
   public static RegressionEvaluation crossValidation(@NonNull DataSet dataset,
                                                      @NonNull Model regression,
                                                      @NonNull String inputSource,
                                                      @NonNull String predictedSource,
                                                      int nFolds
                                                     ) {
      RegressionEvaluation evaluation = new RegressionEvaluation(inputSource, predictedSource);
      AtomicInteger foldId = new AtomicInteger(0);
      for(Split split : Split.createFolds(dataset.shuffle(), nFolds)) {
         regression.estimate(split.train);
         evaluation.evaluate(regression, split.test);
      }
      return evaluation;
   }

   /**
    * Evaluates the given Model with the given testing data.
    *
    * @param model           the model
    * @param testingData     the testing data
    * @param inputSource     the input source to calculate p-values
    * @param predictedSource the predicted source to for outcomes
    * @return the RegressionEvaluation
    */
   public static RegressionEvaluation evaluate(@NonNull Model model,
                                               @NonNull DataSet testingData,
                                               @NonNull String inputSource,
                                               @NonNull String predictedSource) {
      RegressionEvaluation evaluation = new RegressionEvaluation(inputSource, predictedSource);
      evaluation.evaluate(model, testingData);
      return evaluation;
   }

   /**
    * Instantiates a new Regression evaluation.
    *
    * @param inputSource     the input source
    * @param predictedSource the predicted source
    */
   public RegressionEvaluation(String inputSource, @NonNull String predictedSource) {
      this.inputSource = inputSource;
      this.predictedSource = predictedSource;
   }

   /**
    * Calculates the adjusted r2
    *
    * @return the adjusted r2
    */
   public double adjustedR2() {
      double r2 = r2();
      return r2 - (1.0 - r2) * p / (gold.size() - p - 1.0);
   }

   /**
    * Adds an evaluation entry.
    *
    * @param gold      the gold value
    * @param predicted the predicted value
    */
   public void entry(double gold, @NonNull NDArray predicted) {
      this.gold.add(gold);
      this.predicted.add(predicted.scalar());
   }

   /**
    * Evaluate the given model using the given dataset
    *
    * @param model   the model to evaluate
    * @param dataset the dataset to evaluate over
    */
   public void evaluate(@NonNull Model model, @NonNull DataSet dataset) {
      for(Datum ii : dataset) {
         p = Math.max(p, ii.get(inputSource).asNDArray().length());
         gold.add(ii.get(predictedSource).asNDArray().scalar());
         predicted.add(model.transform(ii).get(predictedSource).asNDArray().scalar());
      }
   }

   /**
    * Calculates the mean squared error
    *
    * @return the mean squared error
    */
   public double meanSquaredError() {
      return squaredError() / gold.size();
   }

   /**
    * Merge this evaluation with another combining the results.
    *
    * @param evaluation the other evaluation to combine
    */
   public void merge(RegressionEvaluation evaluation) {
      gold.addAllOf(evaluation.gold);
      predicted.addAllOf(evaluation.predicted);
   }

   @Override
   public void output(@NonNull PrintStream printStream) {
      TableFormatter formatter = new TableFormatter();
      formatter.title("Regression Metrics");
      formatter.header(Arrays.asList("Metric", "Value"));
      formatter.content(Arrays.asList("RMSE", rootMeanSquaredError()));
      formatter.content(Arrays.asList("R^2", r2()));
      formatter.content(Arrays.asList("Adj. R^2", adjustedR2()));
      formatter.print(printStream);
   }

   /**
    * Calculates the r2
    *
    * @return the r2
    */
   public double r2() {
      double yMean = Arrays.stream(gold.elements())
                           .average().orElse(0d);
      double SSTO = Arrays.stream(gold.elements())
                          .map(d -> Math.pow(d - yMean, 2))
                          .sum();
      double SSE = squaredError();
      return 1.0 - (SSE / SSTO);
   }

   /**
    * Calculates the root mean squared error
    *
    * @return the root mean squared error
    */
   public double rootMeanSquaredError() {
      return Math.sqrt(meanSquaredError());
   }

   /**
    * Sets the total number of predictor variables (i.e. features)
    *
    * @param p the number of predictor variables
    */
   public void setP(double p) {
      this.p = p;
   }

   /**
    * Calculates the squared error
    *
    * @return the squared error
    */
   public double squaredError() {
      double error = 0;
      for(int i = 0; i < gold.size(); i++) {
         error += Math.pow(predicted.get(i) - gold.get(i), 2);
      }
      return error;
   }
}//END OF RegressionEvaluation
