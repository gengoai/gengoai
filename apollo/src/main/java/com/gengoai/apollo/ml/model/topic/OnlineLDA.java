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

import com.gengoai.ParameterDef;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import lombok.NonNull;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.special.Gamma;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.gengoai.apollo.ml.observation.VariableCollection.mergeVariableSpace;
import static com.gengoai.function.Functional.with;

/**
 * <p>
 * An online implementation of LDA which performs a single pass over the data set.
 * </p>
 *
 * @author David B. Bracewell
 */
public class OnlineLDA extends BaseVectorTopicModel {
   private static final GammaDistribution GAMMA_DISTRIBUTION = new GammaDistribution(100d, 0.01d);
   private static final long serialVersionUID = 1L;
   public static final ParameterDef<Double> alpha = ParameterDef.doubleParam("alpha");
   public static final ParameterDef<Double> eta = ParameterDef.doubleParam("eta");
   public static final ParameterDef<Integer> inferenceSamples = ParameterDef.intParam("inferenceSamples");
   public static final ParameterDef<Double> kappa = ParameterDef.doubleParam("kappa");
   public static final ParameterDef<Double> tau0 = ParameterDef.doubleParam("tau0");
   private final OnlineLDAFitParameters parameters;
   private NDArray lambda;

   /**
    * Instantiates a new OnlineLDA with default parameters.
    */
   public OnlineLDA() {
      this(new OnlineLDAFitParameters());
   }

   /**
    * Instantiates a new OnlineLDA with the given parameters.
    *
    * @param parameters the parameters
    */
   public OnlineLDA(@NonNull OnlineLDAFitParameters parameters) {
      this.parameters = parameters;
   }

   /**
    * Instantiates a new OnlineLDA with the given parameter updater
    *
    * @param updater the updater
    */
   public OnlineLDA(@NonNull Consumer<OnlineLDAFitParameters> updater) {
      this.parameters = with(new OnlineLDAFitParameters(), updater);
   }

   private NDArray dirichletExpectation(NDArray array) {
      NDArray vector = array.rowSums().mapi(Gamma::digamma);
      return array.map(Gamma::digamma)
                  .subiColumnVector(vector);
   }

   private void eStep(ModelP m, List<NDArray> batch) {
      m.gamma = gammaSample(batch.size(), m.K);
      var eLogTheta = dirichletExpectation(m.gamma);
      var expELogTheta = eLogTheta.map(Math::exp);
      m.stats = m.lambda.zeroLike();

      for(int i = 0; i < batch.size(); i++) {
         NDArray n = batch.get(i);
         int[] ids = n.sparseIndices();
         if(ids.length == 0) {
            continue;
         }
         NDArray nv = NDArrayFactory.DENSE.array(ids.length);
         for(int i1 = 0, i2 = 0; i1 < ids.length; i1++, i2++) {
            nv.set(i2, n.get(ids[i1]));
         }
         var gammaD = m.gamma.getRow(i);
         var expELogThetaD = expELogTheta.getRow(i);
         var expELogBetaD = m.expELogBeta.getColumns(ids);
         var phiNorm = expELogThetaD.mmul(expELogBetaD).addi(1E-100);

         NDArray lastGamma;
         for(int iteration = 0; iteration < parameters.inferenceSamples.value(); iteration++) {
            lastGamma = gammaD;
            var v1 = nv.div(phiNorm).mmul(expELogBetaD.T());
            gammaD = expELogThetaD.mul(v1).addi(m.alpha);
            var eLogThetaD = dirichletExpectation(gammaD);
            expELogThetaD = eLogThetaD.map(Math::exp);
            phiNorm = expELogThetaD.mmul(expELogBetaD).addi(1E-100);
            if(gammaD.map(lastGamma, (d1, d2) -> Math.abs(d1 - d2)).mean() < 0.001) {
               break;
            }
         }
         m.gamma.setRow(i, gammaD);
         var o = outer(expELogThetaD, nv.div(phiNorm));
         for(int k = 0; k < ids.length; k++) {
            m.stats.incrementiColumn(ids[k], o.getColumn(k));
         }
      }
      m.stats.muli(m.expELogBeta);
   }

