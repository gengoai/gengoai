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

package com.gengoai.apollo.ml;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gengoai.Validation.checkArgument;

/**
 * Representation of a split (e.g. fold, 80/20, etc.) of a {@link DataSet} into a train and test {@link DataSet}.
 *
 * @author David B. Bracewell
 */
public class Split {
   /**
    * The training dataset
    */
   public final DataSet train;
   /**
    * The testing dataset.
    */
   public final DataSet test;

   /**
    * Split the dataset into a train and test split.
    *
    * @param pctTrain the percentage of the dataset to use for training
    * @return A TestTrainSet of one TestTrain item
    */
   public static Split createTrainTestSplit(@NonNull DataSet data, double pctTrain) {
      checkArgument(pctTrain > 0 && pctTrain < 1, "Percentage should be between 0 and 1");
      int split = (int) Math.floor(pctTrain * data.size());
      List<Datum> dataList = data.collect();
      InMemoryDataSet train = new InMemoryDataSet(dataList.subList(0, split)
                                                          .stream()
                                                          .map(Datum::copy)
                                                          .collect(Collectors.toList()),
                                                  data.metadata,
                                                  data.ndArrayFactory);
      InMemoryDataSet test = new InMemoryDataSet(dataList.subList(split, dataList.size())
                                                         .stream()
                                                         .map(Datum::copy)
                                                         .collect(Collectors.toList()),
                                                 data.metadata,
                                                 data.ndArrayFactory);
      return new Split(train, test);
   }

   /**
    * Instantiates a new Split.
    *
    * @param train the training dataset
    * @param test  the testing dataset.
    */
   public Split(@NonNull DataSet train, @NonNull DataSet test) {
      this.train = train;
      this.test = test;
   }

   /**
    * Generates <code>numberOfFolds</code> {@link Split}s for cross-validation. Each split will have
    * <code>dataset.size() / numberOfFolds</code> testing data and the remaining data as training data.
    *
    * @param numberOfFolds the number of folds
    * @return An array of {@link Split} for each fold of the dataset
    */
   public static Split[] createFolds(@NonNull DataSet data, int numberOfFolds) {
      checkArgument(numberOfFolds > 0, "Number of folds must be >= 0");
      long size = data.size();
      checkArgument(size >= numberOfFolds, "Number of folds must be <= number of examples");
      Split[] folds = new Split[numberOfFolds];
      long foldSize = size / numberOfFolds;
      List<Datum> dataList = data.collect();
      for(int i = 0; i < numberOfFolds; i++) {
         int testStart = (int) (i * foldSize);
         int testEnd = (int) (testStart + foldSize);
         InMemoryDataSet test = new InMemoryDataSet(dataList.subList(testStart, testEnd)
                                                            .stream()
                                                            .map(Datum::copy)
                                                            .collect(Collectors.toList()),
                                                    data.metadata,
                                                    data.ndArrayFactory);
         List<Datum> trainData = new ArrayList<>();
         if(testStart > 0) {
            trainData.addAll(dataList.subList(0, testStart));
         }
         if(testEnd < size) {
            trainData.addAll(dataList.subList(testEnd, dataList.size()));
         }
         InMemoryDataSet train = new InMemoryDataSet(trainData.stream().map(Datum::copy).collect(Collectors.toList()),
                                                     data.metadata,
                                                     data.ndArrayFactory);
         folds[i] = new Split(train, test);
      }
      return folds;
   }

   @Override
   public String toString() {
      return "Split{train=" + train.size() + ", test=" + test.size() + "}";
   }

}//END OF TrainTest
