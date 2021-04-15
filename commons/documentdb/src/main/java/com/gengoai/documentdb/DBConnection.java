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

import java.util.List;

/**
 * <p>A connection to a document database which allows opening, creating, dropping, and listing tables within the
 * database.</p>
 *
 * @author David B. Bracewell
 */
public interface DBConnection extends AutoCloseable {

   /**
    * Create document db.
    *
    * @param table         the table
    * @param primaryKey    the primary key
    * @param indexedFields the indexed fields
    * @return the document db
    * @throws DocumentDBException the document db exception
    */
   DBTable create(@NonNull String table, @NonNull IndexedField primaryKey, @NonNull IndexedField... indexedFields) throws DocumentDBException;

   /**
    * Drops a table space with the given name.
    *
    * @param table the table to drop
    * @throws DocumentDBException Either the table does not exist, or there was an error performing the drop
    */
   void drop(@NonNull String table) throws DocumentDBException;

   /**
    * Checks if a table with the given name exists or not
    *
    * @param table the name of the table
    * @return True if a table with the given name is defined, False otherwise
    */
   boolean exists(@NonNull String table);

   /**
    * Lists the available table in the database
    *
    * @return the list of table  names
    * @throws DocumentDBException Something went wrong trying to generate the list of table names
    */
   List<String> list() throws DocumentDBException;

   /**
    * Opens an existing table.
    *
    * @param table the table to open
    * @return the document database for the given table name
    * @throws DocumentDBException Either the table  does not exist or there was an error when opening the table
    */
   DBTable open(@NonNull String table) throws DocumentDBException;

}//END OF DBConnection
