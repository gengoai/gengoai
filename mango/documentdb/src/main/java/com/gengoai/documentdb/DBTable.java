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
    * @throws IOException the io exception
    */
   void add(@NonNull DBDocument document) throws IOException;

   /**
    * Add all.
    *
    * @param documents the documents
    * @throws IOException the io exception
    */
   void addAll(@NonNull Iterable<DBDocument> documents) throws IOException;

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
   DBDocument get(@NonNull Object id);

   long numberOfDocuments() throws DocumentDBException;

   /**
    * Remove boolean.
    *
    * @param document the document
    * @return the boolean
    * @throws IOException the io exception
    */
   boolean remove(@NonNull DBDocument document) throws IOException;

   /**
    * Search list.
    *
    * @param query the query
    * @return the list
    * @throws IOException the io exception
    */
   default List<DBDocument> search(String query) throws IOException {
      return search(query, Integer.MAX_VALUE);
   }

   /**
    * Search list.
    *
    * @param field  the field
    * @param vector the vector
    * @return the list
    * @throws IOException the io exception
    */
   default List<DBDocument> search(String field, float... vector) throws IOException {
      return search(field, Integer.MAX_VALUE, vector);
   }

   /**
    * Search list.
    *
    * @param query   the query
    * @param numHits the num hits
    * @return the list
    * @throws IOException the io exception
    */
   List<DBDocument> search(String query, int numHits) throws IOException;

   /**
    * Search list.
    *
    * @param field   the field
    * @param numHits the num hits
    * @param vector  the vector
    * @return the list
    * @throws IOException the io exception
    */
   List<DBDocument> search(String field, int numHits, float... vector) throws IOException;

   /**
    * Update.
    *
    * @param document the document
    * @throws IOException the io exception
    */
   default void update(@NonNull DBDocument document) throws IOException {
      add(document);
   }

   /**
    * Update all.
    *
    * @param documents the documents
    * @throws IOException the io exception
    */
   default void updateAll(@NonNull Iterable<DBDocument> documents) throws IOException {
      addAll(documents);
   }


   boolean hasIndex(@NonNull String fieldName);

}//END OF DocumentDB