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

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.ObservationMetadata;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.NoOptEncoder;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.transform.MultiInputTransform;
import com.gengoai.apollo.ml.transform.SingleSourceTransform;
import com.gengoai.apollo.ml.transform.Transform;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.collection.Sets;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * A pipeline model pairs a {@link Model} with a {@link Transformer} where the transformer is used to preprocess the
 * {@link DataSet} and {@link Datum} before fitting and transformation. Additionally, pipeline models will attempt to
 * decode the model output ({@link com.gengoai.apollo.math.linalg.NDArray}) based on the {@link LabelType}. For example, a
 * classifier would have its output NDArray transformed into a {@link com.gengoai.apollo.ml.observation.Classification}
 * and a sequence labeler would have its output translated into a {@link com.gengoai.apollo.ml.observation.VariableSequence}.
 * </p>
 * <p>
 * Pipeline models are instantiated through the use of a builder class as follows:
 * </p>
 * <pre>
 * {@code
 *    PipelineModel model = PipelineModel.builder()
 *                                       .source("words", new MinCountTransform(5),
 *                                                        new TFIDFTransform())
 *                                       .source("chars", new MinCountTransform(10))
 *                                       .add(new CounterVectorizer().sources("words","chars"))
 *                                       .build(new LibLinear());
 *   model.estimate(...);
 * }
 * </pre>
 *
 * @author David B. Bracewell
 */
public class PipelineModel implements Model {
   private static final long serialVersionUID = 1L;
   private Model model;
   @NonNull
   private Transformer transformer;

   /**
    * Creates a new Builder to construct a PipelineModel
    *
    * @return the builder
    */
   public static Builder builder() {
      return new Builder();
   }

   private PipelineModel(@NonNull Model model, @NonNull Transformer preprocessors) {
      this.model = model;
      this.transformer = preprocessors;
   }

   /**
    * Gets the model wrapped by this PipelineModel as the given model type
    *
    * @param <T> the type parameter
    * @return the wrapped model
    */
   public <T extends Model> T getWrappedModel() {
      return Cast.as(model);
   }

   @Override
   public PipelineModel copy() {
      return new PipelineModel(model.copy(), transformer.copy());
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      model.estimate(transformer.fitAndTransform(dataset));
   }

   @Override
   public DataSet fitAndTransform(@NonNull DataSet dataset) {
      estimate(dataset);
      return model.transform(dataset);
   }

   private Encoder getEncoder(String name) {
      if(hasEncoder(name)) {
         return transformer.getMetadata().get(name).getEncoder();
      }
      return NoOptEncoder.INSTANCE;
   }

   @Override
   public FitParameters<?> getFitParameters() {
      return model.getFitParameters();
   }

   @Override
   public Set<String> getInputs() {
      return Sets.union(transformer.getInputs(), model.getInputs());
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      return model.getLabelType(name);
   }

   @Override
   public Set<String> getOutputs() {
      return model.getOutputs();
   }

   private boolean hasEncoder(String name) {
      ObservationMetadata om = transformer.getMetadata().getOrDefault(name, new ObservationMetadata());
      return getLabelType(name) != LabelType.NDArray && om.getEncoder() != null;
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      dataset = transformer.transform(dataset).map(this::transform);
      for(String output : model.getOutputs()) {
         dataset.updateMetadata(output, m -> {
            m.setType(getLabelType(output).getObservationClass());
            m.setEncoder(getEncoder(output));
            if(getLabelType(output) != LabelType.NDArray) {
               m.setDimension(m.getEncoder().size());
            }
         });
      }
      return dataset;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      Datum y = model.transform(transformer.transform(datum));
      for(String name : model.getOutputs()) {
         Observation o = y.get(name);
         if(o.isNDArray() && hasEncoder(name)) {
            y.put(name, getLabelType(name).transform(getEncoder(name), o));
         }
      }
      return y;
   }

   /**
    * Builder class to construct a {@link PipelineModel}
    */
   public static class Builder {
      private List<Transform> transforms = new ArrayList<>();

      /**
       * Constructs a PipelineModel which will execute the added transforms before fitting / transforming with the
       * supplied model
       *
       * @param model the model
       * @return the PipelineModel
       */
      public PipelineModel build(@NonNull Model model) {
         return new PipelineModel(model, new Transformer(transforms));
      }

      /**
       * Adds one or more {@link SingleSourceTransform}s and sets the input and output to {@link Datum#DEFAULT_INPUT}
       *
       * @param transforms the transforms to apply to the default input
       * @return this builder
       */
      public Builder defaultInput(@NonNull SingleSourceTransform... transforms) {
         return source(Datum.DEFAULT_INPUT, transforms);
      }

      /**
       * Adds one or more {@link SingleSourceTransform}s and sets the input and output to {@link Datum#DEFAULT_OUTPUT}
       *
       * @param transforms the transforms to apply to the default output
       * @return this builder
       */
      public Builder defaultOutput(@NonNull SingleSourceTransform... transforms) {
         return source(Datum.DEFAULT_OUTPUT, transforms);
      }

      /**
       * Adds one or more {@link MultiInputTransform}s and sets the inputs to the given source names. Note that this
       * only sets the inputs.
       *
       * @param inputs     the source names
       * @param transforms the transforms to apply to the given source name.
       * @return this builder
       */
      public Builder source(@NonNull String[] inputs,
                            @NonNull MultiInputTransform... transforms) {
         for(MultiInputTransform transform : transforms) {
            this.transforms.add(transform.inputs(inputs));
         }
         return this;
      }

      /**
       * Adds one or more {@link SingleSourceTransform}s and sets the input and output to the given source name.
       *
       * @param name       the source name
       * @param transforms the transforms to apply to the given source name.
       * @return this builder
       */
      public Builder source(@NonNull String name,
                            @NonNull SingleSourceTransform... transforms) {
         for(SingleSourceTransform transform : transforms) {
            this.transforms.add(transform.source(name));
         }
         return this;
      }

      /**
       * Adds the given transform. Care should be taken to properly set the inputs / outputs.
       *
       * @param transform the transform to add.
       * @return this builder
       */
      public Builder transform(@NonNull Transform transform) {
         this.transforms.add(transform);
         return this;
      }

   }

}//END OF PipelineModel
