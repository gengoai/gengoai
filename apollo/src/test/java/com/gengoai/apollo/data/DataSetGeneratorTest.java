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
import com.gengoai.apollo.data.observation.VariableSequence;
import com.gengoai.string.Strings;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class DataSetGeneratorTest {

   @Test
   public void stringTest() {
      DataSetGenerator<String> generator = DataSetGenerator.<String>builder()
            .defaultInput(s -> Variable.binary(s.toLowerCase()))
            .source("chars",
                    Variable::binary,
                    s -> s.chars().mapToObj(Character::toString).collect(Collectors.toList()))
            .defaultOutput(s -> Variable.binary(Boolean.toString(Strings.isUpperCase(s))))
            .build();

      Datum d1 = generator.apply("alphabet");
      assertEquals(Variable.binary("alphabet"), d1.getDefaultInput());
      assertEquals(VariableSequence.from("a", "l", "p", "h", "a", "b", "e", "t"), d1.get("chars"));
      assertEquals(Variable.binary(Boolean.toString(Boolean.FALSE)), d1.getDefaultOutput());

      Datum d2 = generator.apply("Alphabet");
      assertEquals(Variable.binary("alphabet"), d2.getDefaultInput());
      assertEquals(VariableSequence.from("A", "l", "p", "h", "a", "b", "e", "t"), d2.get("chars"));
      assertEquals(Variable.binary(Boolean.toString(Boolean.FALSE)), d2.getDefaultOutput());


      Datum d3 = generator.apply("ALPHABET");
      assertEquals(Variable.binary("alphabet"), d3.getDefaultInput());
      assertEquals(VariableSequence.from("A", "L", "P", "H", "A", "B", "E", "T"), d3.get("chars"));
      assertEquals(Variable.binary(Boolean.toString(Boolean.TRUE)), d3.getDefaultOutput());
   }

   @Test
   public void test2() {
      DataSetGenerator<String> generator = DataSetGenerator.<String>builder()
            .defaultInput(Variable::binary,
                          s -> s.chars().mapToObj(Character::toString).collect(Collectors.toList()))
            .defaultOutput(Variable::binary,
                           s -> s.chars().mapToObj(Character::toString).collect(Collectors.toList()))
            .source("lw", s -> Variable.binary(s.toLowerCase()))
            .build();

      Datum d1 = generator.apply("alphabet");

      assertEquals(Variable.binary("alphabet"), d1.get("lw"));
      assertEquals(VariableSequence.from("a", "l", "p", "h", "a", "b", "e", "t"), d1.getDefaultInput());
      assertEquals(VariableSequence.from("a", "l", "p", "h", "a", "b", "e", "t"), d1.getDefaultOutput());

      Datum d2 = generator.apply("Alphabet");
      assertEquals(Variable.binary("alphabet"), d2.get("lw"));
      assertEquals(VariableSequence.from("A", "l", "p", "h", "a", "b", "e", "t"), d2.getDefaultInput());
      assertEquals(VariableSequence.from("A", "l", "p", "h", "a", "b", "e", "t"), d2.getDefaultOutput());


      Datum d3 = generator.apply("ALPHABET");
      assertEquals(Variable.binary("alphabet"), d3.get("lw"));
      assertEquals(VariableSequence.from("A", "L", "P", "H", "A", "B", "E", "T"), d3.getDefaultInput());
      assertEquals(VariableSequence.from("A", "L", "P", "H", "A", "B", "E", "T"), d3.getDefaultOutput());
   }
}
