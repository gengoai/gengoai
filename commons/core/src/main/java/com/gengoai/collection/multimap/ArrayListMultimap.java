package com.gengoai.collection.multimap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Multimap that stores values in an ArrayList
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = ArrayListMultimap.class)
public class ArrayListMultimap<K, V> extends ListMultimap<K, V> implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Array list multimap.
    */
   public ArrayListMultimap() {
      super(ArrayList::new);
   }

   @JsonCreator
   public ArrayListMultimap(@JsonProperty @NonNull Map<? extends K, ? extends Collection<? extends V>> map) {
      this();
      putAll(map);
   }

}//END OF ArrayListMultimap
