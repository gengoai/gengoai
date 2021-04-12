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

import com.gengoai.io.CSV;
import com.gengoai.io.Resources;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CSVTest {

   @Test
   public void testSchema() throws Exception {
      String csv = "\"x\",\"z\",\"y\"\n" +
            "23, \"A\", \"ALPHA\"   \n" +
            "32, \"Z\", \"BETA\"";
      CSVDataSetReader reader = new CSVDataSetReader(CSV.csv().hasHeader(), Schema.schema(Map.of("x", ValueType.NUMERIC,
                                                                                                 "z", ValueType.CATEGORICAL,
                                                                                                 "y", ValueType.CATEGORICAL)));
      DataSet ds = reader.read(Resources.fromString(csv));
      List<Datum> data = ds.collect();

      assertEquals(23, data.get(0).get("x").asVariable().getValue(), 0d);
      assertEquals("z=A", data.get(0).get("z").asVariable().getName());
      assertEquals("y=ALPHA", data.get(0).get("y").asVariable().getName());


      assertEquals(32, data.get(1).get("x").asVariable().getValue(), 0d);
      assertEquals("z=Z", data.get(1).get("z").asVariable().getName());
      assertEquals("y=BETA", data.get(1).get("y").asVariable().getName());
   }

}
