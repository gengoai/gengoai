package com.gengoai.collection.multimap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableSupplier;

import java.util.Set;

/**
 * Multimap which stores values in a set
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = LinkedHashSetMultimap.class)
public abstract class SetMultimap<K, V> extends BaseMultimap<K, V, Set<V>> {
   private static final long serialVersionUID = 1L;
   private final SerializableSupplier<Set<V>> setSupplier;

   /**
    * Instantiates a new Set multimap.
    *
    * @param setSupplier the set supplier
    */
   protected SetMultimap(SerializableSupplier<Set<V>> setSupplier) {
      this.setSupplier = setSupplier;
   }

   @Override
   public Set<V> get(Object key) {
      return new ForwardingSet<>(Cast.as(key), map, setSupplier);
   }
}//END OF ListMultimap
