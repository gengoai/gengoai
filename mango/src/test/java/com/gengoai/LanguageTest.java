package com.gengoai;/*
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

import org.junit.Test;

import java.text.Collator;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class LanguageTest {

  @Test
  public void testCollator() throws Exception {
    Collator collator = Language.JAPANESE.getCollator();
    assertTrue(collator.equals("A", "Ａ"));
    assertFalse(collator.equals("a", "Ａ"));
    collator = Language.JAPANESE.getCollator(Collator.PRIMARY, Collator.NO_DECOMPOSITION);
    assertFalse(collator.equals("A", "Ａ"));
    assertTrue(collator.equals("a", "a"));
    collator = Language.JAPANESE.getCollator(Collator.PRIMARY);
    assertTrue(collator.equals("a", "Ａ"));
  }

  @Test
  public void testUsesWhitespace() throws Exception {
    assertFalse(Language.JAPANESE.usesWhitespace());
    assertFalse(Language.CHINESE.usesWhitespace());
    assertTrue(Language.ENGLISH.usesWhitespace());
  }

  @Test
  public void testIsRightToLeft() throws Exception {
    assertFalse(Language.JAPANESE.isRightToLeft());
    assertFalse(Language.CHINESE.isRightToLeft());
  }

  @Test
  public void testGetCode() throws Exception {
    assertEquals(Language.ENGLISH.getCode().toLowerCase(), "en");
    assertEquals(Language.CHINESE.getCode().toLowerCase(), "com.gengoai.hermes.zh");
  }

  @Test
  public void testAsLocale() throws Exception {
    assertEquals(Language.ENGLISH.asLocale(), Locale.US);
  }

  @Test
  public void testParseLanguage() throws Exception {
    assertEquals(Language.fromString("en"), Language.ENGLISH);
    assertEquals(Language.fromString("ja_JP"), Language.JAPANESE);
    assertEquals(Language.fromString("zh_cn"), Language.CHINESE);
    assertEquals(Language.fromString("zh_tw"), Language.CHINESE);
  }

  @Test
  public void testGetLocales() throws Exception {
    assertTrue(Language.JAPANESE.getLocales().contains(Locale.JAPAN));
  }


}
