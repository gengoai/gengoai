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

package com.gengoai.sql.sqlite;

import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.object.SQLObject;
import com.gengoai.sql.object.Table;
import com.gengoai.sql.statement.*;
import lombok.*;

/**
 * The type Sq lite full text table.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class SQLiteFullTextTable extends SQLObject {
   Table owner;

   /**
    * Instantiates a new Sq lite full text table.
    *
    * @param name  the name
    * @param owner the owner
    */
   public SQLiteFullTextTable(String name, @NonNull Table owner) {
      super(name);
      this.owner = owner;
   }

   /**
    * Creates a delete statement that will delete all rows matching the given where criteria
    *
    * @param where the criteria for row deletion
    * @return the delete statement
    */
   public Delete delete(String where) {
      return Delete.from(this).where(where);
   }

   /**
    * Creates a delete statement that will delete all rows matching the given where criteria
    *
    * @param where the criteria for row deletion
    * @return the delete statement
    */
   public Delete delete(SQLElement where) {
      return Delete.from(this).where(where);
   }

   @Override
   public String getKeyword() {
      return "TABLE";
   }

   /**
    * Rebuilds the full text table
    *
    * @return the sql update statement
    */
   public UpdateStatement rebuild() {
      return Insert.into(this).columns(getName()).values(SQL.L("rebuild"));
   }

   /**
    * Creates a select statement selecting from this table with nothing else specified
    *
    * @return the select statement
    */
   public Select select() {
      return Select.from(this);
   }

   /**
    * Creates a select statement selecting from this table returning the specified columns
    *
    * @param columns the columns to select
    * @return the select statement
    */
   public Select select(@NonNull SQLElement... columns) {
      return Select.from(this).columns(columns);
   }

   /**
    * Creates a select statement selecting all columns from this table.
    *
    * @return the select statement
    */
   public Select selectAll() {
      return Select.from(this).columns(SQL.ALL);
   }

   /**
    * Creates an update statement for this table
    *
    * @param updateType the type of update to perform
    * @return the update statement
    */
   public Update update(@NonNull UpdateType updateType) {
      return Update.table(this).type(updateType);
   }

   /**
    * Creates an update statement for this table
    *
    * @return the update statement
    */
   public Update update() {
      return Update.table(this);
   }

}//END OF SQLiteFullTextTable

