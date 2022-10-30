package com.gengoai.collection.multimap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.collection.Sorting;
import com.gengoai.function.SerializableComparator;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

/**
 * Multimap in which keys are mapped to values in a tree set.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = TreeSetMultimap.class)
public class TreeSetMultimap<K, V> extends SetMultimap<K, V> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new TreeSetMultimap.
    */
   public TreeSetMultimap() {
      this(Sorting.natural());
   }

   /**
    * Instantiates a new TreeSetMultimap
    *
    * @param comparator the comparator to use for comparing values
    */
   public TreeSetMultimap(SerializableComparator<V> comparator) {
      super(() -> new TreeSet<>(comparator));
   }

   @JsonCreator
   public TreeSetMultimap(@JsonProperty @NonNull Map<? extends K, ? extends Collection<? extends V>> map) {
      this();
      putAll(map);
   }

}//END OF TreeSetMultimap
