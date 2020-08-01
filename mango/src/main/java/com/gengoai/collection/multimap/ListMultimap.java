package com.gengoai.collection.multimap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableSupplier;

import java.util.List;

/**
 * Multimap which stores values in a list
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = ArrayListMultimap.class)
public abstract class ListMultimap<K, V> extends BaseMultimap<K, V, List<V>> {
   private static final long serialVersionUID = 1L;
   private final SerializableSupplier<List<V>> listSupplier;

   /**
    * Instantiates a new List multimap.
    *
    * @param listSupplier the list supplier
    */
   protected ListMultimap(SerializableSupplier<List<V>> listSupplier) {
      this.listSupplier = listSupplier;
   }

   @Override
   public List<V> get(Object key) {
      return new ForwardingList<>(Cast.as(key), map, listSupplier);
   }

}//END OF ListMultimap
