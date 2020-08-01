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

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.ObservationMetadata;
import com.gengoai.apollo.ml.StreamingDataSet;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * A {@link Transform} composed of an ordered set of other transforms. The transformer tracks the metadata during the
 * fitting process to make available to other resources.
 * </p>
 *
 * @author David B. Bracewell
 */
public class Transformer implements Serializable, Transform {
   private static final long serialVersionUID = 1L;
   @Getter
   private final List<Transform> transforms;
   @Getter
   private final Map<String, ObservationMetadata> metadata = new HashMap<>();

   /**
    * Instantiates a new Transformer.
    *
    * @param transforms the transforms
    */
   public Transformer(@NonNull List<? extends Transform> transforms) {
      this.transforms = transforms.stream().map(Transform::copy).collect(Collectors.toList());
   }

   @Override
   public Transformer copy() {
      return new Transformer(transforms);
   }

   @Override
   public DataSet fitAndTransform(DataSet dataset) {
      DataSet temp = dataset;
      if(dataset instanceof StreamingDataSet) {
         temp = temp.map(Datum::copy);
      }
      for(Transform transform : transforms) {
         temp = transform.fitAndTransform(temp);
      }
      metadata.clear();
      metadata.putAll(temp.getMetadata());
      return temp;
   }

   @Override
   public Set<String> getInputs() {
      return transforms.stream().flatMap(d -> d.getInputs().stream()).collect(Collectors.toSet());
   }

   @Override
   public Set<String> getOutputs() {
      return transforms.stream().flatMap(d -> d.getOutputs().stream()).collect(Collectors.toSet());
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      DataSet out = dataset;
      for(Transform transform : transforms) {
         out = transform.transform(out);
      }
      return out;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      Datum d = datum.copy();
      for(Transform transform : transforms) {
         d = transform.transform(d);
      }
      return d;
   }

}//END OF Transformer
