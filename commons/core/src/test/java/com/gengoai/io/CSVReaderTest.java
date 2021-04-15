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

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class CSVReaderTest {


  @Test
  public void processRowTest() throws IOException {
    List<String> names = CSV.builder()
      .hasHeader()
      .reader(Resources.fromClasspath("com/gengoai//Schools.csv"))
      .processRows(m -> Optional.of(m.get(1)));
    assertEquals(names.get(0), "Abilene Christian University");
    assertEquals(names.get(2), "Adrian College");
    assertEquals(names.get(4), "University of Akron");
    assertEquals(names.get(names.size() - 1), "Youngstown State University");
  }

  @Test
  public void headerTest() throws IOException {
    CSVReader reader = CSV.builder()
                          .hasHeader()
                          .reader(Resources.fromClasspath("com/gengoai//Schools.csv"));
    assertEquals(
      Arrays.asList("schoolID", "schoolName", "schoolCity", "schoolState", "schoolNick"),
      reader.getHeader()
    );
    reader.close();
  }


  @Test
  public void readAllTest() throws IOException {
    List<List<String>> rows = CSV.builder()
      .hasHeader()
      .reader(Resources.fromClasspath("com/gengoai//Schools.csv"))
      .readAll();
    assertEquals(rows.get(0).get(1), "Abilene Christian University");
    assertEquals(rows.get(2).get(1), "Adrian College");
    assertEquals(rows.get(4).get(1), "University of Akron");
    assertEquals(rows.get(rows.size() - 1).get(1), "Youngstown State University");
  }

  @Test
  public void readMessy() throws IOException {
    List<List<String>> rows = CSV.builder()
      .reader(Resources.fromClasspath("com/gengoai//messy.csv"))
      .readAll();
    assertEquals(2, rows.size());
    assertEquals("row 1\n" +
      "is great", rows.get(0).get(0));
    assertEquals("row\"row", rows.get(0).get(2));
    assertEquals("#", rows.get(1).get(1));
  }

}