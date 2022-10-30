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

package com.gengoai.apollo.model.sequence;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.InMemoryDataSet;
import com.gengoai.apollo.data.observation.VariableSequence;
import com.gengoai.apollo.model.Model;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.assertTrue;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public abstract class BaseSequenceLabelerTest {
   final Model sequenceLabeler;
   DataSet data;

   public BaseSequenceLabelerTest(Model sequenceLabeler) {
      this.sequenceLabeler = sequenceLabeler;
      List<Datum> ex = new ArrayList<>();
      for (int i = 0; i < 200; i++) {
         ex.add(Datum.of($(Datum.DEFAULT_INPUT, VariableSequence.from("apples", "and", "oranges")),
                         $(Datum.DEFAULT_OUTPUT, VariableSequence.from("FRUIT", "O", "FRUIT"))));

         ex.add(Datum.of($(Datum.DEFAULT_INPUT, VariableSequence.from("apple", "and", "pcs")),
                         $(Datum.DEFAULT_OUTPUT, VariableSequence.from("COMPUTER", "O", "COMPUTER"))));

         ex.add(Datum.of($(Datum.DEFAULT_INPUT, VariableSequence.from("nuts", "and", "bolts")),
                         $(Datum.DEFAULT_OUTPUT, VariableSequence.from("PART", "O", "PART"))));
      }
      this.data = new InMemoryDataSet(ex);
   }

   @Test
   public void test() {
      sequenceLabeler.estimate(data);


      Datum pred = sequenceLabeler
            .transform(Datum.of($(Datum.DEFAULT_INPUT, VariableSequence.from("apples", "and", "oranges"))));

      double correct = 0;
      VariableSequence y = pred.getDefaultOutput().asVariableSequence();
      if ("FRUIT".equals(y.get(0).getName())) correct++;
      if ("O".equals(y.get(1).getName())) correct++;
      if ("FRUIT".equals(y.get(2).getName())) correct++;


      pred = sequenceLabeler
            .transform(Datum.of($(Datum.DEFAULT_INPUT, VariableSequence.from("nuts", "and", "bolts"))));
      y = pred.getDefaultOutput().asVariableSequence();
      if ("PART".equals(y.get(0).getName())) correct++;
      if ("O".equals(y.get(1).getName())) correct++;
      if ("PART".equals(y.get(2).getName())) correct++;


      pred = sequenceLabeler
            .transform(Datum.of($(Datum.DEFAULT_INPUT, VariableSequence.from("apple", "and", "pcs"))));
      y = pred.getDefaultOutput().asVariableSequence();
      if ("COMPUTER".equals(y.get(0).getName())) correct++;
      if ("COMPUTER".equals(y.get(1).getName())) correct++;
      if ("COMPUTER".equals(y.get(2).getName())) correct++;


      assertTrue(correct >= 5);
   }

}//END OF BaseSequenceLabelerTest
