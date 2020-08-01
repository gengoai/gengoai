package com.gengoai.collection.multimap;

import com.gengoai.Validation;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static com.gengoai.tuple.Tuples.$;

/**
 * The type Entry set iterator.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 */
class EntrySetIterator<K, V> implements Iterator<Map.Entry<K, V>> {
   private final Map<K, Collection<V>> map;
   private Iterator<K> keyIterator = null;
   private Iterator<V> currentCollectionIter = null;
   private K currentKey = null;
   private V currentValue = null;

   /**
    * Instantiates a new Entry set iterator.
    *
    * @param map the map
    */
   public EntrySetIterator(Map<K, Collection<V>> map) {
      this.map = map;
      this.keyIterator = map.keySet().iterator();
   }


   private boolean advance() {
      while (currentCollectionIter == null || !currentCollectionIter.hasNext()) {
         if (keyIterator.hasNext()) {
            currentKey = keyIterator.next();
            currentCollectionIter = map.get(currentKey).iterator();
         } else {
            return false;
         }
      }
      return true;
   }

   @Override
   public boolean hasNext() {
      return advance();
   }

   @Override
   public Map.Entry<K, V> next() {
      Validation.checkState(advance(), "No such element");
      currentValue = currentCollectionIter.next();
      return $(currentKey, currentValue);
   }

   @Override
   public void remove() {
      map.getOrDefault(currentKey, Collections.emptyList()).remove(currentValue);
   }
}
