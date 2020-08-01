package com.gengoai.collection.multimap;

import com.gengoai.conversion.Cast;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * List backed multimaps
 *
 * @param <K> the key type parameter
 * @param <V> the value type parameter
 * @param <C> the type parameter
 * @author David B. Bracewell
 */
public abstract class BaseMultimap<K, V, C extends Collection<V>> implements Multimap<K, V>, Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The Map.
    */
   protected final Map<K, C> map = new HashMap<>();

   @Override
   public C removeAll(Object key) {
      return map.remove(key);
   }

   @Override
   public void replace(K key, Iterable<? extends V> values) {
      Collection<V> list = get(key);
      list.clear();
      values.forEach(list::add);
   }

   @Override
   public Map<K, Collection<V>> asMap() {
      return Cast.as(map);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof BaseMultimap)) return false;
      BaseMultimap<?, ?, ?> that = (BaseMultimap<?, ?, ?>) o;
      return Objects.equals(map, that.map);
   }

   @Override
   public int hashCode() {
      return Objects.hash(map);
   }

   @Override
   public String toString() {
      return map.toString();
   }
}//END OF ListMultimap
