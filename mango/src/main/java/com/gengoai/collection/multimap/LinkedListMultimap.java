package com.gengoai.collection.multimap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * The type Linked list multimap.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = LinkedListMultimap.class)
public class LinkedListMultimap<K, V> extends ListMultimap<K, V> implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Linked list multimap.
    */
   public LinkedListMultimap() {
      super(LinkedList::new);
   }

   @JsonCreator
   public LinkedListMultimap(@JsonProperty @NonNull Map<? extends K, ? extends Collection<? extends V>> map) {
      this();
      putAll(map);
   }

}//END OF LinkedListMultimap
