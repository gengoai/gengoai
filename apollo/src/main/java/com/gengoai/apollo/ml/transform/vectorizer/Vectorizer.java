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

package com.gengoai.apollo.ml.transform.vectorizer;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.transform.AbstractSingleSourceTransform;
import com.gengoai.apollo.ml.transform.Transform;
import com.gengoai.stream.MStream;
import lombok.Getter;
import lombok.NonNull;

/**
 * <p>
 * Base class for specialized {@link Transform}s that converts {@link Observation}s into {@link NDArray} observations.
 * Each vectorizer has an associated {@link Encoder} that learns the mapping of variable names into NDArray indexes.
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class Vectorizer<T extends Vectorizer<T>> extends AbstractSingleSourceTransform<T> {
   private static final long serialVersionUID = 1L;
   @Getter
   protected final Encoder encoder;

   protected Vectorizer(@NonNull Encoder encoder) {
      this.encoder = encoder;
   }

   @Override
   protected void fit(@NonNull MStream<Observation> observations) {
      if(!encoder.isFixed()) {
         encoder.fit(observations);
      }
   }

   @Override
   protected void updateMetadata(@NonNull DataSet dataset) {
      dataset.updateMetadata(output, m -> {
         m.setDimension(encoder.size());
         m.setType(NDArray.class);
         m.setEncoder(encoder);
      });
   }

}//END OF Vectorizer
