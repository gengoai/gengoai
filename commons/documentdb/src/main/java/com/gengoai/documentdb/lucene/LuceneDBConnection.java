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

package com.gengoai.documentdb.lucene;

import com.gengoai.documentdb.DBConnection;
import com.gengoai.documentdb.DBTable;
import com.gengoai.documentdb.DocumentDBException;
import com.gengoai.documentdb.IndexedField;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.List;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class LuceneDBConnection implements DBConnection {
   @NonNull
   private final Resource root;

   public LuceneDBConnection(@NonNull Resource root) {
      this.root = root;
   }

   @Override
   public void close() throws Exception {

   }

   @Override
   public DBTable create(@NonNull String table,
                         @NonNull IndexedField primaryKey,
                         @NonNull IndexedField... indexedFields) throws DocumentDBException {
      Resource child = getTableLocation(table, false);
      LuceneDocumentDB db = new LuceneDocumentDB(child, primaryKey);
      db.addIndex(indexedFields);
      return db;
   }

   @Override
   public void drop(@NonNull String table) throws DocumentDBException {
      Resource child = getTableLocation(table, true);
      try {
         child.delete(true);
      } catch (RuntimeException e) {
         throw new DocumentDBException(e.getCause());
      }
   }

   @Override
   public boolean exists(@NonNull String table) {
      return root.getChild(table).exists();
   }

   @Override
   public List<String> list() throws DocumentDBException {
      return null;
   }

   @Override
   public DBTable open(@NonNull String table) throws DocumentDBException {
      return new LuceneDocumentDB(getTableLocation(table, true));
   }

   private Resource getTableLocation(String table, boolean mustExistTrueNotExistFalse) throws DocumentDBException {
      if (Strings.isNullOrBlank(table)) {
         throw DocumentDBException.tableNameMustNotBeBlank();
      }
      Resource child = root.getChild(table);
      if (mustExistTrueNotExistFalse && !child.exists()) {
         throw DocumentDBException.tableDoesNotExitException(table);
      } else if (child.exists() && !mustExistTrueNotExistFalse) {
         throw DocumentDBException.tableAlreadyExists(table);
      }
      return child;
   }
}//END OF LuceneDBConnection
