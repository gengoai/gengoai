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

package com.gengoai.collection;

import com.gengoai.collection.tree.Trie;
import com.gengoai.collection.tree.TrieMatch;
import com.gengoai.string.CharMatcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class TrieTest {

   Trie<String> trie;


   @Before
   public void setUp() throws Exception {
      trie = new Trie<>();
      trie.put("ran", "run");
      trie.put("rand", "rand");
      trie.put("was", "is");
      trie.put("wasn't", "is");
      trie.put("isn't", "is");
   }

   @Test
   public void findAll() throws Exception {
      String content = "I ran the rand program and wasn't happy";
      List<TrieMatch<String>> matches = trie.find(content, CharMatcher.WhiteSpace);
      assertEquals(3, matches.size());

      assertEquals("run", matches.get(0).value);
      assertEquals("ran", matches.get(0).getMatch(content));

      assertEquals("rand", matches.get(1).value);
      assertEquals("rand", matches.get(1).getMatch(content));

      assertEquals("is", matches.get(2).value);
      assertEquals("wasn't", matches.get(2).getMatch(content));
   }

   @Test
   public void size() throws Exception {
      assertEquals(5, trie.size());
   }

   @Test
   public void isEmpty() throws Exception {
      assertFalse(trie.isEmpty());
   }

   @Test
   public void containsKey() throws Exception {
      assertTrue(trie.containsKey("ran"));
      assertFalse(trie.containsKey(null));
      assertFalse(trie.containsKey("randell"));
   }

   @Test
   public void containsValue() throws Exception {
      assertTrue(trie.containsValue("is"));
      assertFalse(trie.containsValue(null));
      assertFalse(trie.containsValue("go"));
   }

   @Test
   public void get() throws Exception {
      assertEquals("is", trie.get("wasn't"));
      assertEquals("is", trie.get("was"));
      assertNull(trie.get(null));
      assertNull(trie.get("abba"));
   }

   @Test
   public void prefix() throws Exception {
      Map<String, String> prefix = trie.prefix("ran");

      assertEquals(2, prefix.size());
      assertEquals(Sets.hashSetOf("ran", "rand"), prefix.keySet());
   }

   @Test
   public void keySet() throws Exception {
      assertEquals(Sets.hashSetOf("ran", "rand", "was", "wasn't", "isn't"), trie.keySet());
      assertEquals(Sets.hashSetOf("was", "wasn't"), trie.prefix("was").keySet());
      assertTrue(trie.prefix("zeb").keySet().isEmpty());
      assertTrue(new Trie<String>().keySet().isEmpty());
   }

   @Test
   public void values() throws Exception {
      Collection<String> values = trie.values();
      assertEquals(1, values.stream().filter(s -> s.equals("run")).count());
      assertEquals(1, values.stream().filter(s -> s.equals("rand")).count());
      assertEquals(3, values.stream().filter(s -> s.equals("is")).count());
   }

   @Test
   public void entrySet() throws Exception {
      Set<Map.Entry<String, String>> entries = trie.entrySet();
      assertEquals(5, entries.size());
      assertTrue(entries.contains($("ran", "run")));
      assertTrue(entries.contains($("rand", "rand")));
      assertTrue(entries.contains($("was", "is")));
      assertTrue(entries.contains($("wasn't", "is")));
      assertTrue(entries.contains($("isn't", "is")));

      Map.Entry<String, String> first = entries.iterator().next();
      String key = first.getKey();
      first.setValue("ABBA");
      assertEquals("ABBA", trie.get(key));

   }

   @Test
   public void put() throws Exception {
      assertEquals("run", trie.put("ran", null));


      Map<String, String> prefix = trie.prefix("ran");

      assertEquals(2, prefix.size());
      assertEquals(Sets.hashSetOf("ran", "rand"), prefix.keySet());
   }

   @Test
   public void remove() throws Exception {
      assertEquals("rand", trie.remove("rand"));
      assertEquals(4, trie.size());
   }

   @Test
   public void putAll() throws Exception {
      trie.putAll(hashMapOf($("goes", "go"), $("went", "go")));
      assertEquals(7, trie.size());
      assertEquals("go", trie.get("goes"));
      assertEquals("go", trie.get("went"));
   }

   @Test
   public void clear() throws Exception {
      trie.clear();
      assertTrue(trie.isEmpty());
   }

}