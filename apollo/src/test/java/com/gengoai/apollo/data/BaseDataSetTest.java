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
import com.gengoai.apollo.data.observation.VariableList;
import com.gengoai.io.Resources;
import com.gengoai.stream.Streams;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

public abstract class BaseDataSetTest {
   final DataSetType dataSetType;

   public BaseDataSetTest(DataSetType dataSetType) {
      this.dataSetType = dataSetType;
   }

   @Test
   public void batch() {
      DataSet data = dataSetType.create(dataSetType.getStreamingContext()
                                                   .stream(IntStream.range(0, 100)
                                                                    .mapToObj(i -> Datum.of($("A", Math.random())))));
      Iterator<DataSet> batchIterator = data.batchIterator(10);
      int count = 0;
      while (batchIterator.hasNext()) {
         batchIterator.next();
         count++;
      }
      assertEquals(10, count);
   }

   @Test
   public void cache() {
      DataSet dataSet = dataSetType.create(Streams.reusableStream(Datum.of($("A", 1.0),
                                                              $("B", List.of("A", "B", "C")),
                                                              $("C", $("word", 4)),
                                                              $("Y", "1")),
                                                     Datum.of($("A", 0.0),
                                                              $("B", List.of("A", "Z", "C")),
                                                              $("C", $("word", 3)),
                                                              $("Y", "0"))));
      DataSet p1 = dataSet.cache();
      assertEquals(dataSet.size(), p1.size());
   }

   @Test
   public void create() {
      DataSet data = dataSetType.create(Streams.reusableStream(Datum.of($("A", 1.0),
                                                                        $("B", List.of("A", "B", "C")),
                                                                        $("C", $("word", 4)),
                                                                        $("Y", "1")),
                                                               Datum.of($("A", 0.0),
                                                                        $("B", List.of("A", "Z", "C")),
                                                                        $("C", $("word", 3)),
                                                                        $("Y", "0"))));

      assertEquals(2, data.size());
      List<Datum> listOfDatum = data.collect();
      assertEquals(2, listOfDatum.size());
      assertEquals(Datum.of($("A", 1.0),
                            $("B", List.of("A", "B", "C")),
                            $("C", $("word", 4)),
                            $("Y", "1")),
                   listOfDatum.get(0));
      assertEquals(Datum.of($("A", 0.0),
                            $("B", List.of("A", "Z", "C")),
                            $("C", $("word", 3)),
                            $("Y", "0")),
                   listOfDatum.get(1));
   }

   @Test
   public void  probe() {
      DataSet dataSet = dataSetType.create(Streams.reusableStream(Datum.of($("A", 1.0),
                                                                           $("B", List.of("A", "B", "C")),
                                                                           $("C", $("word", 4)),
                                                                           $("Y", "1")),
                                                                  Datum.of($("A", 0.0),
                                                                           $("B", List.of("A", "Z", "C")),
                                                                           $("C", $("word", 3)),
                                                                           $("Y", "0"))));

      dataSet.probe();
      assertEquals(0, dataSet.getMetadata("A").dimension);
      assertEquals(Variable.class, dataSet.getMetadata("A").type);
      assertNull(dataSet.getMetadata("A").encoder);

      assertEquals(0, dataSet.getMetadata("B").dimension);
      assertEquals(VariableList.class, dataSet.getMetadata("B").type);
      assertNull(dataSet.getMetadata("B").encoder);

      assertEquals(0, dataSet.getMetadata("C").dimension);
      assertEquals(Variable.class, dataSet.getMetadata("C").type);
      assertNull(dataSet.getMetadata("C").encoder);

      assertEquals(0, dataSet.getMetadata("Y").dimension);
      assertEquals(Variable.class, dataSet.getMetadata("Y").type);
      assertNull(dataSet.getMetadata("Y").encoder);
   }

   @Test
   public void persist() {
      DataSet dataSet = dataSetType.create(Streams.reusableStream(Datum.of($("A", 1.0),
                                                              $("B", List.of("A", "B", "C")),
                                                              $("C", $("word", 4)),
                                                              $("Y", "1")),
                                                     Datum.of($("A", 0.0),
                                                              $("B", List.of("A", "Z", "C")),
                                                              $("C", $("word", 3)),
                                                              $("Y", "0"))));
      DataSet p1 = dataSet.persist();
      assertEquals(dataSet.size(), p1.size());
      DataSet p2 = dataSet.persist(Resources.temporaryFile());
      assertEquals(dataSet.size(), p2.size());
   }

   @Test
   public void take() {
      DataSet data = dataSetType.create(Streams.reusableStream(Datum.of($("A", 1.0),
                                                           $("B", List.of("A", "B", "C")),
                                                           $("C", $("word", 4)),
                                                           $("Y", "1")),
                                                  Datum.of($("A", 0.0),
                                                           $("B", List.of("A", "Z", "C")),
                                                           $("C", $("word", 3)),
                                                           $("Y", "0"))));

      List<Datum> take10 = data.take(10);
      assertEquals(2, take10.size());
   }

}