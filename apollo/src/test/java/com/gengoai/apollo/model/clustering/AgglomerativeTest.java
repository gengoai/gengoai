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

package com.gengoai.apollo.model.clustering;

import com.gengoai.apollo.evaluation.SilhouetteEvaluation;
import com.gengoai.conversion.Cast;

/**
 * @author David B. Bracewell
 */
public class AgglomerativeTest extends BaseClustererTest {

   public AgglomerativeTest() {
      super(new AgglomerativeClusterer());
   }

   @Override
   public Clustering convertClustering(Clusterer clustering) {
      Clustering c = clustering.getClustering();
      return Cast.<HierarchicalClustering>as(c).asFlat(4000);
   }

   @Override
   public boolean passes(SilhouetteEvaluation mce) {
      return mce.getAvgSilhouette() >= 0.85;
   }

}//END OF KMeansTest
