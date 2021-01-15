package com.gengoai.collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.conversion.Cast;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.gengoai.tuple.Tuples.$;

/**
 * Table implementation using a map of maps.
 *
 * @param <R> the row type parameter
 * @param <C> the column type parameter
 * @param <V> the value type parameter
 * @author David B. Bracewell
 */
@NoArgsConstructor
public class HashBasedTable<R, C, V> implements Table<R, C, V>, Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<R, Map<C, V>> map = new HashMap<>();

   @JsonCreator
   protected HashBasedTable(List<TableEntry<R, C, V>> entryList) {
      for (TableEntry<R, C, V> rcvTableEntry : entryList) {
         put(rcvTableEntry.getRow(), rcvTableEntry.getCol(), rcvTableEntry.getValue());
      }
   }

   @Override
   public void clear() {
      map.clear();
   }

   @Override
   public Map<R, V> column(C column) {
      return new ColumnView(column);
   }

   @Override
   public Set<C> columnKeySet() {
      return new ColumnSet();
   }

   @Override
   public boolean contains(R row, C column) {
      return map.containsKey(row) && map.get(row).containsKey(column);
   }

   @Override
   public boolean containsColumn(C column) {
      return map.values().stream().anyMatch(m -> m.containsKey(column));
   }

   @Override
   public boolean containsRow(R row) {
      return map.containsKey(row);
   }

   private void createRowIfNeeded(R row) {
      if (!map.containsKey(row)) {
         map.put(row, new HashMap<>());
      }
   }

   private void deleteRowIfEmpty(R row) {
      if (map.get(row).isEmpty()) {
         map.remove(row);
      }
   }

   @Override
   @JsonValue
   public Set<TableEntry<R, C, V>> entrySet() {
      return map.entrySet().stream()
            .flatMap(e -> e.getValue().entrySet()
                  .stream()
                  .map(c -> new TableEntry<>(e.getKey(), c.getKey(), c.getValue())))
            .collect(Collectors.toSet());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof HashBasedTable)) return false;
      HashBasedTable<?, ?, ?> that = (HashBasedTable<?, ?, ?>) o;
      return Objects.equals(map, that.map);
   }

   @Override
   public V get(R row, C column) {
      return map.containsKey(row)
            ? map.get(row).get(column)
            : null;
   }

   @Override
   public int hashCode() {
      return Objects.hash(map);
   }

   @Override
   public V put(R row, C column, V value) {
      createRowIfNeeded(row);
      return map.get(row).put(column, value);
   }

   @Override
   public V remove(R row, C column) {
      if (map.containsKey(row)) {
         V value = map.get(row).remove(column);
         deleteRowIfEmpty(row);
         return value;
      }
      return null;
   }

   @Override
   public Map<R, V> removeColumn(C column) {
      Map<R, V> rval = map.keySet()
            .stream()
            .filter(key -> map.get(key).containsKey(column))
            .map(key -> $(key, map.get(key).remove(column)))
            .collect(Collectors.toMap(o -> o.v1, o -> o.v2));
      map.keySet().removeIf(k -> map.get(k).isEmpty());
      return rval;
   }

   @Override
   public Map<C, V> removeRow(R row) {
      return map.remove(row);
   }

   @Override
   public Map<C, V> row(R row) {
      return new RowView(row);
   }

   @Override
   public Set<R> rowKeySet() {
      return map.keySet();
   }

   @Override
   public int size() {
      return map.values().stream().mapToInt(Map::size).sum();
   }

   @Override
   public String toString() {
      return map.toString();
   }

   @Override
   public Collection<V> values() {
      return map.values().stream().flatMap(map -> map.values().stream()).collect(Collectors.toList());
   }

   private class RowView extends AbstractMap<C, V> {
      private final R row;

      private RowView(R row) {
         this.row = row;
      }

      @Override
      public void clear() {
         map.remove(row);
      }

      @Override
      public Set<Entry<C, V>> entrySet() {
         return map.containsKey(row)
               ? map.get(row).entrySet()
               : Collections.emptySet();
      }

      @Override
      public V get(Object key) {
         return HashBasedTable.this.get(row, Cast.as(key));
      }

      @Override
      public V put(C key, V value) {
         return HashBasedTable.this.put(row, key, value);
      }

      @Override
      public V remove(Object key) {
         return HashBasedTable.this.remove(row, Cast.as(key));
      }

      @Override
      public int size() {
         return map.containsKey(row)
               ? map.get(row).size()
               : 0;
      }
   }

   private class ColumnIterator implements Iterator<C> {
      private Iterator<Map.Entry<R, Map<C, V>>> rowEntryIterator = map.entrySet().iterator();
      private Set<C> seen = new HashSet<>();
      private Iterator<Map.Entry<C, V>> colValueIterator = null;
      private C column;
      private C lastColumn;

      private boolean advance() {
         while (column == null) {
            while (colValueIterator != null && colValueIterator.hasNext()) {
               C nc = colValueIterator.next().getKey();
               if (!seen.contains(nc)) {
                  column = nc;
                  seen.add(nc);
                  return true;
               }
            }
            if (!rowEntryIterator.hasNext()) {
               return false;
            }
            colValueIterator = rowEntryIterator.next().getValue().entrySet().iterator();
         }
         return true;
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public C next() {
         advance();
         lastColumn = column;
         column = null;
         return lastColumn;
      }

      @Override
      public void remove() {
         removeColumn(lastColumn);
         map.keySet().forEach(HashBasedTable.this::deleteRowIfEmpty);
      }
   }

   private class ColumnSet extends AbstractSet<C> {

      @Override
      public Iterator<C> iterator() {
         return new ColumnIterator();
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         int size = size();
         map.values().forEach(m -> m.keySet().removeAll(c));
         map.keySet().forEach(HashBasedTable.this::deleteRowIfEmpty);
         return size != size();
      }

      @Override
      public boolean removeIf(Predicate<? super C> filter) {
         int size = size();
         map.values().forEach(m -> m.keySet().removeIf(filter));
         map.keySet().forEach(HashBasedTable.this::deleteRowIfEmpty);
         return size != size();
      }

      @Override
      public int size() {
         return Iterators.size(new ColumnIterator());
      }

   }

   private class ColumnView extends AbstractMap<R, V> {
      private final C column;

      private ColumnView(C column) {
         this.column = column;
      }

      @Override
      public void clear() {
         HashBasedTable.this.removeColumn(column);
      }

      @Override
      public boolean containsKey(Object key) {
         return map.containsKey(key) && map.get(key).containsKey(column);
      }

      @Override
      public Set<Entry<R, V>> entrySet() {
         return new IteratorSet<>(() -> Iterators.transform(Iterators.filter(map.entrySet().iterator(),
               e -> e.getValue().containsKey(column)),
               e -> $(e.getKey(), e.getValue().get(column))));
      }

      @Override
      public V get(Object key) {
         return HashBasedTable.this.get(Cast.as(key), column);
      }

      @Override
      public V put(R key, V value) {
         return HashBasedTable.this.put(key, column, value);
      }

      @Override
      public V remove(Object key) {
         return HashBasedTable.this.remove(Cast.as(key), column);
      }
   }
}//END OF BaseTable
