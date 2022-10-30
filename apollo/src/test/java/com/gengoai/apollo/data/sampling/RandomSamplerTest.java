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

package com.gengoai.apollo.data.sampling;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.stream.StreamingContext;
import org.junit.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.assertEquals;

public class RandomSamplerTest {

   @Test
   public void sampleDist() {
      DataSet dataSet = DataSet.of(StreamingContext.distributed()
                                                   .range(0, 100)
                                                   .map(i -> Datum.of($(Datum.DEFAULT_INPUT, Variable.binary("A")),
                                                                      $(Datum.DEFAULT_OUTPUT, Variable
                                                                            .binary("ALPHA")))));

      RandomSampler randomSampler = new RandomSampler();
      randomSampler.setSampleSize(15);
      randomSampler.setWithReplacement(false);
      dataSet = randomSampler.sample(dataSet);

      assertEquals(15, dataSet.size(), 0d);

   }

   @Test
   public void sampleLocal() {
      DataSet dataSet = DataSet.of(
            Stream.concat(
                  IntStream.range(0, 20)
                           .mapToObj(i -> Datum.of($(Datum.DEFAULT_INPUT, Variable.binary("A")),
                                                   $(Datum.DEFAULT_OUTPUT, Variable.binary("ALPHA")))),
                  IntStream.range(0, 10)
                           .mapToObj(i -> Datum.of($(Datum.DEFAULT_INPUT, Variable.binary("B")),
                                                   $(Datum.DEFAULT_OUTPUT, Variable.binary("BETA"))))
            ));

      RandomSampler randomSampler = new RandomSampler();
      randomSampler.setSampleSize(15);
      randomSampler.setWithReplacement(false);
      dataSet = randomSampler.sample(dataSet);

      assertEquals(15, dataSet.size(), 0d);

   }

}
