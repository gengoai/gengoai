package com.gengoai.collection.multimap;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.collection.Collect;
import com.gengoai.collection.Iterables;
import lombok.NonNull;

import java.util.*;

/**
 * Maps keys to multiple values. Acts as a {@code Map<K, Collection<V>} where individual implementations specify the
 * type of collection, e.g. List, Set, etc.
 *
 * @param <K> the key type parameter
 * @param <V> the value type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = ArrayListMultimap.class)
public interface Multimap<K, V> {

   /**
    * A map representation of the multimap where the values are represented in a Collection.
    *
    * @return the map
    */
   @JsonValue
   Map<K, Collection<V>> asMap();

   /**
    * Clears all items in the multimap
    */
   default void clear() {
      asMap().clear();
   }

   /**
    * Checks if the given key and value exist in the multimap
    *
    * @param key   the key
    * @param value the value
    * @return True if the key is mapped to value, false otherwise
    */
   default boolean contains(Object key, Object value) {
      return asMap().containsKey(key) && asMap().get(key).contains(value);
   }

   /**
    * Checks if the key is contained in the multimap
    *
    * @param key the key
    * @return True if the key is mapped to one or more values in the multimap
    */
   default boolean containsKey(Object key) {
      return asMap().containsKey(key);
   }

   /**
    * Checks if the value exists in the multimap
    *
    * @param value the value
    * @return True if there exists a key that is mapped to the value, false otherwise
    */
   default boolean containsValue(Object value) {
      return values().contains(value);
   }

   /**
    * A set of the entries in the multimap
    *
    * @return the set of key-value pairs in the multimap
    */
   default Set<Map.Entry<K, V>> entries() {
      return new AbstractSet<Map.Entry<K, V>>() {
         @Override
         public Iterator<Map.Entry<K, V>> iterator() {
            return new EntrySetIterator<>(asMap());
         }

         @Override
         public int size() {
            return Multimap.this.size();
         }
      };
   }

   /**
    * Gets the values mapped to by the given key. A new collection is created if the key does not exist in the
    * multimap.
    *
    * @param key the key
    * @return the collection of values.
    */
   Collection<V> get(Object key);

   /**
    * Checks if the multimap is empty, i.e. has no valid mappings.
    *
    * @return True if it is empty
    */
   default boolean isEmpty() {
      return asMap().isEmpty();
   }

   /**
    * Gets the set of keys in the multimap
    *
    * @return the set of keys in the multimap
    */
   default Set<K> keySet() {
      return asMap().keySet();
   }

   /**
    * Puts the key-value pair mapping in the multimap
    *
    * @param key   the key
    * @param value the value
    * @return true if the key-value pair was successfully added.
    */
   default boolean put(K key, V value) {
      return get(key).add(value);
   }

   /**
    * Put all key-value pairs in the multimap
    *
    * @param map the map of key value pairs to add
    */
   default void putAll(@NonNull Map<? extends K, ? extends Collection<? extends V>> map) {
      map.forEach(this::putAll);
   }

   /**
    * Put all key-value pairs in the multimap
    *
    * @param multimap the map of key value pairs to add
    */
   default void putAll(@NonNull Multimap<? extends K, ? extends V> multimap) {
      putAll(multimap.asMap());
   }

   /**
    * Puts all values for a given key
    *
    * @param key    the key
    * @param values the values
    */
   default void putAll(K key, Iterable<? extends V> values) {
      values.forEach(v -> put(key, v));
   }

   /**
    * Removes the given key-value pair from the multimap
    *
    * @param key   the key
    * @param value the value
    * @return true the key-value pair was successfully removed
    */
   default boolean remove(Object key, Object value) {
      boolean success = asMap().containsKey(key) && asMap().get(key).remove(value);
      return success;
   }

   /**
    * Removes all values for the given key.
    *
    * @param key the key
    * @return the collection of values associated with the key
    */
   Collection<V> removeAll(Object key);

   /**
    * Replaces the values for a given key with the given new values
    *
    * @param key    the key
    * @param values the values
    */
   void replace(K key, Iterable<? extends V> values);

   /**
    * The number of key-value mappings in the multimap
    *
    * @return the number of key-value mappings
    */
   default int size() {
      return asMap().values().stream().mapToInt(Collection::size).sum();
   }

   /**
    * Provides a  view of the values in the multimap
    *
    * @return the collection
    */
   default Collection<V> values() {
      return Collect.asCollection(Iterables.flatten(asMap().values()));
   }

}//END OF Multimap
