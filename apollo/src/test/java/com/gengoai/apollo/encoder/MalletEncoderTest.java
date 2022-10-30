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

import cc.mallet.types.Alphabet;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Variable;
import org.junit.Test;

import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MalletEncoderTest {

   @Test
   public void test() {
      DataSet dataSet = DataSet.of(
            Stream.concat(
                  IntStream.range(0, 20)
                           .mapToObj(i -> Datum.of($(Datum.DEFAULT_INPUT, Variable.binary("A")),
                                                   $(Datum.DEFAULT_OUTPUT, Variable.binary("true")))),
                  IntStream.range(0, 10)
                           .mapToObj(i -> Datum.of($(Datum.DEFAULT_INPUT, Variable.binary("B")),
                                                   $(Datum.DEFAULT_OUTPUT, Variable.binary("false"))))
            ));

      Encoder encoder = new MalletEncoder(new Alphabet(new Object[]{"false", "true"}));
      encoder.fit(dataSet.stream().map(Datum::getDefaultOutput));

      for (Datum datum : dataSet) {
         String v = datum.getDefaultOutput().asVariable().getName();
         if (v.equals("false")) {
            assertEquals(0, encoder.encode(v));
            assertEquals("false", encoder.decode(encoder.encode(v)));
         } else {
            assertEquals(1, encoder.encode(v));
            assertEquals("true", encoder.decode(encoder.encode(v)));
         }
      }

      assertEquals(-1, encoder.encode("not so true"));
      assertTrue(encoder.isFixed());
      assertEquals(Set.of("false", "true"), encoder.getAlphabet());
      assertEquals(2, encoder.size());
   }

}//END OF FixedEncoderTest
