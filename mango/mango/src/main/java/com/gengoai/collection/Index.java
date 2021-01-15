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

package com.gengoai.collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.Copyable;
import com.gengoai.stream.Streams;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>An index represents a mapping from an Item to an id. All ids are positive integers.</p>
 *
 * @param <E> the type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = HashMapIndex.class)
public interface Index<E> extends Iterable<E>, Copyable<Index<E>> {

   /**
    * <p>Adds an item to the index. If the item is already in the index, the item's id is returned otherwise the newly
    * generated id is returned.</p>
    *
    * @param item the item to add
    * @return The id of the item
    */
   int add(E item);

   /**
    * <p>Adds all the items in the iterable to the index</p>
    *
    * @param items The items to add.
    */
   void addAll(Iterable<E> items);

   /**
    * Retrieves an unmodifiable list view of the index
    *
    * @return an unmodifiable list view of the index
    */
   List<E> asList();

   /**
    * Clears the index
    */
   void clear();

   /**
    * Determines if an item is in the index
    *
    * @param item The item
    * @return True if the item is in the index, False if not
    */
   boolean contains(E item);

   /**
    * Gets the item with the given id.
    *
    * @param id The id
    * @return The item associated with the id or null if there is none.
    */
   E get(int id);

   /**
    * Gets the id of the item or -1 if it is not in the index.
    *
    * @param item The item whose id we want
    * @return The id of the item or -1 if it is not in the index
    */
   int getId(E item);

   /**
    * Determines if the index is empty
    *
    * @return True if there are no items in the index
    */
   @JsonIgnore
   boolean isEmpty();

   /**
    * Gets a set of the items in the Index
    *
    * @return the set
    */
   Set<E> itemSet();

   /**
    * The number of items in the index
    *
    * @return The number of items in the index
    */
   int size();

   /**
    * Stream stream.
    *
    * @return the stream
    */
   default Stream<E> stream() {
      return Streams.asStream(this);
   }

}//END OF Index
