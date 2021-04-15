package com.gengoai.collection;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class MapsTest {


   @Test
   public void asHashMap() {
      Map<String, String> map = new HashMap<>();
      map.put("A", "a");
      map.put("B", "b");
      map.put("C", "c");
      assertEquals(map, Maps.asHashMap(Arrays.asList("A", "B", "C"), String::toLowerCase));
   }

   @Test
   public void putAll() {
      Map<String, String> map = new HashMap<>();
      map.put("A", "a");
      map.put("B", "b");
      map.put("C", "c");
      assertEquals(map, Maps.putAll(new HashMap<>(), $("A", "a"),
                                    $("B", "b"),
                                    $("C", "c")));
   }


   @Test
   public void sortByKey() {
      Map<String, String> map = new HashMap<>();
      map.put("A", "a");
      map.put("B", "b");
      map.put("C", "c");
      List<Map.Entry<String, String>> asc = Maps.sortEntriesByKey(map, true);
      List<Map.Entry<String, String>> dsc = Maps.sortEntriesByKey(map, false);
      assertNotEquals(asc, dsc);
      assertEquals("A", asc.get(0).getKey());
      assertEquals("C", dsc.get(0).getKey());
   }

   @Test
   public void sortByValue() {
      Map<String, String> map = new HashMap<>();
      map.put("A", "a");
      map.put("B", "b");
      map.put("C", "c");
      List<Map.Entry<String, String>> asc = Maps.sortEntriesByValue(map, true);
      List<Map.Entry<String, String>> dsc = Maps.sortEntriesByValue(map, false);
      assertNotEquals(asc, dsc);
      assertEquals("A", asc.get(0).getKey());
      assertEquals("C", dsc.get(0).getKey());
   }

   @Test
   public void create() {
      assertTrue(Maps.create(Map.class) instanceof HashMap);
      assertTrue(Maps.create(HashMap.class) instanceof HashMap);
      assertTrue(Maps.create(TreeMap.class) instanceof TreeMap);
      assertTrue(Maps.create(SortedMap.class) instanceof TreeMap);
      assertTrue(Maps.create(LinkedHashMap.class) instanceof LinkedHashMap);
      assertTrue(Maps.create(ConcurrentMap.class) instanceof ConcurrentHashMap);
      assertTrue(Maps.create(ConcurrentHashMap.class) instanceof ConcurrentHashMap);
      assertTrue(Maps.create(ConcurrentSkipListMap.class) instanceof ConcurrentSkipListMap);
   }
}