package com.gengoai.collection.multimap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Multimap that stores values in a LinkedHashSet
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = LinkedHashSetMultimap.class)
public class LinkedHashSetMultimap<K, V> extends SetMultimap<K, V> implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Linked hash set multimap.
    */
   public LinkedHashSetMultimap() {
      super(LinkedHashSet::new);
   }

   @JsonCreator
   public LinkedHashSetMultimap(@JsonProperty @NonNull Map<? extends K, ? extends Collection<? extends V>> map) {
      this();
      putAll(map);
   }

}//END OF LinkedHashSetMultimap
