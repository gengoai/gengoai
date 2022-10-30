/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.conversion;

import lombok.NonNull;

import java.io.Serializable;
import java.util.*;

/**
 * <p>A collection of static methods making casting from one type to another a bit easier. Note that the standard use
 * case for the collection based conversions are for use in for loops or method calls where the collection is read
 * only.</p>
 * <p>Note that these methods only cast and do not convert, i.e. an Integer object cannot be cast to a Double object.
 * If conversion is needed then transformations should be done using Guava or the Converter class.</p>
 *
 * @author David B. Bracewell
 */
public final class Cast {

   /**
    * Casts an object to the desired return throwing a <code>java.lang.ClassCastException</code> if the given object
    * cannot be cast as the desired type.
    *
    * @param <T> the type parameter
    * @param o   the object
    * @return the casted object or null if the object was null
    * @throws java.lang.ClassCastException If the given object cannot be cast as the return type
    */
   @SuppressWarnings("unchecked")
   public static <T> T as(Object o) {
      return o == null
             ? null
             : (T) o;
   }

   /**
    * Casts an object to a given type throwing a <code>java.lang.ClassCastException</code> if the given object cannot be
    * cast as the desired type.
    *
    * @param <T>   the type parameter
    * @param o     the object
    * @param clazz The class to cast to
    * @return the casted object or null if the object was null or the object was not of the desired type
    */
   public static <T> T as(Object o, @NonNull Class<T> clazz) {
      return o == null
             ? null
             : clazz.cast(o);
   }

   /**
    * <p>Casts the elements in an iterable in a lazy fashion. Calls to remove on the iterator will be reflected in
    * iterable </p>
    *
    * @param <T>      the type parameter
    * @param iterable the iterable
    * @return the casted iterable or null if the given iterable is null
    */
   public static <T> Iterable<T> cast(Iterable<?> iterable) {
      return iterable == null
             ? null
             : new CastingIterable<>(iterable);
   }

   /**
    * <p>Casts the elements in a set in a lazy fashion. Changes to the returned set are reflected in the the given
    * set.</p>
    *
    * @param <T> the type parameter
    * @param set the set
    * @return the casted set or null if the given set is null
    */
   public static <T> Set<T> cast(Set<?> set) {
      return set == null
             ? null
             : new CastingSet<>(set);
   }

   /**
    * <p>Casts the elements in a list in a lazy fashion. Changes to the returned list are reflected in the the given
    * list.</p>
    *
    * @param <T>  the type parameter
    * @param list the list to cast
    * @return the casted list or null if the given list is null
    */
   public static <T> List<T> cast(List<?> list) {
      return list == null
             ? null
             : new CastingList<>(list);
   }

   /**
    * <p>Casts the elements in an iterator in a lazy fashion. Changes to the returned iterator are reflected in the the
    * given iterator.</p>
    *
    * @param <T>      the type parameter
    * @param iterator the iterator to cast
    * @return the casted iterator or null if the given iterator was null
    */
   public static <T> Iterator<T> cast(Iterator<?> iterator) {
      return iterator == null
             ? null
             : new CastingIterator<>(iterator);
   }

   /**
    * <p>Casts the elements in a collection in a lazy fashion. Changes to the returned collection are reflected in the
    * the given collection.</p>
    *
    * @param <T>        the type parameter
    * @param collection the collection to cast
    * @return the casted collection or null if the collection is null
    */
   public static <T> Collection<T> cast(Collection<?> collection) {
      return collection == null
             ? null
             : new CastingCollection<>(collection);
   }

   /**
    * <p>Casts a map in a lazy fashion. Changes to the returned map are reflected in the
    * the given map.</p>
    *
    * @param <K> the type parameter
    * @param <V> the type parameter
    * @param map the map to cast
    * @return the casted map or null if the map is null
    */
   public static <K, V> Map<K, V> cast(Map<?, ?> map) {
      return map == null
             ? null
             : new CastingMap<>(map);
   }

   private Cast() {
      throw new IllegalAccessError();
   }

   private static class CastingMap<K, V> extends AbstractMap<K, V> implements Serializable {

      private static final long serialVersionUID = -2611216527976365947L;
      private final Map<?, ?> map;

      private CastingMap(Map<?, ?> map) {
         this.map = map;
      }

      @Override
      public Set<Entry<K, V>> entrySet() {
         return new CastingSet<>(map.entrySet());
      }
   }//END OF Cast$CastingMap

   private static class CastingCollection<V> extends AbstractCollection<V> implements Serializable {

      private static final long serialVersionUID = 7649013713070530029L;
      private final Collection<?> wrap;

      private CastingCollection(Collection<?> wrap) {
         this.wrap = wrap;
      }

      @Override
      public Iterator<V> iterator() {
         return new CastingIterator<>(wrap.iterator());
      }

      @Override
      public int size() {
         return wrap.size();
      }
   }//END OF Cast$CastingCollection

   private static class CastingList<V> extends AbstractList<V> implements Serializable {

      private static final long serialVersionUID = -5983768563529915927L;
      private final List<?> wrap;

      private CastingList(List<?> wrap) {
         this.wrap = wrap;
      }

      @Override
      public V get(int index) {
         return Cast.as(wrap.get(index));
      }

      @Override
      public int size() {
         return wrap.size();
      }

   }//END OF Cast$CastingList

   private static class CastingIterable<V> implements Iterable<V>, Serializable {

      private static final long serialVersionUID = 4904704966440313023L;
      private final Iterable<?> wrap;

      private CastingIterable(Iterable<?> wrap) {
         this.wrap = wrap;
      }

      @Override
      public Iterator<V> iterator() {
         return new CastingIterator<>(wrap.iterator());
      }
   }//END OF Cast$CastingIterable

   private static class CastingIterator<V> implements Iterator<V>, Serializable {

      private static final long serialVersionUID = -6524171187233137692L;
      private final Iterator<?> wrap;

      private CastingIterator(Iterator<?> wrap) {
         this.wrap = wrap;
      }

      @Override
      public boolean hasNext() {
         return wrap.hasNext();
      }

      @Override
      public V next() {
         return as(wrap.next());
      }

      @Override
      public void remove() {
         wrap.remove();
      }
   }//END OF Cast$CastingIterator

   private static class CastingSet<V> extends AbstractSet<V> implements Serializable {

      private static final long serialVersionUID = 8015953484790512598L;
      private final Set<?> wrap;

      private CastingSet(Set<?> wrap) {
         this.wrap = wrap;
      }

      @Override
      public Iterator<V> iterator() {
         return new CastingIterator<>(wrap.iterator());
      }

      @Override
      public int size() {
         return wrap.size();
      }

   }//END OF Cast$CastingSet

}//END OF Cast
