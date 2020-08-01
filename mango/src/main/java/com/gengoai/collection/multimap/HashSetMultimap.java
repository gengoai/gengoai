package com.gengoai.collection.multimap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Multimap that stores values in a HashSet.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = HashSetMultimap.class)
public class HashSetMultimap<K, V> extends SetMultimap<K, V> implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Hash set multimap.
    */
   public HashSetMultimap() {
      super(HashSet::new);
   }

   @JsonCreator
   public HashSetMultimap(@JsonProperty @NonNull Map<? extends K, ? extends Collection<? extends V>> map) {
      this();
      putAll(map);
   }
}//END OF HashSetMultimap
