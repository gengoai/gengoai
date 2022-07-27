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

package com.gengoai.apollo.data.transform;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.observation.Variable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.assertTrue;

public class BinTransformTest {

   @Test
   public void testBin() {
      List<Datum> data = new ArrayList<>();
      for (int i = 0; i < 100; i++) {
         data.add(Datum.of($(Datum.DEFAULT_INPUT, Variable.real("A", "B", Math.random()))));
         data.add(Datum.of($(Datum.DEFAULT_INPUT, Variable.real("A", "C", Math.random()))));
      }
      for (int i = 0; i < 100; i++) {
         data.add(Datum.of($(Datum.DEFAULT_INPUT, Variable.real("B", "B", Math.random()))));
         data.add(Datum.of($(Datum.DEFAULT_INPUT, Variable.real("B", "C", Math.random()))));
      }
      DataSet dataSet = DataSet.of(data);

      dataSet = new BinTransform(4, true).fitAndTransform(dataSet);
      for (Datum datum : dataSet) {
         Variable v = datum.getDefaultInput().asVariable();
         assertTrue(
               v.getSuffix().startsWith("B-Bin[")
                     || v.getSuffix().startsWith("C-Bin[")
         );
      }
   }

   @Test
   public void testBin2() {
      List<Datum> data = new ArrayList<>();
      for (int i = 0; i < 100; i++) {
         data.add(Datum.of($(Datum.DEFAULT_INPUT, Variable.real("A", "B", Math.random()))));
         data.add(Datum.of($(Datum.DEFAULT_INPUT, Variable.real("A", "C", Math.random()))));
      }
      for (int i = 0; i < 100; i++) {
         data.add(Datum.of($(Datum.DEFAULT_INPUT, Variable.real("B", "B", Math.random()))));
         data.add(Datum.of($(Datum.DEFAULT_INPUT, Variable.real("B", "C", Math.random()))));
      }
      DataSet dataSet = DataSet.of(data);

      dataSet = new BinTransform(4, false).fitAndTransform(dataSet);
      for (Datum datum : dataSet) {
         Variable v = datum.getDefaultInput().asVariable();
         assertTrue(
               v.getSuffix().startsWith("Bin[")
         );
      }
   }

}