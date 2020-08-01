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

package com.gengoai.sql.constraint;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.SQLFormattable;
import com.gengoai.sql.constraint.column.*;
import lombok.NonNull;

import java.util.List;

/**
 * Defines a constraint that is specified as part of the Column Declaration.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
      @JsonSubTypes.Type(name = "As", value = As.class),
      @JsonSubTypes.Type(name = "Check", value = Check.class),
      @JsonSubTypes.Type(name = "Collate", value = Collate.class),
      @JsonSubTypes.Type(name = "Default", value = Default.class),
      @JsonSubTypes.Type(name = "ForeignKey", value = ForeignKey.class),
      @JsonSubTypes.Type(name = "NotNull", value = NotNull.class),
      @JsonSubTypes.Type(name = "PrimaryKey", value = PrimaryKey.class),
      @JsonSubTypes.Type(name = "Unique", value = Unique.class)
})
public interface ColumnConstraint extends SQLFormattable {

   /**
    * Adds an "AS" constraint where the generated value is a stored column.
    *
    * @param expression the expression to define the value of the column
    * @return the column constraint
    */
   static ColumnConstraint asStored(@NonNull SQLElement expression) {
      return asStored(null, expression);
   }

   /**
    * Adds an "AS" constraint where the generated value is a stored column.
    *
    * @param name       the name of the constraint
    * @param expression the expression to define the value of the column
    * @return the column constraint
    */
   static ColumnConstraint asStored(String name, @NonNull SQLElement expression) {
      return new As(name, expression, true);
   }

   /**
    * Adds an "AS" constraint where the generated value is a virtual column.
    *
    * @param expression the expression to define the value of the column
    * @return the column constraint
    */
   static ColumnConstraint asVirtual(@NonNull SQLElement expression) {
      return asVirtual(null, expression);
   }

   /**
    * Adds an "AS" constraint where the generated value is a virtual column.
    *
    * @param name       the name of the constraint
    * @param expression the expression to define the value of the column
    * @return the column constraint
    */
   static ColumnConstraint asVirtual(String name, @NonNull SQLElement expression) {
      return new As(name, expression, false);
   }

   /**
    * Adds an "AUTOINCREMENT" constraint
    *
    * @return the column constraint
    */
   static ColumnConstraint autoIncrement() {
      return AutoIncrement.INSTANCE;
   }

   /**
    * Adds a "check" constraint that checks that the given expression passes.
    *
    * @param expression the expression to use for the check constraint.
    * @return the column constraint
    */
   static ColumnConstraint check(@NonNull SQLElement expression) {
      return check(null, expression);
   }

   /**
    * Adds a "check" constraint that checks that the given expression passes.
    *
    * @param name       the name of the constraint
    * @param expression the expression to use for the check constraint.
    * @return the column constraint
    */
   static ColumnConstraint check(String name, @NonNull SQLElement expression) {
      return new Check(name, expression);
   }

   /**
    * Defines the collation used for the given textual column
    *
    * @param collationName the collation name
    * @return the column constraint
    */
   static ColumnConstraint collate(String collationName) {
      return new Collate(collationName);
   }

   /**
    * Defines a "default" value constraint that sets the column to the given expression on insertion if one is not
    * specified.
    *
    * @param expression the expression to use for setting the default value (typically this needs to be constant.)
    * @return the column constraint
    */
   static ColumnConstraint defaultValue(@NonNull SQLElement expression) {
      return defaultValue(null, expression);
   }

   /**
    * Defines a "default" value constraint that sets the column to the given expression on insertion if one is not
    * specified.
    *
    * @param name       the name of the constraint
    * @param expression the expression to use for setting the default value (typically this needs to be constant.)
    * @return the column constraint
    */
   static ColumnConstraint defaultValue(String name, @NonNull SQLElement expression) {
      return new Default(name, expression);
   }

   /**
    * Defines a "foreign key" constraint between this column and one or more columns on a foreign table.
    *
    * @param name                the name of the constraint
    * @param foreignTable        the name of the foreign table (i.e. the one we have a constraint on)
    * @param foreignTableColumns the name of the foreign table columns
    * @param onUpdate            the action to perform on update of the foreign table
    * @param onDelete            the action to perform on delete of the foreign table
    * @param deferred            the deferrability of the constraint
    * @return the column constraint
    */
   static ColumnConstraint foreignKey(String name,
                                      String foreignTable,
                                      @NonNull List<String> foreignTableColumns,
                                      ForeignKeyAction onUpdate,
                                      ForeignKeyAction onDelete,
                                      Deferrable deferred) {
      return new ForeignKey(name, foreignTable, foreignTableColumns, onUpdate, onDelete, deferred);
   }

   /**
    * Defines a "foreign key" constraint between this column and one or more columns on a foreign table.
    *
    * @param foreignTable        the name of the foreign table (i.e. the one we have a constraint on)
    * @param foreignTableColumns the name of the foreign table columns
    * @param onUpdate            the action to perform on update of the foreign table
    * @param onDelete            the action to perform on delete of the foreign table
    * @return the column constraint
    */
   static ColumnConstraint foreignKey(String foreignTable,
                                      @NonNull List<String> foreignTableColumns,
                                      ForeignKeyAction onUpdate,
                                      ForeignKeyAction onDelete) {
      return foreignKey(null, foreignTable, foreignTableColumns, onUpdate, onDelete, Deferrable.NOT_DEFERRABLE);
   }

   /**
    * Defines a "foreign key" constraint between this column and one or more columns on a foreign table.
    *
    * @param name                the name of the constraint
    * @param foreignTable        the name of the foreign table (i.e. the one we have a constraint on)
    * @param foreignTableColumns the name of the foreign table columns
    * @return the column constraint
    */
   static ColumnConstraint foreignKey(String name, String foreignTable, @NonNull List<String> foreignTableColumns) {
      return foreignKey(name, foreignTable, foreignTableColumns, null, null, Deferrable.NOT_DEFERRABLE);
   }

   /**
    * Defines a "foreign key" constraint between this column and one or more columns on a foreign table.
    *
    * @param foreignTable        the name of the foreign table (i.e. the one we have a constraint on)
    * @param foreignTableColumns the name of the foreign table columns
    * @return the column constraint
    */
   static ColumnConstraint foreignKey(String foreignTable,
                                      @NonNull List<String> foreignTableColumns) {
      return foreignKey(null, foreignTable, foreignTableColumns, null, null, Deferrable.NOT_DEFERRABLE);
   }

   /**
    * Not null column constraint.
    *
    * @return the column constraint
    */
   static ColumnConstraint notNull() {
      return new NotNull();
   }

   /**
    * Primary key column constraint.
    *
    * @return the column constraint
    */
   static ColumnConstraint primaryKey() {
      return primaryKey(null, null);
   }

   /**
    * Primary key column constraint.
    *
    * @param name           the name
    * @param conflictClause the conflict clause
    * @return the column constraint
    */
   static ColumnConstraint primaryKey(String name,
                                      ConflictClause conflictClause) {
      return new PrimaryKey(name, conflictClause);
   }

   /**
    * Primary key column constraint.
    *
    * @param name the name
    * @return the column constraint
    */
   static ColumnConstraint primaryKey(String name) {
      return new PrimaryKey(name, null);
   }

   /**
    * Unique column constraint.
    *
    * @return the column constraint
    */
   static ColumnConstraint unique() {
      return new Unique();
   }

   /**
    * Unique column constraint.
    *
    * @param conflictClause the conflict clause
    * @return the column constraint
    */
   static ColumnConstraint unique(ConflictClause conflictClause) {
      return unique(null, conflictClause);
   }

   /**
    * Unique column constraint.
    *
    * @param name           the name
    * @param conflictClause the conflict clause
    * @return the column constraint
    */
   static ColumnConstraint unique(String name, ConflictClause conflictClause) {
      return new Unique(name, conflictClause);
   }

   /**
    * Provides value boolean.
    *
    * @return the boolean
    */
   boolean providesValue();

}//END OF ColumnConstraint
