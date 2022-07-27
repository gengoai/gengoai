package com.gengoai.collection;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public abstract class BaseTableTest {
   private final Table<String, String, Double> table;

   protected BaseTableTest(Supplier<Table<String, String, Double>> supplier) {
      table = supplier.get();
      table.put("A", "B", 1.0);
      table.put("A", "C", 2.0);
      table.put("B", "C", 5.0);
      table.put("B", "D", 1.0);
   }


   @Test
   public void get() {
      assertEquals(1.0, table.get("A", "B"), 0.0);
      assertEquals(2.0, table.get("A", "C"), 0.0);
      assertEquals(5.0, table.get("B", "C"), 0.0);
      assertEquals(1.0, table.get("B", "D"), 0.0);
   }

   @Test
   public void remove() {
      assertEquals(1.0, table.remove("A", "B"), 0.0);
      assertNull(table.put("A", "B", 1.0));
      assertNull(table.remove("A", "E"));
   }

   @Test
   public void removeColumn() {
      Map<String, Double> c = Maps.hashMapOf($("A", 2.0), $("B", 5.0));
      assertEquals(c, table.removeColumn("C"));
      assertFalse(table.containsColumn("C"));
      table.column("C").putAll(c);
      assertTrue(table.containsColumn("C"));

      table.column("C").entrySet().remove($("A", 2.0));
   }

   @Test
   public void removeRow() {
      Map<String, Double> a = Maps.hashMapOf($("B", 1.0), $("C", 2.0));
      assertEquals(a, table.removeRow("A"));
      assertFalse(table.containsRow("A"));
      table.row("A").putAll(a);
      assertTrue(table.containsRow("A"));
   }

   @Test
   public void contains() {
      assertTrue(table.contains("A", "C"));
      assertFalse(table.contains("A", "E"));
   }

   @Test
   public void size() {
      assertEquals(4, table.size());
   }

   @Test
   public void values() {
      assertEquals(4, table.values().size());
      assertEquals(Sets.hashSetOf(1.0, 2.0, 5.0), Sets.asHashSet(table.values()));
   }

   @Test
   public void columnKeySet() {
      Map<String, Double> c = Maps.hashMapOf($("A", 2.0), $("B", 5.0));
      Set<String> cKeys = Sets.hashSetOf("C", "B", "D");

      assertEquals(cKeys, table.columnKeySet());

      table.columnKeySet().remove("C");
      cKeys.remove("C");
      assertEquals(cKeys, table.columnKeySet());


      table.column("C").putAll(c);
      table.columnKeySet().removeAll(Collections.singletonList("C"));
      assertEquals(cKeys, table.columnKeySet());

      table.column("C").putAll(c);
   }

   @Test
   public void rowKeySet() {
   }

   @Test
   public void clear() {
   }
}