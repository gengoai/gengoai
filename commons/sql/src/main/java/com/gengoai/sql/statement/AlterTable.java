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

import com.gengoai.sql.SQLElement;
import com.gengoai.sql.object.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Defines an SQL Alter Table statement to alter the definition of a table. The alter statement is made up of one or
 * more {@link AlterTableAction}s that define the action to perform to alter the table. Note that some RDBMS (e.g.
 * SQLite) only allow one action per alter statement. Furthermore, not all RDBMS support all {@link
 * AlterTableAction}.</p>
 */
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class AlterTable implements UpdateStatement {
   private static final long serialVersionUID = 1L;
   SQLElement table;
   List<AlterTableAction> actions = new ArrayList<>();

   private AlterTable(@NonNull SQLElement table) {
      this.table = table;
   }

   /**
    * Static method for constructing an AlterTable statement given the table being altered.
    *
    * @param table the table
    * @return the AlterTable statement
    */
   public static AlterTable table(@NonNull SQLElement table) {
      return new AlterTable(table);
   }

   /**
    * Adds an action to the statement.
    *
    * @param action the action
    * @return the AlterTable statement
    */
   public AlterTable action(@NonNull AlterTableAction action) {
      actions.add(action);
      return this;
   }

   /**
    * Adds an {@link AlterTableAction.AddColumn} action which will add the given {@link Column} to the table.
    *
    * @param column the column to add
    * @return the AlterTable statement
    */
   public AlterTable addColumn(@NonNull Column column) {
      actions.add(new AlterTableAction.AddColumn(column));
      return this;
   }

   /**
    * Adds an alter column action which updates the column based on the given column definition.
    *
    * @param column the column with new a definition
    * @return the AlterTable statement
    */
   public AlterTable alterColumn(@NonNull Column column) {
      actions.add(new AlterTableAction.AlterColumn(column));
      return this;
   }

   /**
    * Adds a drop column action to drop the column with the given name
    *
    * @param name the name of the column to drop
    * @return the AlterTable statement
    */
   public AlterTable dropColumn(String name) {
      actions.add(new AlterTableAction.DropColumn(name));
      return this;
   }

   /**
    * Adds a rename column action to rename the <code>old name</code> column to <code>new name</code>
    *
    * @param oldName the old name of the column
    * @param newName the new name of the column
    * @return the AlterTable statement
    */
   public AlterTable renameColumn(String oldName, String newName) {
      actions.add(new AlterTableAction.RenameColumn(oldName, newName));
      return this;
   }

   /**
    * Adds a rename table action to rename the table to <code>new name</code>
    *
    * @param newName the new name of the table
    * @return the AlterTable statement
    */
   public AlterTable renameTable(String newName) {
      actions.add(new AlterTableAction.RenameTable(newName));
      return this;
   }


}//END OF AlterTable
