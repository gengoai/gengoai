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

package com.gengoai.sql.statement;

import com.gengoai.Validation;
import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLFormattable;
import com.gengoai.sql.object.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 * <p>Actions that can be performed when altering a table. Note that not all dbms may support all actioons.</p>
 */
public interface AlterTableAction extends SQLFormattable {

   /**
    * Renames a column
    */
   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   class RenameColumn implements AlterTableAction {
      private static final long serialVersionUID = 1L;
      String oldName;
      String newName;

      /**
       * Instantiates a new Rename column.
       *
       * @param oldName the old name
       * @param newName the new name
       */
      public RenameColumn(String oldName, String newName) {
         this.oldName = Validation.notNullOrBlank(oldName);
         this.newName = Validation.notNullOrBlank(newName);
      }

      @Override
      public String toSQL(@NonNull SQLDialect dialect) {
         return "RENAME COLUMN " + SQLDialect.escape(oldName) + " TO " + SQLDialect.escape(newName);
      }
   }//END OF RenameColumn

   /**
    * Drops a column
    */
   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   class DropColumn implements AlterTableAction {
      private static final long serialVersionUID = 1L;
      String name;

      /**
       * Instantiates a new Drop column.
       *
       * @param name the name
       */
      public DropColumn(String name) {
         this.name = Validation.notNullOrBlank(name);
      }

      @Override
      public String toSQL(@NonNull SQLDialect dialect) {
         return "DROP COLUMN " + SQLDialect.escape(name);
      }
   }//END OF RenameColumn

   /**
    * Alters a column definition
    */
   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   class AlterColumn implements AlterTableAction {
      private static final long serialVersionUID = 1L;
      Column newDefinition;

      public AlterColumn(@NonNull Column newDefinition) {
         this.newDefinition = newDefinition;
      }

      @Override
      public String toSQL(@NonNull SQLDialect dialect) {
         return "ALTER COLUMN " + dialect.columnDefinition(newDefinition);
      }
   }//END OF AlterColumn

   /**
    * Adds a column
    */
   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   class AddColumn implements AlterTableAction {
      private static final long serialVersionUID = 1L;
      Column newDefinition;

      public AddColumn(@NonNull Column newDefinition) {
         this.newDefinition = newDefinition;
      }

      @Override
      public String toSQL(@NonNull SQLDialect dialect) {
         return "ADD COLUMN " + dialect.columnDefinition(newDefinition);
      }
   }//END OF AlterColumn

   /**
    * Renames the table
    */
   @Value
   @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
   class RenameTable implements AlterTableAction {
      private static final long serialVersionUID = 1L;
      String newTableName;

      public RenameTable(String newTableName) {
         this.newTableName = Validation.notNullOrBlank(newTableName);
      }

      @Override
      public String toSQL(@NonNull SQLDialect dialect) {
         return "RENAME TO " + SQLDialect.escape(newTableName);
      }
   }//END OF RenameTable
}//END OF AlterTableAction