   private Stream<NDArray> encode(Datum d) {
      if(parameters.combineInputs.value()) {
         return mergeVariableSpace(d.stream(getInputs()))
               .getVariableSpace()
               .map(o -> toCountVector(o, parameters.namingPattern.value()));
      }
      return d.stream(getInputs())
              .map(o -> toCountVector(o, parameters.namingPattern.value()));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      encoderFit(dataset, getInputs(), parameters.namingPattern.value());
      final ModelP model = new ModelP();
      final double D = dataset.size();
      int batchSize = parameters.batchSize.value();
      int batchCount = 0;
      for(DataSet docs : Iterables.asIterable(dataset.batchIterator(batchSize))) {
         List<NDArray> batch = docs.parallelStream()
                                   .flatMap(this::encode)
                                   .collect();
         eStep(model, batch);
         mStep(model, D, batchCount, batch.size());
         batchCount++;
      }
      model.lambda.diviColumnVector(model.lambda.rowSums());
      for(int i = 0; i < model.lambda.rows(); i++) {
         NDArray topic = model.lambda.getRow(i);
         Counter<String> cntr = Counters.newCounter();
         topic.forEachSparse((fi, v) -> {
            cntr.set(encoder.decode(fi), v);
         });
         topics.add(new Topic(i, cntr));
      }
      this.lambda = model.lambda;
   }

   private NDArray gammaSample(int r, int c) {
      return NDArrayFactory.DENSE.array(r, c).mapi(d -> GAMMA_DISTRIBUTION.sample());
   }

   @Override
   public OnlineLDAFitParameters getFitParameters() {
      return parameters;
   }

   @Override
   public NDArray getTopicDistribution(String feature) {
      NDArray n = NDArrayFactory.ND.array(getNumberOfTopics());
      for(Topic topic : topics) {
         n.set(topic.getId(), topic.getFeatureDistribution().get(feature));
      }
      return n;
   }

   @Override
   protected NDArray inference(NDArray n) {
      final ModelP model = new ModelP(lambda);
      eStep(model, Collections.singletonList(n));
      return model.gamma.divi(model.gamma.sum());
   }

   private void mStep(ModelP model, double dataSetSize, int batchCount, int batchSize) {
      double rho = Math.pow(parameters.tau0.value() + batchCount, -parameters.kappa.value());
      NDArray a = model.lambda.mul(1 - rho);
      NDArray b = model.stats.mul(dataSetSize / batchSize).addi(parameters.eta.value());
      model.lambda = a.add(b);
      model.eLogBeta = dirichletExpectation(model.lambda);
      model.expELogBeta = model.eLogBeta.map(Math::exp);
   }

   private NDArray outer(NDArray vector, NDArray matrix) {
      NDArray out = NDArrayFactory.DENSE.array((int) vector.length(), (int) matrix.length());
      for(long i = 0; i < vector.length(); i++) {
         for(long j = 0; j < matrix.length(); j++) {
            out.set((int) i, (int) j, vector.get(i) * matrix.get(j));
         }
      }
      return out;
   }

   public static class OnlineLDAFitParameters extends TopicModelFitParameters {
      /**
       * The number of topics to discover (default 100).
       */
      public final Parameter<Integer> K = parameter(Params.Clustering.K, 100);
      /**
       * The number of documents to process at a time (default 512).
       */
      public final Parameter<Integer> batchSize = parameter(Params.Optimizable.batchSize, 512);
      /**
       * Hyperparaemeter defining the prior on the weight vectors (default 0.1).
       */
      public final Parameter<Double> alpha = parameter(OnlineLDA.alpha, 0.1);
      /**
       * Hyperparaemeter defining the prior on the topics (default 0.1).
       */
      public final Parameter<Double> eta = parameter(OnlineLDA.eta, 0.01);
      /**
       * The learning parameter to lessen the influence of early iterations (default 1.0).
       */
      public final Parameter<Double> tau0 = parameter(OnlineLDA.tau0, 1.0);
      /**
       * The exponential learning decay rate  (default 0.75).
       */
      public final Parameter<Double> kappa = parameter(OnlineLDA.kappa, 0.75);
      /**
       * The number of samples to perform during the estimation step (default 100).
       */
      public final Parameter<Integer> inferenceSamples = parameter(OnlineLDA.inferenceSamples, 100);
   }

   private class ModelP {
      NDArray lambda;
      NDArray eLogBeta;
      NDArray expELogBeta;
      NDArray stats;
      NDArray gamma;
      int K;
      double alpha;

      public ModelP() {
         K = parameters.K.value();
         lambda = gammaSample(K, encoder.size());
         eLogBeta = dirichletExpectation(lambda);
         expELogBeta = eLogBeta.map(Math::exp);
         stats = lambda.zeroLike();
         alpha = parameters.alpha.value();
      }

      public ModelP(NDArray pLambda) {
         K = parameters.K.value();
         lambda = pLambda;
         eLogBeta = dirichletExpectation(lambda);
         expELogBeta = eLogBeta.map(Math::exp);
         stats = lambda.zeroLike();
         alpha = parameters.alpha.value();
      }

   }

}//END OF OnlineLDA
