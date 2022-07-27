package com.gengoai.cache;

import org.junit.Before;
import org.junit.Test;

import static com.gengoai.collection.Lists.arrayListOf;
import static junit.framework.TestCase.*;

/**
 * @author David B. Bracewell
 */
public class LRUCacheTest {
   private Cache<String, String> cache;

   @Before
   public void setUp() throws Exception {
      cache = new LRUCache<>(5);
      cache.put("A", "A");
      cache.put("B", "B");
      cache.put("C", "C");
      cache.put("D", "D");
      cache.put("E", "E");
      cache.put("B", "Z");
      cache.put("F", "F");
   }

   @Test
   public void containsKey() {
      assertTrue(cache.containsKey("F"));
      assertFalse(cache.containsKey("A"));
   }

   @Test
   public void get() {
      assertEquals("Z", cache.get("B"));
      assertEquals("ZZZZ", cache.get("A", () -> "ZZZZ"));
      assertFalse(cache.containsKey("C"));
   }

   @Test
   public void invalidate() {
      cache.invalidate("B");
      assertFalse(cache.containsKey("B"));


      cache.invalidateAll(arrayListOf("C", "D"));
      assertFalse(cache.containsKey("C"));
      assertFalse(cache.containsKey("D"));
   }

   @Test
   public void invalidateAll() {
      cache.invalidateAll();
      assertTrue(cache.isEmpty());
   }


   @Test
   public void size() {
      assertEquals(5, cache.size());
   }
}