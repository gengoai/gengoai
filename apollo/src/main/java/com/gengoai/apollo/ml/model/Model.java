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

import com.gengoai.Copyable;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.transform.Transform;
import com.gengoai.io.Compression;
import com.gengoai.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;

/**
 * <p>
 * A model represents a learned mapping, i.e. {@link Transform}, from one or more input {@link Observation}s to one or
 * more outputs. A Model learns its mapping by estimating, or fitting, its model parameters to a given {@link DataSet}
 * (i.e. through the {@link Transform#fitAndTransform(DataSet)} method.
 * </p>
 * <p>
 * In addition to the parameters defining the mapping, each model has a set of parameters that control the learning
 * process and are represented as {@link FitParameters}. Each model will have its own subclass of {@link FitParameters}
 * defining its specific parameters. Implementations should have the following set of constructors for consistency.
 * </p>
 * <p><ul>
 * <li>Model() - No Arg Constructor that initializes the estimator with default parameters.</li>
 * <li>Model(? extends FitParameters) - FitParameter constructor that takes in a non-null instance of this
 * Model's custom FitParameters.</li>
 * <li>Model(Consumer&lt;? extends FitParameters&gt;) - Updating constructing which creates a default set of
 * parameters which is then updated by the given consumer.</li>
 * </ul></p>
 *
 * @author David B. Bracewell
 */
public interface Model extends Transform, Serializable {

   /**
    * Reads the model from the given resource
    *
    * @param resource the resource
    * @return the model
    * @throws IOException Something went wrong reading the model
    */
   static Model load(@NonNull Resource resource) throws IOException {
      return resource.getChild("model.bin.gz").readObject();
   }

   @Override
   default Model copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Estimates (fits) the model's parameters using the given {@link DataSet}
    *
    * @param dataset the dataset
    */
   void estimate(@NonNull DataSet dataset);

   @Override
   default DataSet fitAndTransform(@NonNull DataSet dataset) {
      estimate(dataset);
      return dataset.map(this::transform);
   }

   /**
    * Gets the {@link FitParameters} defined for this estimator
    *
    * @return the FitParameters
    */
   FitParameters<?> getFitParameters();

   /**
    * Returns the {@link LabelType} for the {@link Datum#DEFAULT_OUTPUT} source
    *
    * @return the LabelType
    */
   default LabelType getLabelType() {
      return getLabelType(Datum.DEFAULT_OUTPUT);
   }

   /**
    * Returns the {@link LabelType} for the given source
    *
    * @return the LabelType
    */
   LabelType getLabelType(@NonNull String name);

   /**
    * Writes the model the given ZipWriter.
    *
    * @param resource the resource to write the model to
    * @throws IOException Something went wrong writing the model
    */
   default void save(@NonNull Resource resource) throws IOException {
      resource.getChild("model.bin.gz").setCompression(Compression.GZIP).writeObject(this);
   }

}//END OF Model
