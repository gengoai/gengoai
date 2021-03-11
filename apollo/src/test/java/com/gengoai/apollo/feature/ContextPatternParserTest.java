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

package com.gengoai.apollo.feature;

import com.gengoai.conversion.Cast;
import org.junit.Test;

import static org.junit.Assert.*;

public class ContextPatternParserTest {

   @Test
   public void testNGram() {
      var p1 = "<1,2>WORD[-1,0]|POS[0]";
      var features = ContextPatternParser.parse(p1);
      assertEquals(3, features.size());
      var f1 = features.get(0);
      ContextFeaturizerImpl<?> impl = Cast.as(f1);
      assertFalse(impl.isIgnoreEmptyContext());
      assertEquals("WORD[-1]|POS[0]", impl.getFeaturePrefix());
      var patterns = impl.getPatterns();
      assertEquals(-1, patterns.get(0).getOffset());
      assertEquals("WORD", patterns.get(0).getPrefix());
      assertEquals(0, patterns.get(1).getOffset());
      assertEquals("POS", patterns.get(1).getPrefix());


      var f2 = features.get(1);
      impl = Cast.as(f2);
      assertFalse(impl.isIgnoreEmptyContext());
      assertEquals("WORD[-1]|WORD[0]|POS[0]", impl.getFeaturePrefix());
      patterns = impl.getPatterns();
      assertEquals(-1, patterns.get(0).getOffset());
      assertEquals("WORD", patterns.get(0).getPrefix());
      assertEquals(0, patterns.get(1).getOffset());
      assertEquals("WORD", patterns.get(1).getPrefix());
      assertEquals(0, patterns.get(2).getOffset());
      assertEquals("POS", patterns.get(2).getPrefix());


      var f3 = features.get(2);
      impl = Cast.as(f3);
      assertFalse(impl.isIgnoreEmptyContext());
      assertEquals("WORD[0]|POS[0]", impl.getFeaturePrefix());
      patterns = impl.getPatterns();
      assertEquals(0, patterns.get(0).getOffset());
      assertEquals("WORD", patterns.get(0).getPrefix());
      assertEquals(0, patterns.get(1).getOffset());
      assertEquals("POS", patterns.get(1).getPrefix());
   }

   @Test
   public void testPREFIX() {
      var p1 = "WORD[-1,0]|POS[0]";
      var features = ContextPatternParser.parse(p1);
      assertEquals(2, features.size());


      var f1 = features.get(0);
      ContextFeaturizerImpl<?> impl = Cast.as(f1);
      assertFalse(impl.isIgnoreEmptyContext());
      assertEquals("WORD[-1]|POS[0]", impl.getFeaturePrefix());
      var patterns = impl.getPatterns();
      assertEquals(-1, patterns.get(0).getOffset());
      assertEquals("WORD", patterns.get(0).getPrefix());
      assertEquals(0, patterns.get(1).getOffset());
      assertEquals("POS", patterns.get(1).getPrefix());


      var f2 = features.get(1);
      impl = Cast.as(f2);
      assertFalse(impl.isIgnoreEmptyContext());
      assertEquals("WORD[0]|POS[0]", impl.getFeaturePrefix());
      patterns = impl.getPatterns();
      assertEquals(0, patterns.get(0).getOffset());
      assertEquals("WORD", patterns.get(0).getPrefix());
      assertEquals(0, patterns.get(1).getOffset());
      assertEquals("POS", patterns.get(1).getPrefix());
   }

   @Test
   public void testSimple() {
      var p1 = "WORD[-1]|POS[0]";
      var features = ContextPatternParser.parse(p1);
      assertEquals(1, features.size());
      var f1 = features.get(0);
      ContextFeaturizerImpl<?> impl = Cast.as(f1);
      assertFalse(impl.isIgnoreEmptyContext());
      assertEquals("WORD[-1]|POS[0]", impl.getFeaturePrefix());
      var patterns = impl.getPatterns();
      assertEquals(-1, patterns.get(0).getOffset());
      assertEquals("WORD", patterns.get(0).getPrefix());
      assertEquals(0, patterns.get(1).getOffset());
      assertEquals("POS", patterns.get(1).getPrefix());
   }

   @Test
   public void testStrict() {
      var p1 = "~WORD[-1]|WORD[0]";
      var features = ContextPatternParser.parse(p1);
      assertEquals(1, features.size());
      var f1 = features.get(0);
      ContextFeaturizerImpl<?> impl = Cast.as(f1);
      assertTrue(impl.isIgnoreEmptyContext());
      assertEquals("WORD[-1]|WORD[0]", impl.getFeaturePrefix());
      var patterns = impl.getPatterns();
      assertEquals(-1, patterns.get(0).getOffset());
      assertEquals("WORD", patterns.get(0).getPrefix());
      assertEquals(0, patterns.get(1).getOffset());
      assertEquals("WORD", patterns.get(1).getPrefix());
   }

}