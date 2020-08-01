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

import com.gengoai.collection.counter.Counter;
import com.gengoai.string.Strings;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

/**
 * <p>
 * A topic is defined as distribution over features and is associated with an id. Optionally, a name can be specified
 * for each topic to make it more human understandable.
 * </p>
 *
 * @author David B. Bracewell
 */
@Data
public class Topic implements Serializable {
   private static final long serialVersionUID = 1L;
   private final int id;
   private final Counter<String> featureDistribution;
   private String name = Strings.EMPTY;

   /**
    * Instantiates a new Topic.
    *
    * @param id                  the id
    * @param featureDistribution the feature distribution
    */
   public Topic(int id, @NonNull Counter<String> featureDistribution) {
      this.id = id;
      this.featureDistribution = featureDistribution;
   }

}//END OF Topic
