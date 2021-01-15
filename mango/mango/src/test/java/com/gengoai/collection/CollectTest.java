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

import java.util.*;
import java.util.stream.Stream;

import static com.gengoai.collection.Lists.arrayListOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class CollectTest {


   @Test
   public void addAll() {
      List<String> list = new ArrayList<>();
      Collect.addAll(list, Arrays.asList("A", "B"));
      assertEquals(arrayListOf("A", "B"), list);


      Collect.addAll(list, Arrays.asList("A", "B").iterator());
      assertEquals(arrayListOf("A", "B", "A", "B"), list);


      Collect.addAll(list, Stream.of("A", "B"));
      assertEquals(arrayListOf("A", "B", "A", "B", "A", "B"), list);


      assertEquals(list, Collect.asCollection(list));
      assertEquals(list.toString(),
                   Iterables.asIterable(arrayListOf("A", "B", "A", "B", "A", "B").iterator()).toString());
   }


   @Test
   public void create() throws Exception {
      assertNull(Collect.newCollection(null));
      assertTrue(Collect.newCollection(List.class) instanceof ArrayList);
      assertTrue(Collect.newCollection(Set.class) instanceof HashSet);
      assertTrue(Collect.newCollection(NavigableSet.class) instanceof TreeSet);
      assertTrue(Collect.newCollection(Queue.class) instanceof LinkedList);
      assertTrue(Collect.newCollection(Deque.class) instanceof LinkedList);
      assertTrue(Collect.newCollection(Stack.class).getClass() == Stack.class);
      assertTrue(Collect.newCollection(LinkedHashSet.class).getClass() == LinkedHashSet.class);
   }

   @Test(expected = RuntimeException.class)
   public void badCreate() throws Exception {
      Collect.newCollection(NoNoArg.class);
   }



   static class NoNoArg extends AbstractCollection<String> {

      public NoNoArg(String param) {

      }

      @Override
      public Iterator<String> iterator() {
         return null;
      }

      @Override
      public int size() {
         return 0;
      }
   }

}//END OF CollectionUtilsTest
