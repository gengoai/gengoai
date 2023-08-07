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

package com.gengoai.apollo.model;

import com.gengoai.ParameterDef;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.observation.Classification;
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.encoder.IndexEncoder;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.math.measure.Distance;
import com.gengoai.apollo.math.measure.Measure;
import com.gengoai.collection.HashMapIndex;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

import static com.gengoai.function.Functional.with;
import static com.gengoai.tuple.Tuples.$;

/**
 * Naive implementation of K nearest neighbors
 */
public class KNN extends SingleSourceModel<KNN.Parameters, KNN> {
    private static final long serialVersionUID = 1L;
    /**
     * K the number of items to use in making a label decision
     */
    public static final ParameterDef<Integer> K = ParameterDef.intParam("K");
    /**
     * The measure to use in selecting the best K points
     */
    public static final ParameterDef<Measure> MEASURE = ParameterDef.param("measure", Measure.class);


    private final com.gengoai.collection.Index<String> labels = new HashMapIndex<>();
    private final com.gengoai.collection.Index<String> features = new HashMapIndex<>();
    private final List<NumericNDArray> points = new ArrayList<>();

    /**
     * Instantiates a new LibLinear model.
     */
    public KNN() {
        this(new KNN.Parameters());
    }

    /**
     * Instantiates a new LibLinear model
     *
     * @param parameters the model parameters
     */
    public KNN(@NonNull KNN.Parameters parameters) {
        super(parameters);
    }

    /**
     * Instantiates a new LibLinear model
     *
     * @param updater the model parameter updater
     */
    public KNN(@NonNull Consumer<KNN.Parameters> updater) {
        this(with(new KNN.Parameters(), updater));
    }

    @Override
    public void estimate(@NonNull DataSet dataset) {
        labels.clear();
        points.clear();
        features.clear();

        //Build up the labels and features index
        //The output should not be vectorized and the input may or may not be vectorized
        dataset.forEach(d -> {
            labels.add(d.get(parameters.output.value()).asVariable().getName());
            Observation obs = d.get(parameters.input.value());
            if (obs.isVariableCollection()) {
                obs.getVariableSpace().forEach(v -> features.add(v.getName()));
            }
        });

        //Convert the dataset into a set of points
        dataset.forEach(d -> {
            NumericNDArray point;
            Observation obs = d.get(parameters.input.value());
            if (obs.isNDArray()) {
                point = obs.asNumericNDArray();
            } else {
                point = nd.SFLOAT32.zeros(features.size());
                for (Variable variable : obs.asVariableCollection()) {
                    point.set(features.getId(variable.getName()), variable.getValue());
                }
            }
            point.setLabel(d.get(parameters.output.value()).asVariable().getName());
            points.add(point);
        });
    }

    @Override
    public LabelType getLabelType(@NonNull String name) {
        if (name.equals(parameters.output.value())) {
            return LabelType.classificationType(labels.size());
        }
        throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
    }


    @Override
    protected Observation transform(@NonNull Observation observation) {
        Measure measure = getFitParameters().measure.value();
        PriorityQueue<Tuple2<String, Double>> pq = new PriorityQueue<>(
                (t1, t2) -> measure.getOptimum().compare(t1.v2, t2.v2)
        );

        //Build the numeric ndarray from the observation
        NumericNDArray source;
        if (features.isEmpty() || observation.isNDArray()) {
            source = observation.asNumericNDArray();
        } else {
            source = nd.SFLOAT32.zeros(features.size());
            for (Variable variable : observation.asVariableCollection()) {
                source.set(features.getId(variable.getName()), variable.getValue());
            }
        }

        //Calculate measure for each point in the training data
        for (NumericNDArray point : points) {
            pq.add($(point.getLabel().toString(), measure.calculate(point, source)));
        }


        //Build the classification
        NumericNDArray output = nd.DFLOAT32.zeros(labels.size());
        IndexEncoder encoder = new IndexEncoder(labels);
        pq.stream().limit(parameters.K.value()).forEach(t -> {
            long index = encoder.encode(t.v1);
            output.set(index, output.get(index).doubleValue() + 1.0);
        });
        return new Classification(output, encoder);
    }

    @Override
    protected void updateMetadata(@NonNull DataSet data) {

    }

    public static class Parameters extends SingleSourceFitParameters<Parameters> {
        private static final long serialVersionUID = 1L;
        /**
         * K the number of items to use in making a label decision
         */
        public final Parameter<Integer> K = parameter(KNN.K, 2);


        public final Parameter<Measure> measure = parameter(KNN.MEASURE, Distance.Euclidean);

    }//END OF Parameters

}//END OF KNN
