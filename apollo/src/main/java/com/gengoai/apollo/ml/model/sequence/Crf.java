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

package com.gengoai.apollo.ml.model.sequence;

import com.gengoai.ParameterDef;
import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Params;
import com.gengoai.apollo.ml.model.SingleSourceFitParameters;
import com.gengoai.apollo.ml.model.SingleSourceModel;
import com.gengoai.apollo.ml.observation.*;
import com.gengoai.conversion.Cast;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.jcrfsuite.CrfTagger;
import com.gengoai.jcrfsuite.util.Pair;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;
import third_party.org.chokkan.crfsuite.*;

import java.io.IOException;
import java.util.Base64;
import java.util.function.Consumer;

import static com.gengoai.function.Functional.with;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>
 * A wrapper around CrfSuite. This model will work with both {@link Sequence} and {@link NDArray} as input, but requires
 * a {@link Sequence} for output.
 * </p>
 *
 * @author David B. Bracewell
 */
public class Crf extends SingleSourceModel<Crf.Parameters, Crf> {
   private static final long serialVersionUID = 1L;
   public static final ParameterDef<Double> C1 = ParameterDef.doubleParam("c1");
   public static final ParameterDef<Double> C2 = ParameterDef.doubleParam("c2");
   public static final ParameterDef<Double> EPS = ParameterDef.doubleParam("eps");
   public static final ParameterDef<Integer> MIN_FEATURE_FREQ = ParameterDef.intParam("minFeatureFreq");
   public static final ParameterDef<CrfSolver> SOLVER = ParameterDef.param("solver", CrfSolver.class);
   protected String modelFile;
   protected volatile CrfTagger tagger;

   /**
    * Instantiates a new Crf with default parameters.
    */
   public Crf() {
      super(new Parameters());
   }

   /**
    * Instantiates a new Crf with the given parameters.
    *
    * @param parameters the parameters
    */
   public Crf(@NonNull Parameters parameters) {
      super(parameters);
   }

   /**
    * Instantiates a new Crf with the given parameter updater.
    *
    * @param updater the updater
    */
   public Crf(@NonNull Consumer<Parameters> updater) {
      super(with(new Parameters(), updater));
   }

   private Item createItem(Observation o) {
      Item item = new Item();
      if(o instanceof VariableCollection) {
         VariableList instance = Cast.as(o);
         for(Variable feature : instance) {
            item.add(new Attribute(feature.getName(), feature.getValue()));
         }
      } else if(o instanceof Variable) {
         Variable v = Cast.as(o);
         item.add(new Attribute(v.getName(), v.getValue()));
      } else {
         throw new IllegalStateException("Unsupported type: " + o.getClass());
      }
      return item;
   }

   private Item createItem(NDArray row) {
      Item item = new Item();
      row.forEachSparse((i, v) -> item.add(new Attribute(Long.toString(i), v)));
      return item;
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      CrfSuiteLoader.INSTANCE.load();
      Trainer trainer = new Trainer();

      for(Datum datum : dataset) {
         Observation input = Validation.notNull(datum.get(parameters.input.value()),
                                                "Null Input Observation");
         Sequence<?> output = Validation.notNull(datum.get(parameters.output.value()).asSequence(),
                                                 "Null Output Observation");
         Tuple2<ItemSequence, StringList> instance = toItemSequence(input, output);
         trainer.append(instance.v1, instance.v2, 0);
      }
      trainer.select(parameters.crfSolver.value().parameterSetting, "crf1d");
      trainer.set("max_iterations", Integer.toString(parameters.maxIterations.value()));
      trainer.set("c2", Double.toString(parameters.c2.value()));
      trainer.set("c1", Double.toString(parameters.c1.value()));
      trainer.set("epsilon", Double.toString(parameters.eps.value()));
      trainer.set("feature.minfreq", Integer.toString(parameters.minFeatureFreq.value()));
      modelFile = Resources.temporaryFile().asFile().orElseThrow(IllegalArgumentException::new).getAbsolutePath();
      trainer.train(modelFile, -1);
      trainer.clear();
      tagger = new CrfTagger(modelFile);
   }

