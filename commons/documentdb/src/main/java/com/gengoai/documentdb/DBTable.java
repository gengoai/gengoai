/*
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

package com.gengoai.documentdb;

import lombok.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public interface DBTable extends AutoCloseable, Iterable<DBDocument> {


   /**
    * Add.
    *
    * @param document the document
    * @throws DocumentDBException the io exception
    */
   default void add(@NonNull DBDocument document) throws DocumentDBException{
      addAll(Collections.singleton(document));
   }

   /**
    * Add all.
    *
    * @param documents the documents
    * @throws DocumentDBException the io exception
    */
   void addAll(@NonNull Iterable<DBDocument> documents) throws DocumentDBException;

   void addIndex(@NonNull IndexedField... indexedFields) throws DocumentDBException;

   /**
    * Commit.
    *
    * @throws IOException the io exception
    */
   void commit() throws IOException;

   /**
    * Get db document.
    *
    * @param id the id
    * @return the db document
    */
   DBDocument get(@NonNull Object id) throws DocumentDBException;

   long numberOfDocuments() throws DocumentDBException;

   /**
    * Remove boolean.
    *
    * @param document the document
    * @return the boolean
    * @throws DocumentDBException the io exception
    */
   boolean remove(@NonNull DBDocument document) throws DocumentDBException;

   /**
    * Search list.
    *
    * @param query the query
    * @return the list
    * @throws DocumentDBException the io exception
    */
   default List<DBDocument> search(String query) throws DocumentDBException {
      return search(query, Integer.MAX_VALUE);
   }

   /**
    * Search list.
    *
    * @param field  the field
    * @param vector the vector
    * @return the list
    * @throws DocumentDBException the io exception
    */
   default List<DBDocument> search(String field, float... vector) throws DocumentDBException {
      return search(field, Integer.MAX_VALUE, vector);
   }

   /**
    * Search list.
    *
    * @param query   the query
    * @param numHits the num hits
    * @return the list
    * @throws DocumentDBException the io exception
    */
   List<DBDocument> search(String query, int numHits) throws DocumentDBException;

   /**
    * Search list.
    *
    * @param field   the field
    * @param numHits the num hits
    * @param vector  the vector
    * @return the list
    * @throws IOException the io exception
    */
   List<DBDocument> search(String field, int numHits, float... vector) throws DocumentDBException;

   /**
    * Update.
    *
    * @param document the document
    * @throws DocumentDBException the io exception
    */
   default void update(@NonNull DBDocument document) throws DocumentDBException {
      add(document);
   }

   /**
    * Update all.
    *
    * @param documents the documents
    * @throws DocumentDBException the io exception
    */
   default void updateAll(@NonNull Iterable<DBDocument> documents) throws DocumentDBException {
      addAll(documents);
   }


   boolean hasIndex(@NonNull String fieldName);

}//END OF DocumentDB