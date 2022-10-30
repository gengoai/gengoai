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

package com.gengoai.apollo.encoder;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.string.Strings;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

public class HashingEncoderTest {

   @Test
   public void test() {
      DataSet dataSet = DataSet.of(
            IntStream.range(0, 200).mapToObj(i -> Datum.of($(Datum.DEFAULT_INPUT,
                                                             Variable.binary(Strings.randomHexString(4))))));

      HashingEncoder encoder = new HashingEncoder(10);
      encoder.fit(dataSet.stream().map(Datum::getDefaultOutput));


      Map<Integer, Long> counts = dataSet.stream()
                                         .map(Datum::getDefaultInput)
                                         .map(o -> o.asVariable().getName())
                                         .map(encoder::encode)
                                         .countByValue();

      assertEquals(10, counts.size());
      assertNull(encoder.decode(3000));
      assertEquals("Hash(3)", encoder.decode(3));
      assertTrue(encoder.isFixed());
      assertEquals(IntStream.range(0, 10)
                            .mapToObj(i -> "Hash(" + i + ")")
                            .collect(Collectors.toSet()), encoder.getAlphabet());
      assertEquals(10, encoder.size());

   }

}//END OF BinaryEncoderTest
