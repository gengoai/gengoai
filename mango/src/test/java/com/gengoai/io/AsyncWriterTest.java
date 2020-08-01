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
import com.gengoai.string.Strings;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class AsyncWriterTest {

  private static List<String> randomList() {
    return IntStream.range(0, 100).mapToObj(i -> Strings.randomString(10, 'a', 'z')).collect(Collectors.toList());
  }

  @Test
  public void testWriter() throws Exception {
    Resource string = Resources.fromString("");
    List<String> list = randomList();
    try (final AsyncWriter writer = new AsyncWriter(string.writer())) {
      list.parallelStream().forEach((s -> {
        try {
          writer.write(s + "\n");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }));
    }

    List<String> lines = string.readLines();
    assertEquals(list.size(), lines.size());
    Collections.sort(list);
    Collections.sort(lines);
    assertEquals(list, lines);


  }
}//END OF AsyncWriterTest
