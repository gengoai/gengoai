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

package com.gengoai.apollo.ml.transform;

import com.gengoai.Copyable;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.ObservationMetadata;
import com.gengoai.json.TypeInfo;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Set;

/**
 * <p>
 * A function that transforms one or more observations on a {@link Datum}. Transforms often require being "fitted" to
 * data the data before the transform can take place. Typically, transforms are used as part of a {@link
 * com.gengoai.apollo.ml.model.PipelineModel} where multiple transforms preprocess data before being feed into a machine
 * learning algorithm. The <code>Pipeline</code> handles fitting its transforms to the training data set and applying
 * the transforms to new datum.
 * </p>
 * <p>
 * Note that transforms may be done in-place, i.e. an update, or create a new {@link Datum} object. Transforms should
 * update the {@link ObservationMetadata} on the {@link DataSet} during data set transformations.
 * </p>
 *
 * @author David B. Bracewell
 */
@TypeInfo
public interface Transform extends Serializable, Copyable<Transform> {

   @Override
   default Transform copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Fits the transform to dataset where this could mean collecting statistics or even building a model. After fitting
    * the transform is applied to the dataset and updates the {@link ObservationMetadata} for the DataSet to reflect any
    * changes.
    *
    * @param dataset the dataset
    * @return the transformed DataSet
    */
   DataSet fitAndTransform(@NonNull DataSet dataset);

   /**
    * Returns the set of sources this transform uses as input.
    *
    * @return the set of sources this transform uses as input.
    */
   Set<String> getInputs();

   /**
    * Returns the set of sources this transform uses as output.
    *
    * @return the set of sources this transform uses as output.
    */
   Set<String> getOutputs();

   /**
    * Transforms the given named {@link Datum}
    *
    * @param datum the datum
    * @return the transformed datum
    */
   Datum transform(@NonNull Datum datum);

   /**
    * Transforms the {@link Datum} in the given {@link DataSet} and updates the {@link ObservationMetadata} for the
    * DataSet to reflect any changes.
    *
    * @param dataset The dataset to transform
    * @return The transformed dataset
    */
   DataSet transform(@NonNull DataSet dataset);
}//END OF Transform
