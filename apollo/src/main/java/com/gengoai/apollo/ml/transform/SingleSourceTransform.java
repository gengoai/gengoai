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

import com.gengoai.apollo.ml.observation.Observation;
import lombok.NonNull;

/**
 * <p>
 * Definition of a {@link Transform} in which there is a single input {@link Observation} and a single output {@link
 * Observation}. The input and output are specified via the {@link #input(String)} and {@link #output(String)} methods
 * respectively. For convenience, a {@link #source(String)} method exists that will set the input and output source to
 * be the same.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface SingleSourceTransform extends Transform {
   /**
    * Fluent setter of the input source name. Note: if the  output source is blank / empty it will set the output source
    * as well.
    *
    * @param name the name of the input source
    * @return this transform
    */
   SingleSourceTransform input(@NonNull String name);

   /**
    * Fluent setter of the output source name
    *
    * @param name the name of the output source
    * @return this transform
    */
   SingleSourceTransform output(@NonNull String name);

   /**
    * Fluent setter that sets both the input and output to the given name.
    *
    * @param name the name of the input/output
    * @return this transform
    */
   SingleSourceTransform source(@NonNull String name);

}//END OF SingleSourceTransform
