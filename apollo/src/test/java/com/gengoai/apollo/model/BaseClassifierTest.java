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
import com.gengoai.apollo.data.transform.Merge;
import com.gengoai.apollo.data.transform.Transform;
import com.gengoai.apollo.data.transform.Transformer;
import com.gengoai.apollo.data.transform.VectorAssembler;
import com.gengoai.apollo.data.transform.vectorizer.IndexingVectorizer;
import com.gengoai.apollo.evaluation.ClassifierEvaluation;
import com.gengoai.apollo.evaluation.MultiClassEvaluation;
import com.gengoai.collection.Sets;
import com.gengoai.io.CSV;
import com.gengoai.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author David B. Bracewell
 */
public abstract class BaseClassifierTest {

   private final Model classifier;
   private final boolean merge;

   public BaseClassifierTest(Model classifier, boolean merge) {
      if(merge) {
         this.classifier = classifier;
      } else {
         this.classifier = PipelineModel.builder()
                                        .source("class", new IndexingVectorizer())
                                        .build(classifier);
      }
      this.merge = merge;
   }

   @Test
   public void fitAndEvaluate() {
      assertTrue(passes(MultiClassEvaluation.crossvalidation(irisDataset(),
                                                             classifier,
                                                             10,
                                                             "class")));
   }

   public DataSet irisDataset() {
      CSVDataSetReader csv = new CSVDataSetReader(CSV.builder().hasHeader(true));
      try {
         DataSet ds = csv.read(Resources.fromClasspath("com/gengoai/apollo/ml/iris.csv"));
         ds.probe();
         List<Transform> transforms = new ArrayList<>();
         if(merge) {
            transforms.add(Merge.builder()
                                .inputs(Sets.difference(ds.getMetadata().keySet(),
                                                        Collections.singleton("class")))
                                .output(Datum.DEFAULT_INPUT)
                                .build());
         } else {
            transforms.add(new VectorAssembler(Sets.difference(ds.getMetadata().keySet(),
                                                               Collections.singleton("class")), Datum.DEFAULT_INPUT));
         }
         return new Transformer(transforms).fitAndTransform(ds);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   public abstract boolean passes(ClassifierEvaluation mce);

}//END OF BaseClassifierTest
