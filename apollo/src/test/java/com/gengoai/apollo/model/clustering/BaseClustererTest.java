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

import com.gengoai.apollo.data.CSVDataSetReader;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Schema;
import com.gengoai.apollo.data.ValueType;
import com.gengoai.apollo.data.transform.ReplaceNonFinite;
import com.gengoai.apollo.data.transform.Transformer;
import com.gengoai.apollo.data.transform.VectorAssembler;
import com.gengoai.apollo.evaluation.SilhouetteEvaluation;
import com.gengoai.io.CSV;
import com.gengoai.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author David B. Bracewell
 */
public abstract class BaseClustererTest {
   private final Clusterer clusterer;

   public BaseClustererTest(Clusterer clusterer) {
      this.clusterer = clusterer;
   }

   public Clustering convertClustering(Clusterer clustering) {
      return clustering.getClustering();
   }

   @Test
   public void fitAndEvaluate() {
      SilhouetteEvaluation evaluation = new SilhouetteEvaluation(clusterer.getFitParameters().measure.value());
      clusterer.estimate(loadWaterData());
      evaluation.evaluate(convertClustering(clusterer));
      assertTrue(passes(evaluation));
   }

   protected DataSet loadWaterData() {
      var csv = new CSVDataSetReader(CSV.csv(), Schema.schema(Map.of(
            "AutoColumn-0", ValueType.IGNORE
      )));
      try {
         //Read in the DataSet and remove the first column
         var ds = csv.read(Resources.fromClasspath("com/gengoai/apollo/ml/water-treatment.data"))
                     .probe()
                     .cache();
         var transformer = new Transformer(
               new ReplaceNonFinite(0),
               new VectorAssembler()
         );
         return transformer.fitAndTransform(ds);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected abstract boolean passes(SilhouetteEvaluation evaluation);

}//END OF BaseClustererTest
