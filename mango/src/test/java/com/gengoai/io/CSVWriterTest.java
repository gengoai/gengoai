/*
 * (c) 2005 David B. Bracewell
 *
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

package com.gengoai.io;

import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.gengoai.collection.Maps.mapOf;
import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class CSVWriterTest {


   @Test
   public void testReadWrite() throws Exception {


      Map<String, String> map = mapOf(LinkedHashMap::new,
                                      $("A", "1"),
                                      $("B", "2"));

      Resource r = new StringResource();
      try (CSVWriter writer = CSV.builder().delimiter('\t').writer(r)) {
         writer.write("1\"\t", "2", "3");
         writer.write(Arrays.asList("4", "5", "6"));
         writer.write(Arrays.asList("7", "8", "9").iterator());
         writer.write(map);
         writer.write(map, ':');
      }

      try (CSVReader reader = CSV.builder().delimiter('\t').reader(r)) {
         assertArrayEquals(new String[]{"1\"\t", "2", "3"}, reader.nextRow().toArray(new String[3]));
         assertArrayEquals(new String[]{"4", "5", "6"}, reader.nextRow().toArray(new String[3]));
         assertArrayEquals(new String[]{"7", "8", "9"}, reader.nextRow().toArray(new String[3]));
         assertArrayEquals(new String[]{"1", "2"}, reader.nextRow().toArray(new String[2]));
         assertArrayEquals(new String[]{"A:1", "B:2"}, reader.nextRow().toArray(new String[2]));
      }

      r = new StringResource();
      try (CSVWriter writer = CSV.builder().writer(r)) {
         writer.write("1\"\t", "2", "3");
         writer.write(Arrays.asList("4", "5", "6"));
         writer.write(Arrays.asList("7", "8", "9").iterator());
         writer.write(map);
         writer.write(map, ':');
      }

      try (CSVReader reader = CSV.builder().reader(r)) {
         assertArrayEquals(new String[]{"1\"\t", "2", "3"}, reader.nextRow().toArray(new String[3]));
         assertArrayEquals(new String[]{"4", "5", "6"}, reader.nextRow().toArray(new String[3]));
         assertArrayEquals(new String[]{"7", "8", "9"}, reader.nextRow().toArray(new String[3]));
         assertArrayEquals(new String[]{"1", "2"}, reader.nextRow().toArray(new String[2]));
         assertArrayEquals(new String[]{"A:1", "B:2"}, reader.nextRow().toArray(new String[2]));
      }


   }


}//END OF CSVWriterTest

