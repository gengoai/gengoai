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

package com.gengoai.apollo.evaluation;

import com.gengoai.Validation;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Split;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.measure.Correlation;
import com.gengoai.apollo.model.Model;
import com.gengoai.conversion.Cast;
import com.gengoai.string.TableFormatter;
import lombok.NonNull;
import org.apache.mahout.math.list.DoubleArrayList;

import java.io.PrintStream;

import static java.util.Arrays.asList;

/**
 * <p>Common methods and metrics for evaluating a binary classifier Evaluation includes the following metrics in
 * addition to those in {@link ClassifierEvaluation}.</p>
 * <ul>
 * <li>Area Under the Curve (AUC)</li>
 * <li>Accuracy</li>
 * <li>False Positive Rate</li>
 * <li>False Negative Rate</li>
 * <li>True Positive Rate</li>
 * <li>True negative Rate</li>
 * <li>Majority Class Baseline</li>
 * </ul>
 *
 * @author David B. Bracewell
 */
public class BinaryEvaluation extends ClassifierEvaluation {
    private static final long serialVersionUID = 1L;
    private final DoubleArrayList[] prob = {new DoubleArrayList(), new DoubleArrayList()};
    private double fn = 0;
    private double fp = 0;
    private double negative = 0d;
    private double positive = 0d;
    private double tn = 0;
    private double tp = 0;

    /**
     * Instantiates a new BinaryEvaluation.
     *
     * @param outputName the name of the output source we will evaluate
     */
    public BinaryEvaluation(@NonNull String outputName) {
        super(outputName);
    }

    /**
     * Performs a cross-validation of the given classifier using the given dataset
     *
     * @param dataset the dataset to perform cross-validation on
     * @param model   the classifier to train and test
     * @param nFolds  the number of folds to perform
     * @return the classifier evaluation
     */
    public static BinaryEvaluation crossvalidation(@NonNull DataSet dataset,
                                                   @NonNull Model model,
                                                   int nFolds,
                                                   String outputName) {
        Validation.notNullOrBlank(outputName, "The output name cannot be blank or null.");
        Validation.checkArgument(nFolds > 1, "Must specify more than 1 fold.");
        BinaryEvaluation evaluation = new BinaryEvaluation(outputName);
        for (Split split : Split.createFolds(dataset.shuffle(), nFolds)) {
            DataSet d = model.fitAndTransform(split.train);
            split.test.putAllMetadata(d.getMetadata());
            evaluation.evaluate(model, split.test);
        }
        return evaluation;
    }

    /**
     * Evaluates the given Model with the given testing data.
     *
     * @param model            the model
     * @param testingData      the testing data
     * @param outputSourceName the output source name
     * @return the binary evaluation
     */
    public static BinaryEvaluation evaluate(@NonNull Model model,
                                            @NonNull DataSet testingData,
                                            @NonNull String outputSourceName) {
        Validation.notNullOrBlank(outputSourceName, "The output name cannot be blank or null.");
        BinaryEvaluation evaluation = new BinaryEvaluation(outputSourceName);
        evaluation.evaluate(model, testingData);
        return evaluation;
    }

    @Override
    public double accuracy() {
        return (tp + tn) / (positive + negative);
    }

    /**
     * Calculates the AUC (Area Under the Curve)
     *
     * @return the AUC
     */
    public double auc() {
        return Correlation.AUC.calculate(prob[0].elements(), prob[1].elements());
    }

    /**
     * Calculates the baseline score, which is <code>max(positive,negative) / (positive+negative)</code>
     *
     * @return the baseline score
     */
    public double baseline() {
        double total = positive + negative;
        double class0 = tn + fp;
        double class1 = tp + fn;
        return Math.max(class0, class1) / total;
    }

    @Override
    public void entry(double gold, @NonNull NumericNDArray predicted) {
        int goldClass = (int) gold;
        int predictedClass = (int) predicted.argMaxOffset();
        prob[goldClass].add(predicted.getDouble(1));
        if (goldClass == 1) {
            positive++;
            if (predictedClass == 1) {
                tp++;
            } else {
                fn++;
            }
        } else {
            negative++;
            if (predictedClass == 1) {
                fp++;
            } else {
                tn++;
            }
        }
    }

    @Override
    public void evaluate(@NonNull Model model, @NonNull DataSet dataset) {
        dataset.forEach(d -> entry(getIntegerLabelFor(d.get(outputName), dataset),
                                   model.transform(d).get(outputName).asNumericNDArray()));
    }

    @Override
    public double falseNegatives() {
        return fn;
    }

    @Override
    public double falsePositives() {
        return fp;
    }

    @Override
    public void merge(ClassifierEvaluation evaluation) {
        if (evaluation instanceof BinaryEvaluation) {
            BinaryEvaluation bce = Cast.as(evaluation);
            this.prob[0].addAllOf(bce.prob[0]);
            this.prob[1].addAllOf(bce.prob[1]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void output(PrintStream printStream, boolean printConfusionMatrix) {
        TableFormatter tableFormatter = new TableFormatter();

        if (printConfusionMatrix) {
            tableFormatter.header(asList("Predicted / Gold", "TRUE", "FALSE", "TOTAL"));
            tableFormatter.content(
                    asList("TRUE", truePositives(), falsePositives(), (truePositives() + falsePositives())));
            tableFormatter.content(
                    asList("FALSE", falseNegatives(), trueNegatives(), (falseNegatives() + trueNegatives())));
            tableFormatter.footer(asList("", (truePositives() + falseNegatives()), (falsePositives() + trueNegatives()),
                                         positive + negative));
            tableFormatter.print(printStream);
            tableFormatter = new TableFormatter();
        }

        tableFormatter.header(asList("Metric", "Score"));
        tableFormatter.content(asList("AUC", auc()));
        tableFormatter.content(asList("Accuracy", accuracy()));
        tableFormatter.content(asList("Baseline", baseline()));
        tableFormatter.content(asList("TP Rate", truePositiveRate()));
        tableFormatter.content(asList("FP Rate", falsePositiveRate()));
        tableFormatter.content(asList("TN Rate", trueNegativeRate()));
        tableFormatter.content(asList("FN Rate", falseNegativeRate()));
        tableFormatter.print(printStream);
    }

    @Override
    public double trueNegatives() {
        return tn;
    }

    @Override
    public double truePositives() {
        return tp;
    }

}//END OF BinaryClassifierEvaluation
