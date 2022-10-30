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
import com.gengoai.apollo.data.observation.Observation;
import com.gengoai.apollo.data.observation.Variable;
import org.junit.Test;

import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.assertEquals;

public class UnderSamplingTest {

   @Test
   public void sample() {
      DataSet dataSet = DataSet.of(
            Stream.concat(
                  IntStream.range(0, 20)
                           .mapToObj(i -> Datum.of($(Datum.DEFAULT_INPUT, Variable.binary("A")),
                                                   $(Datum.DEFAULT_OUTPUT, Variable.binary("ALPHA")))),
                  IntStream.range(0, 10)
                           .mapToObj(i -> Datum.of($(Datum.DEFAULT_INPUT, Variable.binary("B")),
                                                   $(Datum.DEFAULT_OUTPUT, Variable.binary("BETA"))))
            ));

      UnderSampling underSampling = new UnderSampling(Datum.DEFAULT_OUTPUT);
      dataSet = underSampling.sample(dataSet);

      Map<String,Long> counts = dataSet.stream()
                                       .map(Datum::getDefaultOutput)
                                       .map(Observation::asVariable)
                                       .map(Variable::getSuffix)
                                       .countByValue();

      assertEquals(10L, counts.get("ALPHA"), 1);
      assertEquals(10L, counts.get("BETA"), 1);
   }

}
