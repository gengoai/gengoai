package com.gengoai.cache;

import com.gengoai.Validation;
import com.gengoai.collection.LRUMap;
import com.gengoai.function.SerializableSupplier;

import java.util.Collections;
import java.util.Map;

/**
 * Last-Recently-Used Cache with bounded size.
 *
 * @param <K> the key type parameter
 * @param <V> the value type parameter
 * @author David B. Bracewell
 */
public class LRUCache<K, V> implements Cache<K, V> {
   protected transient final Map<K, V> cache;

   /**
    * Instantiates a new LRU cache with given max size.
    *
    * @param maxSize the max size
    */
   public LRUCache(int maxSize) {
      Validation.checkArgument(maxSize > 0, "Cache must have size of greater than zero.");
      this.cache = Collections.synchronizedMap(new LRUMap<>(maxSize));
   }

   @Override
   public boolean containsKey(K key) {
      return cache.containsKey(key);
   }

   @Override
   public V get(K key) {
      return cache.get(key);
   }

   @Override
   public V get(K key, SerializableSupplier<? extends V> supplier) {
      return cache.computeIfAbsent(key, k -> supplier.get());
   }

   @Override
   public void invalidate(K key) {
      cache.remove(key);
   }

   @Override
   public void invalidateAll() {
      cache.clear();
   }

   @Override
   public void put(K key, V value) {
      cache.put(key, value);
   }

   @Override
   public long size() {
      return cache.size();
   }

   @Override
   public String toString() {
      return "LRUCache{" +
            "size=" + size() +
            '}';
   }
}//END OF LRUCache
