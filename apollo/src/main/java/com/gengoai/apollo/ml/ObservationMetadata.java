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

package com.gengoai.apollo.ml;

import com.gengoai.Copyable;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.observation.Observation;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>Metadata about an {@link Observation} source detailing the type (Class information for the Observation), encoder
 * (if the Observation has been encoded), and a dimension (number of values).</p>
 *
 * @author David B. Bracewell
 */
@Data
public class ObservationMetadata implements Serializable, Copyable<ObservationMetadata> {
   /**
    * The Dimension.
    */
   long dimension;
   /**
    * The Type.
    */
   Class<? extends Observation> type;
   /**
    * The Encoder.
    */
   Encoder encoder;

   @Override
   public ObservationMetadata copy() {
      return Copyable.deepCopy(this);
   }
}//END OF ObservationMetadata
