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

package com.gengoai.apollo.data;

import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.stream.StreamingContext;
import com.gengoai.string.Strings;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class SplitTest {
   DataSet dataSet;

   @Test(expected = IllegalArgumentException.class)
   public void badPctSplit() {
      Split split = Split.createTrainTestSplit(dataSet, 8);
   }

   @Test
   public void fold() {
      Split[] folds = Split.createFolds(dataSet, 10);
      for (Split fold : folds) {
         assertEquals(900, fold.train.size());
         assertEquals(100, fold.test.size());
      }

   }

   @Before
   public void init() {
      dataSet = DataSetGenerator
            .<String>builder()
            .defaultInput(Variable::binary)
            .build()
            .generate(StreamingContext.local()
                                      .stream(IntStream.range(0, 1000).mapToObj(i -> Strings.randomHexString(3))));
   }

   @Test
   public void pctSplit() {
      Split split = Split.createTrainTestSplit(dataSet, 0.8);
      assertEquals(800, split.train.size());
      assertEquals(200, split.test.size());
   }

}
