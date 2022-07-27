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

package com.gengoai.apollo.model;

import com.gengoai.apollo.data.CSVDataSetReader;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.VariableNameSpace;
import com.gengoai.apollo.data.transform.Merge;
import com.gengoai.apollo.data.transform.StandardScalar;
import com.gengoai.apollo.data.transform.Transformer;
import com.gengoai.apollo.data.transform.VectorAssembler;
import com.gengoai.apollo.data.transform.vectorizer.CountVectorizer;
import com.gengoai.apollo.evaluation.RegressionEvaluation;
import com.gengoai.collection.Sets;
import com.gengoai.io.CSV;
import com.gengoai.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 * @author David B. Bracewell
 */
public abstract class BaseRegressionTest {
   private final Model regression;

   public BaseRegressionTest(Model regression) {
      this.regression = PipelineModel.builder()
                                     .transform(Merge.builder()
                                                     .inputs(List.of("Frequency",
                                                                     "Angle",
                                                                     "Chord",
                                                                     "Velocity",
                                                                     "Suction"))
                                                     .output(Datum.DEFAULT_INPUT)
                                                     .build())
                                     .defaultInput(new StandardScalar(VariableNameSpace.Prefix),
                                                   new CountVectorizer())
                                     .build(regression);
   }

   public DataSet airfoilDataset() {
      CSVDataSetReader csv = new CSVDataSetReader(CSV.builder()
                                                     .delimiter('\t')
                                                     .header("Frequency",
                                                             "Angle",
                                                             "Chord",
                                                             "Velocity",
                                                             "Suction",
                                                             "Pressure"));
      try {
         DataSet ds = csv.read(Resources.fromClasspath("com/gengoai/apollo/ml/airfoil_self_noise.data")).probe();
         Transformer transformer = new Transformer(List.of(
               new VectorAssembler(Sets.difference(ds.getMetadata().keySet(), Collections.singleton("Pressure")),
                                   Datum.DEFAULT_INPUT)));
         return transformer.fitAndTransform(ds);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Test
   public void fitAndEvaluate() {
      assertTrue(passes(RegressionEvaluation.crossValidation(airfoilDataset(),
                                                             regression,
                                                             Datum.DEFAULT_INPUT,
                                                             "Pressure",
                                                             3)));
   }

   public abstract boolean passes(RegressionEvaluation mce);
}//END OF BaseRegressionTest
