package com.gengoai.collection.multimap;

import com.gengoai.collection.Maps;
import com.gengoai.collection.Sets;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.gengoai.tuple.Tuples.$;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public abstract class BaseMultimapTest {
   private Multimap<String, String> multimap;
   private Collection<String> aValues;
   private Collection<String> bValues;
   private Collection<String> cValues;
   private Supplier<Multimap<String, String>> supplier;

   public BaseMultimapTest(Supplier<Multimap<String, String>> supplier, Supplier<Collection<String>> collectionSupplier) {
      this.supplier = supplier;

      this.multimap = supplier.get();
      this.multimap.put("A", "A");
      this.multimap.put("A", "B");
      this.multimap.put("A", "B");
      this.multimap.put("A", "C");
      this.multimap.put("B", "C");
      this.multimap.put("B", "D");
      this.multimap.put("C", "D");

      this.aValues = collectionSupplier.get();
      this.aValues.add("A");
      this.aValues.add("B");
      this.aValues.add("B");
      this.aValues.add("C");

      this.bValues = collectionSupplier.get();
      this.bValues.add("C");
      this.bValues.add("D");

      this.cValues = collectionSupplier.get();
      this.cValues.add("D");

   }

   @Test
   public void values() {
      assertEquals(aValues.size() + bValues.size() + cValues.size(),
                   multimap.values().size());

      multimap.values().remove("D");
      assertNotEquals(bValues, multimap.get("B"));
      multimap.put("B", "D");
      assertEquals(bValues, multimap.get("B"));
      assertEquals(cValues, multimap.get("C"));
   }

   @Test
   public void size() {
      assertEquals(aValues.size() + bValues.size() + cValues.size(), multimap.size());
   }

   @Test
   public void isEmpty() {
      assertFalse(multimap.isEmpty());
      assertTrue(supplier.get().isEmpty());
   }

   @Test
   public void putAll() {
      Multimap<String, String> copy = supplier.get();
      copy.putAll(multimap);
      assertEquals(multimap, copy);
      copy.clear();
      assertTrue(copy.isEmpty());

      copy.putAll("A", aValues);
      assertEquals(aValues, copy.get("A"));
      copy.clear();
      assertTrue(copy.isEmpty());

      copy.putAll(Maps.hashMapOf($("A", aValues)));
      assertEquals(aValues, copy.get("A"));
      copy.clear();
      assertTrue(copy.isEmpty());

   }

   @Test
   public void entrySet() {
      Set<Map.Entry<String, String>> entrySet = multimap.entries();
      entrySet.remove($("C", "D"));
      assertFalse(multimap.contains("C", "D"));
      multimap.put("C", "D");
      assertEquals(cValues, multimap.get("C"));
   }

   @Test
   public void contains() {
      assertTrue(multimap.contains("A", "B"));
      assertTrue(multimap.containsKey("A"));
      assertTrue(multimap.containsValue("A"));
      assertTrue(multimap.contains("C", "D"));

      assertFalse(multimap.contains("A", "D"));
      assertFalse(multimap.containsKey("D"));
      assertFalse(multimap.containsValue("F"));
   }

   @Test
   public void keySet() {
      assertEquals(Sets.hashSetOf("A", "B", "C"), multimap.keySet());
      multimap.keySet().remove("C");
      assertEquals(Sets.hashSetOf("A", "B"), multimap.keySet());
      multimap.put("C", "D");
      assertEquals(cValues, multimap.get("C"));
   }

   @Test
   public void remove() {
      assertTrue(multimap.remove("C", "D"));
      assertFalse(multimap.remove("D", "D"));
      assertNotEquals(cValues, multimap.get("C"));
      multimap.put("C", "D");
      assertEquals(cValues, multimap.get("C"));
   }

   @Test
   public void removeAll() {
      Collection<String> r = multimap.removeAll("C");
      assertNotEquals(cValues, multimap.get("C"));
      assertEquals(cValues, r);
      multimap.put("C", "D");
      assertEquals(cValues, multimap.get("C"));
   }

   @Test
   public void get() {
      assertEquals(aValues, multimap.get("A"));
      assertEquals(bValues, multimap.get("B"));
      assertEquals(cValues, multimap.get("C"));

      multimap.get("C").remove("D");
      assertNotEquals(cValues, multimap.get("C"));
      multimap.put("C", "D");
      assertEquals(cValues, multimap.get("C"));
   }
}