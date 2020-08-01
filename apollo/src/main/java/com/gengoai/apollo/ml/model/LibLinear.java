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
import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.observation.Observation;
import de.bwaldvogel.liblinear.*;
import lombok.NonNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.gengoai.function.Functional.with;

/**
 * <p>
 * Wrapper around LibLinear. Inputs and outputs are required to be vectorized (i.e. NDArray).
 * </p>
 *
 * @author David B. Bracewell
 */
public class LibLinear extends SingleSourceModel<LibLinear.Parameters, LibLinear> {
   private static final long serialVersionUID = 1L;
   /**
    * C is the cost of constraints violation. (we usually use 1 to 1000)
    */
   public static final ParameterDef<Double> C = ParameterDef.doubleParam("C");
   /**
    * set the epsilon in loss function of epsilon-SVR(default 0.1)
    */
   public static final ParameterDef<Double> P = ParameterDef.doubleParam("P");
   /**
    * Whether to include bias or not
    */
   public static final ParameterDef<Boolean> bias = ParameterDef.boolParam("bias");
   /**
    * eps is the stopping criterion. (we usually use 0.01).
    */
   public static final ParameterDef<Double> eps = ParameterDef.doubleParam("eps");
   /**
    * The solver used
    */
   public static final ParameterDef<SolverType> solver = ParameterDef.param("solver", SolverType.class);

   private de.bwaldvogel.liblinear.Model model;
   private int biasIndex;

   private static double getLabel(NDArray n) {
      if(n.shape().isScalar()) {
         return n.get(0);
      }
      return n.argmax();
   }

   private static Feature[] toFeature(NDArray vector, int biasIndex) {
      int size = (int) vector.size() + (biasIndex > 0
                                        ? 1
                                        : 0);
      final Feature[] feature = new Feature[size];
      AtomicInteger ai = new AtomicInteger(0);
      if(vector.isDense()) {
         for(long i = 0; i < vector.length(); i++) {
            feature[ai.getAndIncrement()] = new FeatureNode((int) i + 1, vector.get(i));
         }
      } else {
         int[] keys = vector.sparseIndices();
         Arrays.sort(keys);
         for(int i : keys) {
            feature[ai.getAndIncrement()] = new FeatureNode(i + 1, vector.get(i));
         }
      }
      if(biasIndex > 0) {
         feature[size - 1] = new FeatureNode(biasIndex, 1.0);
      }
      return feature;
   }

   /**
    * Instantiates a new LibLinear model.
    */
   public LibLinear() {
      this(new Parameters());
   }

   /**
    * Instantiates a new LibLinear model
    *
    * @param parameters the model parameters
    */
   public LibLinear(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new LibLinear model
    *
    * @param updater the model parameter updater
    */
   public LibLinear(@NonNull Consumer<Parameters> updater) {
      this(with(new Parameters(), updater));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      dataset = dataset.cache();
      Validation.checkArgument(dataset.size() > 0, "Empty dataset");
      biasIndex = parameters.bias.value()
                  ? 0
                  : -1;
      int numFeatures = (int) dataset.getMetadata(parameters.input.value()).getDimension();
      Problem problem = new Problem();
      problem.l = (int) dataset.size();
      problem.x = new Feature[problem.l][];
      problem.y = new double[problem.l];
      problem.n = numFeatures + 1;
      problem.bias = biasIndex >= 0
                     ? 0
                     : -1;
      dataset.stream()
             .zipWithIndex()
             .forEach((datum, index) -> {
                problem.x[index.intValue()] = toFeature(datum.get(parameters.input.value()).asNDArray(), 0);
                problem.y[index.intValue()] = getLabel(datum.get(parameters.output.value()).asNDArray());
             });

      if(parameters.verbose.value()) {
         Linear.enableDebugOutput();
      } else {
         Linear.disableDebugOutput();
      }

      model = Linear.train(problem, new Parameter(parameters.solver.value(),
                                                  parameters.C.value(),
                                                  parameters.eps.value(),
                                                  parameters.maxIterations.value(),
                                                  parameters.P.value()));
   }

   @Override
   public Parameters getFitParameters() {
      return parameters;
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if(name.equals(parameters.output.value())) {
         if(parameters.solver.value().isSupportVectorRegression()) {
            return LabelType.NDArray;
         }
         return LabelType.classificationType(model.getNrClass());
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      double[] p = new double[model.getNrClass()];
      if(model.isProbabilityModel()) {
         Linear.predictProbability(model, toFeature(observation.asNDArray(), biasIndex), p);
      } else {
         Linear.predictValues(model, toFeature(observation.asNDArray(), biasIndex), p);
      }

      if(parameters.solver.value().isSupportVectorRegression()) {
         return NDArrayFactory.ND.scalar(p[0]);
      }

      //re-arrange the probabilities to match the target feature
      double[] prime = new double[model.getNrClass()];
      int[] labels = model.getLabels();
      for(int i = 0; i < labels.length; i++) {
         prime[labels[i]] = p[i];
      }
      return NDArrayFactory.ND.rowVector(prime);
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      data.updateMetadata(parameters.output.value(), m -> {
         m.setDimension(model.getNrFeature());
         m.setType(NDArray.class);
      });
   }

   /**
    * Custom fit parameters for LibLinear
    */
   public static class Parameters extends SingleSourceFitParameters<Parameters> {
      private static final long serialVersionUID = 1L;
      /**
       * The cost parameter (default 1.0)
       */
      public final Parameter<Double> C = parameter(LibLinear.C, 1.0);
      /**
       * set the epsilon in loss function of epsilon-SVR (default 0.1)
       */
      public final Parameter<Double> P = parameter(LibLinear.P, 0.1);
      /**
       * Use a bias feature or not. (default false)
       */
      public final Parameter<Boolean> bias = parameter(LibLinear.bias, false);
      /**
       * * eps is the stopping criterion. (we usually use 0.01).
       */
      public final Parameter<Double> eps = parameter(LibLinear.eps, 0.01);
      /**
       * The maximum number of iterations to run the trainer (Default 1000)
       */
      public final Parameter<Integer> maxIterations = parameter(Params.Optimizable.maxIterations, 1000);
      /**
       * The Solver to use. (default L2R_LR)
       */
      public final Parameter<SolverType> solver = parameter(LibLinear.solver, SolverType.L2R_LR);
   }

}//END OF LibLinearModel
