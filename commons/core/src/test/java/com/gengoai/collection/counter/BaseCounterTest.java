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

package com.gengoai.collection.counter;

import com.gengoai.collection.Lists;
import com.gengoai.collection.Sets;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
import com.gengoai.json.Json;
import com.gengoai.Math2;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public abstract class BaseCounterTest {

   protected abstract Counter<String> getCounter1();

   protected abstract Counter<String> getCounter2();

   protected abstract Counter<String> getCounter3();

   protected abstract Counter<String> getEmptyCounter();

   @Test
   public void valueChange() throws Exception {
      Counter<String> counter = getCounter1();

      counter.decrement("a");
      assertEquals(2, counter.get("a"), 0);

      counter.increment("a");
      assertEquals(3, counter.get("a"), 0);

      counter.increment("a", 0);
      assertEquals(3, counter.get("a"), 0);

      counter.increment("Z", 100);
      assertEquals(100, counter.get("Z"), 0);

      counter.decrement("Z", 100);
      assertEquals(0, counter.get("Z"), 0);

      counter.decrement("Z", 0);
      assertEquals(0, counter.get("Z"), 0);

      counter.decrementAll(Arrays.asList("b", "c"));
      assertEquals(0, counter.get("c"), 0);
      assertEquals(1, counter.get("b"), 0);

      counter.incrementAll(Arrays.asList("b", "c"));
      assertEquals(1, counter.get("c"), 0);
      assertEquals(2, counter.get("b"), 0);

      counter.incrementAll(Arrays.asList("b", "c"), 4);
      assertEquals(5, counter.get("c"), 0);
      assertEquals(6, counter.get("b"), 0);

      counter.decrementAll(Arrays.asList("b", "c"), 4);
      assertEquals(1, counter.get("c"), 0);
      assertEquals(2, counter.get("b"), 0);
   }

   @Test
   public void minMax() throws Exception {
      Counter<String> counter = getCounter2();
      assertEquals("B", counter.max());
      assertEquals(5, counter.maximumCount(), 0);
      assertEquals("C", counter.min());
      assertEquals(1, counter.minimumCount(), 0);

      assertEquals(5.0, counter.topN(1).get("B"), 0.0);
      assertEquals(1.0, counter.bottomN(1).get("C"), 0.0);
   }

   @Test
   public void stats() throws Exception {
      Counter<String> counter = getCounter2();
      assertEquals(3.3, counter.average(), 0.04);
      assertEquals(10.0, counter.sum(), 0.0);
      assertEquals(2.08, counter.standardDeviation(), 0.02);
      assertEquals(6.48, counter.magnitude(), 0.01);
   }

   @Test
   public void itemsByCount() throws Exception {
      Counter<String> counter = getCounter2();
      Assert.assertEquals(Lists.arrayListOf("B", "A", "C"), counter.itemsByCount(false));
      assertEquals(Lists.arrayListOf("C", "A", "B"), counter.itemsByCount(true));
   }

   @Test
   public void remove() throws Exception {
      Counter<String> counter = getCounter2();
      counter.removeAll(null);
      assertEquals(10, counter.sum(), 0);
      counter.removeAll(Collections.singleton("A"));
      assertEquals(6, counter.sum(), 0);
      assertEquals(5, counter.remove("B"), 0);
      assertEquals(1, counter.remove("C"), 0);
      assertEquals(0, counter.remove("Z"), 0);
      assertEquals(0, counter.remove(null), 0);
      assertTrue(counter.isEmpty());
   }

   @Test
   public void sample() throws Exception {
      Counter<String> counter = getCounter2();
      assertTrue(counter.contains(counter.sample()));
      double bCount = 0;
      Random rnd = new Random(1234);
      for (int i = 0; i < 50; i++) {
         if (counter.sample(rnd).equals("B")) {
            bCount++;
         }
      }
      assertTrue(bCount / 50.0 >= 0.10);
   }

   @Test
   public void csv() throws Exception {
      Counter<String> counter = getCounter2();
      Resource r = new StringResource();
      counter.writeCsv(r);
      Counter<String> fromCSV = Counters.readCsv(r, String.class);
      assertEquals(counter, fromCSV);
   }


   @Test
   public void contains() throws Exception {
      Counter<String> counterOne = getCounter1();
      assertFalse(counterOne.contains("d"));
      assertTrue(counterOne.contains("a"));
      assertTrue(counterOne.contains("b"));
      assertTrue(counterOne.contains("c"));
   }

   @Test
   public void items() throws Exception {
      Counter<String> counterOne = getCounter1();
      Assert.assertEquals(Sets.hashSetOf("a", "b", "c"), counterOne.items());

      counterOne.items().removeIf(s -> s.equals("a") || s.equals("b"));
      assertEquals(1, counterOne.sum(), 0);

      Iterator<String> it = counterOne.items().iterator();
      it.next();
      it.remove();

      assertEquals(0, counterOne.sum(), 0);

      counterOne = getCounter1();
      counterOne.items().retainAll(Collections.singleton("c"));
      assertEquals(1, counterOne.sum(), 0);

      counterOne.items().removeAll(Collections.singleton("c"));
      assertEquals(0, counterOne.sum(), 0);
   }

   @Test
   public void values() throws Exception {
      Counter<String> counterOne = getCounter1();
      Collection<Double> values = counterOne.values();
      assertEquals(3, values.size(), 0);
      Assert.assertEquals(6, Math2.sum(values), 0);
      assertEquals(3, counterOne.get("a"), 0);
      assertEquals(2, counterOne.get("b"), 0);
      assertEquals(1, counterOne.get("c"), 0);
      assertEquals(0, counterOne.get("z"), 0);
      assertEquals(6, counterOne.sum(), 0);

      counterOne.values().removeIf(d -> d <= 1);
      assertEquals(5, counterOne.sum(), 0);

      counterOne.values().removeAll(Collections.singleton(2.0));
      assertEquals(3, counterOne.sum(), 0);

      counterOne.values().remove(3.0);
      assertEquals(0, counterOne.sum(), 0);

      counterOne = getCounter1();
      counterOne.values().retainAll(Collections.singleton(1.0));
      assertEquals(1.0, counterOne.sum(), 0.0);
   }

   @Test
   public void merge() throws Exception {
      Counter<String> c1 = getEmptyCounter();

      assertTrue(c1.isEmpty());

      Counter<String> c2 = getCounter1();

      c1.merge(c2);
      assertEquals(3.0, c1.get("a"), 0.0);

      c1.merge(c2.asMap());
      assertEquals(6.0, c1.get("a"), 0.0);


      assertEquals(3, c1.size());
   }

   @Test
   public void clear() throws Exception {
      Counter<String> counterOne = getCounter1();
      counterOne.clear();
      assertEquals(0, counterOne.size());
      assertEquals(0.0, counterOne.sum(), 0.0);
   }

   @Test
   public void set() throws Exception {
      Counter<String> counter = getEmptyCounter();

      counter.set("A", 100);
      assertEquals(100, counter.get("A"), 0);

      counter.set("Z", 0);
      assertEquals(0, counter.get("Z"), 0);
   }


   @Test
   public void divideBySum() throws Exception {
      Counter<String> counter = getCounter2();
      counter.divideBySum();
      assertEquals(1.0, counter.sum(), 0.0);
      assertEquals(.5, counter.get("B"), 0.0);
      assertEquals(.4, counter.get("A"), 0.0);
      assertEquals(.1, counter.get("C"), 0.0);
      counter.clear();
      counter.divideBySum();
      assertEquals(0, counter.sum(), 0);
   }

   @Test
   public void adjustValues() throws Exception {
      Counter<String> counter = getCounter2();

      Counter<String> adjusted = counter.adjustValues(d -> d * d);
      assertEquals(42, adjusted.sum(), 0.0);
      assertEquals(25, adjusted.get("B"), 0.0);
      assertEquals(16, adjusted.get("A"), 0.0);
      assertEquals(1, adjusted.get("C"), 0.0);

      counter.adjustValuesSelf(d -> d * d);
      assertEquals(42, counter.sum(), 0.0);
      assertEquals(25, counter.get("B"), 0.0);
      assertEquals(16, counter.get("A"), 0.0);
      assertEquals(1, counter.get("C"), 0.0);
   }

   @Test
   public void filterByValue() throws Exception {
      Counter<String> counter = getCounter2();
      Counter<String> filtered = counter.filterByValue(d -> d < 5.0);
      assertEquals(5, filtered.sum(), 0.0);
      assertEquals(0, filtered.get("B"), 0.0);
      assertEquals(4, filtered.get("A"), 0.0);
      assertEquals(1, filtered.get("C"), 0.0);
   }

   @Test
   public void filterByKey() throws Exception {
      Counter<String> counter = getCounter2();
      Counter<String> filtered = counter.filterByKey(d -> !d.equals("B"));
      assertEquals(5, filtered.sum(), 0.0);
      assertEquals(0, filtered.get("B"), 0.0);
      assertEquals(4, filtered.get("A"), 0.0);
      assertEquals(1, filtered.get("C"), 0.0);
   }

   @Test
   public void mapKeys() throws Exception {
      Counter<String> counter = getCounter2();
      Counter<String> mapped = counter.mapKeys(String::toLowerCase);
      assertEquals(10, mapped.sum(), 0.0);
      assertEquals(5, mapped.get("b"), 0.0);
      assertEquals(4, mapped.get("a"), 0.0);
      assertEquals(1, mapped.get("c"), 0.0);
   }

   @Test
   public void forEach() throws Exception {
      Counter<String> counter = getCounter3();
      counter.forEach((k, v) -> {
         assertEquals("a", k);
         assertEquals(1.0, v, 0.0);
      });

   }

   @Test
   public void entries() throws Exception {
      Counter<String> counter = getCounter2();

      counter.entries().removeIf(e -> e.getValue() >= 5);
      assertEquals(5, counter.sum(), 0);

      counter.entries().removeAll(Collections.singleton($("A", 4.0)));

      assertEquals(1, counter.sum(), 0);

      counter.entries().remove($("C", 1.0));
      assertEquals(0, counter.sum(), 0);


      counter = getCounter2();
      counter.entries().retainAll(Collections.singleton($("A", 4.0)));
      assertEquals(4, counter.sum(), 0);

      Iterator<Map.Entry<String, Double>> it = counter.entries().iterator();
      it.next().setValue(100d);
      assertEquals(100d, counter.sum(), 0);
   }

   @Test
   public void copy() throws Exception {
      Counter<String> counter = getCounter2();
      assertEquals(counter, counter.copy());
   }

   @Test
   public void testJson() throws Exception {
      Counter<String> counter = getCounter2();
      String str = Json.dumps(counter);
      Counter<String> des = Json.parse(str).as(parameterizedType(counter.getClass(), String.class));
      assertEquals(counter, des);
   }

}
