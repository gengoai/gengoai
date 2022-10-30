package com.gengoai.collection.multimap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * The interface Forwarding collection.
 *
 * @param <E> the type parameter
 * @author David B. Bracewell
 */
interface ForwardingCollection<E> extends Collection<E>, Serializable {
   /**
    * Delegate collection.
    *
    * @return the collection
    */
   Collection<E> delegate();

   /**
    * Create if needed collection.
    *
    * @return the collection
    */
   Collection<E> createIfNeeded();

   /**
    * Remove if needed.
    */
   void removeIfNeeded();

   @Override
   default int size() {
      return delegate() == null
             ? 0
             : delegate().size();
   }

   @Override
   default boolean isEmpty() {
      return delegate() == null || delegate().isEmpty();
   }

   @Override
   default boolean contains(Object o) {
      return delegate() != null && delegate().contains(o);
   }

   @Override
   default Iterator<E> iterator() {
      return delegate() == null
             ? Collections.emptyIterator()
             : new Iterator<E>() {
                Iterator<E> backing = delegate().iterator();

                @Override
                public boolean hasNext() {
                   return backing.hasNext();
                }

                @Override
                public E next() {
                   return backing.next();
                }

                @Override
                public void remove() {
                   backing.remove();
                   removeIfNeeded();
                }
             };
   }

   @Override
   default Object[] toArray() {
      return delegate() == null
             ? new Object[]{}
             : delegate().toArray();
   }

   @Override
   default <T> T[] toArray(T[] ts) {
      return delegate() == null
             ? ts
             : delegate().toArray(ts);
   }

   @Override
   default boolean add(E e) {
      return createIfNeeded().add(e);
   }

   @Override
   default boolean remove(Object o) {
      return delegate() != null && delegate().remove(o);
   }

   @Override
   default boolean containsAll(Collection<?> collection) {
      return delegate() != null && delegate().containsAll(collection);
   }

   @Override
   default boolean addAll(Collection<? extends E> collection) {
      boolean toReturn = createIfNeeded().addAll(collection);
      removeIfNeeded();
      return toReturn;
   }

   @Override
   default boolean removeAll(Collection<?> collection) {
      boolean toReturn = delegate() != null && delegate().removeAll(collection);
      removeIfNeeded();
      return toReturn;
   }

   @Override
   default boolean retainAll(Collection<?> collection) {
      boolean toReturn = delegate() != null && delegate().retainAll(collection);
      removeIfNeeded();
      return toReturn;
   }

   @Override
   default void clear() {
      delegate().clear();
      removeIfNeeded();
   }

}//END OF ForwardingCollection
