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

import com.gengoai.apollo.data.observation.VariableList;
import com.gengoai.conversion.Cast;
import com.gengoai.io.Resources;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LibSVMDataSetReaderTest {

   @Test
   public void binary() throws Exception {
      String str = "+1 0:23 3:45 10:23\n"
            + "1 1:6 3:43 9:2\n"
            + "-1 0:90 2:34 5:23\n"
            + "-1 1:90 2:34 4:33 7:32\n";
      LibSVMDataSetReader reader = new LibSVMDataSetReader(false);
      List<Datum> data = reader.read(Resources.fromString(str)).collect();

      assertEquals("true", data.get(0).getDefaultOutput().asVariable().getSuffix());
      assertEquals("true", data.get(1).getDefaultOutput().asVariable().getSuffix());
      assertEquals("false", data.get(2).getDefaultOutput().asVariable().getSuffix());
      assertEquals("false", data.get(3).getDefaultOutput().asVariable().getSuffix());

      VariableList vl0 = Cast.as(data.get(0).getDefaultInput().asVariableCollection());
      assertEquals(23, vl0.get(0).getValue(), 0d);
      assertEquals(45, vl0.get(1).getValue(), 0d);
      assertEquals(23, vl0.get(2).getValue(), 0d);

      VariableList vl1 = Cast.as(data.get(1).getDefaultInput().asVariableCollection());
      assertEquals(6, vl1.get(0).getValue(), 0d);
      assertEquals(43, vl1.get(1).getValue(), 0d);
      assertEquals(2, vl1.get(2).getValue(), 0d);

      VariableList vl2 = Cast.as(data.get(2).getDefaultInput().asVariableCollection());
      assertEquals(90, vl2.get(0).getValue(), 0d);
      assertEquals(34, vl2.get(1).getValue(), 0d);
      assertEquals(23, vl2.get(2).getValue(), 0d);


      VariableList vl3 = Cast.as(data.get(3).getDefaultInput().asVariableCollection());
      assertEquals(90, vl3.get(0).getValue(), 0d);
      assertEquals(34, vl3.get(1).getValue(), 0d);
      assertEquals(33, vl3.get(2).getValue(), 0d);
      assertEquals(32, vl3.get(3).getValue(), 0d);


   }

   @Test
   public void multiClass() throws Exception {
      String str = "A 0:23 3:45 10:23\n"
            + "B 1:6 3:43 9:2\n"
            + "C 0:90 2:34 5:23\n"
            + "D 1:90 2:34 4:33 7:32\n";
      LibSVMDataSetReader reader = new LibSVMDataSetReader(false);
      List<Datum> data = reader.read(Resources.fromString(str)).collect();

      assertEquals("A", data.get(0).getDefaultOutput().asVariable().getSuffix());
      assertEquals("B", data.get(1).getDefaultOutput().asVariable().getSuffix());
      assertEquals("C", data.get(2).getDefaultOutput().asVariable().getSuffix());
      assertEquals("D", data.get(3).getDefaultOutput().asVariable().getSuffix());

      VariableList vl0 = Cast.as(data.get(0).getDefaultInput().asVariableCollection());
      assertEquals(23, vl0.get(0).getValue(), 0d);
      assertEquals(45, vl0.get(1).getValue(), 0d);
      assertEquals(23, vl0.get(2).getValue(), 0d);

      VariableList vl1 = Cast.as(data.get(1).getDefaultInput().asVariableCollection());
      assertEquals(6, vl1.get(0).getValue(), 0d);
      assertEquals(43, vl1.get(1).getValue(), 0d);
      assertEquals(2, vl1.get(2).getValue(), 0d);

      VariableList vl2 = Cast.as(data.get(2).getDefaultInput().asVariableCollection());
      assertEquals(90, vl2.get(0).getValue(), 0d);
      assertEquals(34, vl2.get(1).getValue(), 0d);
      assertEquals(23, vl2.get(2).getValue(), 0d);


      VariableList vl3 = Cast.as(data.get(3).getDefaultInput().asVariableCollection());
      assertEquals(90, vl3.get(0).getValue(), 0d);
      assertEquals(34, vl3.get(1).getValue(), 0d);
      assertEquals(33, vl3.get(2).getValue(), 0d);
      assertEquals(32, vl3.get(3).getValue(), 0d);


   }

}
