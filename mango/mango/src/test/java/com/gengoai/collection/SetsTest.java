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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author David B. Bracewell
 */
public class SetsTest {

   @Test
   public void transform() throws Exception {
      assertEquals(Sets.hashSetOf("A", "B", "C"),
                   Sets.transform(Sets.hashSetOf("a", "b", "c"), String::toUpperCase)
                  );
      assertTrue(Sets.transform(Collections.<String>emptySet(), String::toUpperCase).isEmpty());
   }

   @Test
   public void difference() throws Exception {
      assertEquals(Sets.hashSetOf("A", "B", "C"),
                   Sets.difference(Sets.hashSetOf("A", "B", "C"), Sets.hashSetOf("G", "F", "D"))
                  );
      assertEquals(Sets.hashSetOf("A", "B", "C"),
                   Sets.difference(Sets.hashSetOf("A", "B", "C"), Collections.emptyList())
                  );
      assertTrue(Sets.difference(Collections.emptySet(), Sets.hashSetOf("A", "B", "C")).isEmpty());
   }

   @Test
   public void union() throws Exception {
      assertEquals(Sets.hashSetOf("A", "B", "C", "D", "F", "G"),
                   Sets.union(Sets.hashSetOf("A", "B", "C"), Sets.hashSetOf("G", "F", "D"))
                  );
      assertEquals(Sets.hashSetOf("A", "B", "C"),
                   Sets.union(Sets.hashSetOf("A", "B", "C"), Collections.emptyList())
                  );
      assertTrue(Sets.union(Collections.emptySet(), Collections.emptySet()).isEmpty());
   }

   @Test
   public void intersection() throws Exception {
      assertEquals(Sets.hashSetOf("A", "B", "C"),
                   Sets.intersection(Sets.hashSetOf("A", "B", "C"), Sets.hashSetOf("A", "B", "C", "G", "F", "D"))
                  );
      assertTrue(Sets.intersection(Sets.hashSetOf("A", "B", "C"), Collections.emptyList()).isEmpty());
      assertTrue(Sets.intersection(Collections.emptySet(), Collections.emptySet()).isEmpty());
   }

   @Test
   public void treeSet() throws Exception {
      assertEquals("[A, B, C]", Sets.sortedSetOf("A", "B", "C").toString());
      assertTrue(Sets.sortedSetOf().isEmpty());
   }

   @Test
   public void linkedHashSet() throws Exception {
      assertEquals("[A, B, C]", Sets.linkedHashSetOf("A", "B", "C").toString());
      assertTrue(Sets.linkedHashSetOf().isEmpty());
   }

   @Test
   public void concurrentSet() throws Exception {
      assertEquals(Sets.hashSetOf("A", "B", "C"), Sets.concurrentSetOf("A", "B", "C"));
      assertTrue(Sets.concurrentSetOf().isEmpty());
   }

   @Test
   public void asSet() throws Exception {
      assertEquals(Sets.hashSetOf("A", "B", "C"), Sets.asHashSet(Arrays.asList("A", "B", "C")));
      assertEquals(Sets.hashSetOf("A", "B", "C"), Sets.asHashSet(Stream.of("A", "B", "C")));
      assertEquals(Sets.hashSetOf("A", "B", "C"), Sets.asHashSet(Arrays.asList("A", "B", "C").iterator()));
      assertTrue(Sets.asHashSet(Collections.emptyList()).isEmpty());
      assertTrue(Sets.asHashSet(Collections.emptyIterator()).isEmpty());
   }

   @Test
   public void asConcurrentSet() throws Exception {
      assertEquals(Sets.hashSetOf("A", "B", "C"), Sets.asConcurrentHashSet(Arrays.asList("A", "B", "C")));
      assertEquals(Sets.hashSetOf("A", "B", "C"), Sets.asConcurrentHashSet(Stream.of("A", "B", "C")));
      assertEquals(Sets.hashSetOf("A", "B", "C"), Sets.asConcurrentHashSet(Arrays.asList("A", "B", "C").iterator()));
      assertTrue(Sets.asConcurrentHashSet(Collections.emptyList()).isEmpty());
      assertTrue(Sets.asConcurrentHashSet(Collections.emptyIterator()).isEmpty());
   }

   @Test
   public void asTreeSet() throws Exception {
      assertEquals("[A, B, C]", Sets.asTreeSet(Arrays.asList("A", "B", "C")).toString());
      assertEquals("[A, B, C]", Sets.asTreeSet(Stream.of("A", "B", "C")).toString());
      assertEquals("[A, B, C]", Sets.asTreeSet(Arrays.asList("A", "B", "C").iterator()).toString());
      assertTrue(Sets.asTreeSet(Collections.emptyList()).isEmpty());
      assertTrue(Sets.asTreeSet(Collections.emptyIterator()).isEmpty());
   }

   @Test
   public void asLinkedHashSet() throws Exception {
      assertEquals("[A, B, C]", Sets.asLinkedHashSet(Arrays.asList("A", "B", "C")).toString());
      assertEquals("[A, B, C]", Sets.asLinkedHashSet(Stream.of("A", "B", "C")).toString());
      assertEquals("[A, B, C]", Sets.asLinkedHashSet(Arrays.asList("A", "B", "C").iterator()).toString());
      assertTrue(Sets.asLinkedHashSet(Collections.emptyList()).isEmpty());
      assertTrue(Sets.asLinkedHashSet(Collections.emptyIterator()).isEmpty());

   }

}