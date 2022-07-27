package com.gengoai.collection.multimap;

import com.gengoai.function.SerializableSupplier;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

/**
 * The type Forwarding list.
 *
 * @param <K> the type parameter
 * @param <E> the type parameter
 * @author David B. Bracewell
 */
class ForwardingList<K, E> extends AbstractList<E> implements ForwardingCollection<E> {
   private final K key;
   private final Map<K, List<E>> map;
   private final SerializableSupplier<List<E>> listSupplier;

   /**
    * Instantiates a new Forwarding list.
    *
    * @param key          the key
    * @param map          the map
    * @param listSupplier the list supplier
    */
   public ForwardingList(K key, Map<K, List<E>> map, SerializableSupplier<List<E>> listSupplier) {
      this.key = key;
      this.map = map;
      this.listSupplier = listSupplier;
   }

   @Override
   public List<E> delegate() {
      return map.get(key);
   }

   @Override
   public List<E> createIfNeeded() {
      return map.computeIfAbsent(key, k -> listSupplier.get());
   }

   @Override
   public E get(int i) {
      if (delegate() == null) {
         throw new IndexOutOfBoundsException();
      }
      return delegate().get(i);
   }

   @Override
   public boolean add(E e) {
      return createIfNeeded().add(e);
   }

   @Override
   public void add(int i, E e) {
      createIfNeeded().add(i, e);
   }

   @Override
   public E remove(int i) {
      if (delegate() == null) {
         throw new IndexOutOfBoundsException();
      }
      return delegate().remove(i);
   }

   @Override
   public void removeIfNeeded() {
      if (delegate().isEmpty()) {
         map.remove(key);
      }
   }

   @Override
   public int size() {
      return delegate() == null
             ? 0
             : delegate().size();
   }
}//END OF ForwardingList
