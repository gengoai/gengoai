package com.gengoai.collection.multimap;

import com.gengoai.function.SerializableSupplier;

import java.util.*;

/**
 * The type Forwarding set.
 *
 * @param <K> the type parameter
 * @param <E> the type parameter
 * @author David B. Bracewell
 */
class ForwardingSet<K, E> extends AbstractSet<E> implements ForwardingCollection<E> {
   private final K key;
   private final Map<K, Set<E>> map;
   private final SerializableSupplier<Set<E>> setSupplier;

   /**
    * Instantiates a new Forwarding set.
    *
    * @param key         the key
    * @param map         the map
    * @param setSupplier the set supplier
    */
   public ForwardingSet(K key, Map<K, Set<E>> map, SerializableSupplier<Set<E>> setSupplier) {
      this.key = key;
      this.map = map;
      this.setSupplier = setSupplier;
   }

   @Override
   public Collection<E> delegate() {
      return map.get(key);
   }

   @Override
   public Collection<E> createIfNeeded() {
      return map.computeIfAbsent(key, k -> setSupplier.get());
   }

   @Override
   public Iterator<E> iterator() {
      return ForwardingCollection.super.iterator();
   }

   @Override
   public int size() {
      return delegate() == null
             ? 0
             : delegate().size();
   }

   @Override
   public void removeIfNeeded() {
      if (delegate().isEmpty()) {
         map.remove(key);
      }
   }

   @Override
   public boolean add(E e) {
      return createIfNeeded().add(e);
   }


}//END OF ForwardingSet