   @Override
   public Parameters getFitParameters() {
      return parameters;
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if(parameters.output.value().equals(name)) {
         return LabelType.Sequence;
      }
      throw new IllegalArgumentException("'" + name + "' is not a valid output for this model.");
   }

   private void readObject(java.io.ObjectInputStream stream) throws Exception {
      CrfSuiteLoader.INSTANCE.load();
      Resource tmp = Resources.temporaryFile();
      int length = stream.readInt();
      byte[] bytes = new byte[length];
      stream.readFully(bytes);
      tmp.write(Base64.getDecoder().decode(bytes));
      this.modelFile = tmp.asFile().orElseThrow(IllegalArgumentException::new).getAbsolutePath();
      this.tagger = new CrfTagger(modelFile);
   }

   private Tuple2<ItemSequence, StringList> toItemSequence(Observation in, Sequence<? extends Observation> out) {
      ItemSequence seq = new ItemSequence();
      StringList labels = new StringList();
      if(in instanceof Sequence) {
         Sequence<? extends Observation> sequence = Cast.as(in);
         sequence.forEach(o -> seq.add(createItem(o)));
      } else if(in instanceof NDArray) {
         NDArray ndArray = Cast.as(in);
         for(int i = 0; i < ndArray.rows(); i++) {
            seq.add(createItem(ndArray.getRow(i)));
         }
      } else {
         throw new IllegalArgumentException("Observations of type '" + in.getClass() + "' are not supported as input");
      }
      if(out != null) {
         for(Observation o : out) {
            if(o instanceof Variable) {
               Variable v = Cast.as(o);
               labels.add(v.getName());
            } else {
               throw new IllegalArgumentException("Observations of type '" + o.getClass() + "' are not supported as an output");
            }
         }
      }
      return $(seq, labels);
   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      CrfSuiteLoader.INSTANCE.load();
      ItemSequence itemSequence = toItemSequence(observation, null).v1;
      VariableSequence labeling = new VariableSequence();
      for(Pair<String, Double> pair : tagger.tag(itemSequence)) {
         labeling.add(new Variable(pair.first, pair.second));
      }
      //      deleteItemSequence(itemSequence);
      return labeling;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      data.updateMetadata(parameters.output.value(), m -> {
         m.setType(VariableSequence.class);
      });
   }

   private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
      byte[] modelBytes = Base64.getEncoder().encode(Resources.from(modelFile).readBytes());
      stream.writeInt(modelBytes.length);
      stream.write(modelBytes);
   }

   /**
    * Crf Fit Parameters
    */
   public static class Parameters extends SingleSourceFitParameters<Parameters> {
      private static final long serialVersionUID = 1L;
      /**
       * The maximum number of iterations (defaults to 200)
       */
      public final Parameter<Integer> maxIterations = parameter(Params.Optimizable.maxIterations, 250);
      /**
       * The type of solver to use (defaults to LBFGS)
       */
      public final Parameter<CrfSolver> crfSolver = parameter(SOLVER, CrfSolver.LBFGS);
      /**
       * The coefficient for L1 regularization (default 0.0 - not used)
       */
      public final Parameter<Double> c1 = parameter(C1, 0d);
      /**
       * The coefficient for L2 regularization (default 1.0)
       */
      public final Parameter<Double> c2 = parameter(C2, 1d);
      /**
       * The epsilon parameter to determine convergence (default is 1e-5)
       */
      public final Parameter<Double> eps = parameter(EPS, 1e-5);
      /**
       * The minimum number of times a feature must appear to be kept (default 0 - keep all)
       */
      public final Parameter<Integer> minFeatureFreq = parameter(MIN_FEATURE_FREQ, 0);
   }
}//END OF Crf
