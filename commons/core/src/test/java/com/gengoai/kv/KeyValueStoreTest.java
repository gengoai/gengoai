package com.gengoai.kv;

import com.gengoai.io.Resources;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author David B. Bracewell
 */
public class KeyValueStoreTest {

   @Test
   public void inMemory() {
      KeyValueStore<String, String> kv = KeyValueStore.connect("kv:mem:test");
      kv.put("A", "B");
      kv.put("C", "D");

      assertEquals("test", kv.getNameSpace());
      assertEquals(2L, kv.sizeAsLong());
      assertEquals(2, kv.size());

      KeyValueStore<String, String> kv2 = KeyValueStore.connect("kv:mem:test");
      assertEquals("test", kv2.getNameSpace());
      assertEquals(2L, kv2.sizeAsLong());
      assertEquals(2, kv2.size());
   }

   @Test
   public void inMemoryNavigable() {
      NavigableKeyValueStore<String, String> kv = KeyValueStore.connect("kv:mem:testNav;navigable=true");
      kv.put("A", "B");
      kv.put("C", "D");

      assertEquals("testNav", kv.getNameSpace());
      assertEquals(2L, kv.sizeAsLong());
      assertEquals(2, kv.size());

      KeyValueStore<String, String> kv2 = KeyValueStore.connect("kv:mem:testNav");
      assertEquals("testNav", kv2.getNameSpace());
      assertEquals(2L, kv2.sizeAsLong());
      assertEquals(2, kv2.size());
   }

   @Test(expected = ClassCastException.class)
   public void badCast() {
      KeyValueStore<String, String> kv = KeyValueStore.connect("kv:mem:badCast;navigable=false");
      KeyValueStore<String, String> kv2 = KeyValueStore.connect("kv:mem:badCast;navigable=true");
   }

   @Test
   public void onDisk() {
      String tmpFile = Resources.temporaryFile().path();
      KeyValueStore<String, String> kv = KeyValueStore.connect("kv:disk:test::" + tmpFile);
      kv.put("A", "B");
      kv.put("C", "D");

      assertEquals("test", kv.getNameSpace());
      assertEquals(2L, kv.sizeAsLong());
      assertEquals(2, kv.size());

      KeyValueStore<String, String> kv2 = KeyValueStore.connect("kv:disk:test::" + tmpFile);
      assertEquals("test", kv2.getNameSpace());
      assertEquals(2L, kv2.sizeAsLong());
      assertEquals(2, kv2.size());
   }

}