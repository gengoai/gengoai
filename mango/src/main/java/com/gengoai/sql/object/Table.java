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

package com.gengoai.sql.object;

import com.gengoai.Validation;
import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.SQLObject;
import com.gengoai.sql.constraint.TableConstraint;
import com.gengoai.sql.operator.SQLOperable;
import com.gengoai.sql.statement.*;
import com.gengoai.string.Strings;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Defines an SQL table including its name, columns, and constraints.</p>
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class Table extends SQLObject {
   private static final long serialVersionUID = 1L;
   @Getter
   private final List<Column> columns = new ArrayList<>();
   @Getter
   private final List<TableConstraint> constraints = new ArrayList<>();
   @Getter
   private final String tableType;

   /**
    * Instantiates a new Table.
    *
    * @param name      the name of the Table
    * @param tableType RDMS specific string defining type of table
    */
   public Table(String name, String tableType) {
      this(name, tableType, Collections.emptyList(), Collections.emptyList());
   }

   /**
    * Instantiates a new Table.
    *
    * @param name the name of the Table
    */
   public Table(String name) {
      this(name, null, Collections.emptyList(), Collections.emptyList());
   }

   /**
    * Instantiates a new Table.
    *
    * @param name        the name of the Table
    * @param tableType   RDMS specific string defining type of table
    * @param columns     the columns on the table
    * @param constraints the constraints on the table
    */
   public Table(String name,
                String tableType,
                @NonNull List<Column> columns,
                @NonNull List<TableConstraint> constraints) {
      super(Validation.notNullOrBlank(name));
      this.tableType = Strings.emptyToNull(tableType);
      this.columns.addAll(columns);
      this.constraints.addAll(constraints);
   }

   /**
    * Instantiates a new Table.
    *
    * @param name      the name of the Table
    * @param tableType RDMS specific string defining type of table
    * @param columns   the columns on the table
    */
   public Table(String name, String tableType, @NonNull List<Column> columns) {
      this(name, tableType, columns, Collections.emptyList());
   }

   /**
    * Instantiates a new Table.
    *
    * @param name    the name of the Table
    * @param columns the columns on the table
    */
   public Table(String name, @NonNull List<Column> columns) {
      this(name, null, columns, Collections.emptyList());
   }

   /**
    * Adds the given column to the table definition generating an {@link AlterTable} statement to perform on the
    * database.
    *
    * @param column the column to add
    * @return the {@link AlterTable} statement needed to sync the code definition with the database definition
    */
   public AlterTable addColumn(@NonNull Column column) {
      Validation.checkArgument(columns.stream().noneMatch(c -> c.getName().equalsIgnoreCase(column.getName())),
                               "A column with the name '" + column.getName() + "' already exists");
      columns.add(column);
      return AlterTable.table(this).addColumn(column);
   }

   /**
    * Adds the given {@link TableConstraint} to the table definition generating an {@link AlterTable} statement to
    * perform on the database.
    *
    * @param tableConstraint the table constraint to add
    * @return the {@link AlterTable} statement needed to sync the code definition with the database definition
    */
   public AlterTable addConstraint(@NonNull TableConstraint tableConstraint) {
      this.constraints.add(tableConstraint);
      return AlterTable.table(this);
   }

   /**
    * Alters the definition of the given column generating an {@link AlterTable} statement to perform on the database.
    *
    * @param name     the column to alter
    * @param consumer the consumer to use to update the column definition
    * @return the {@link AlterTable} statement needed to sync the code definition with the database definition
    */
   public AlterTable alterColumn(@NonNull String name, @NonNull Consumer<Column> consumer) {
      Column column = columns.stream()
                             .filter(c -> c.getName().equalsIgnoreCase(name))
                             .findFirst()
                             .orElseThrow(() -> new IllegalArgumentException("No column named +'" + name + "'"));
      consumer.accept(column);
      Validation.checkArgument(name.equalsIgnoreCase(column.getName()),
                               "Changing the column name is not supported in alterColumn, please use renameColumn.");
      return AlterTable.table(this).alterColumn(column);
   }

   /**
    * Generates a select statement to return the total number of rows in the table
    *
    * @return the select "count (0)" select statement.
    */
   public Select countAll() {
      return select(SQL.F.count("0"));
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

   /**
    * Creates a delete statement that will delete all rows in the table
    *
    * @return the delete statement
    */
   public Delete deleteAll() {
      return Delete.from(this);
   }

   /**
    * Removes the column with the given name from the table definition generating an {@link AlterTable} statement to
    * perform on the database.
    *
    * @param name the name of the column to drop
    * @return the {@link AlterTable} statement needed to sync the code definition with the database definition
    */
   public AlterTable dropColumn(@NonNull String name) {
      Column column = columns.stream()
                             .filter(c -> c.getName().equalsIgnoreCase(name))
                             .findFirst()
                             .orElseThrow(() -> new IllegalArgumentException("No column named +'" + name + "'"));
      columns.remove(column);
      return AlterTable.table(this).dropColumn(name);
   }

   @Override
   public String getKeyword() {
      return "TABLE";
   }

   /**
    * Create a non-unique index with a given name on the table over the given columns.
    *
    * @param name    the name of the index
    * @param columns the columns in the index
    * @return the index
    */
   public Index index(String name, @NonNull SQLElement... columns) {
      return new Index(this, name, false, Arrays.asList(columns));
   }

   /**
    * Create a non-unique index on the table over the given columns.
    *
    * @param columns the columns in the index
    * @return the index
    */
   public Index index(@NonNull SQLElement... columns) {
      return new Index(this, Arrays.asList(columns));
   }

   /**
    * Creates an Insert statement for this table automatically filling in the columns with those that are required and
    * setting the values to either be default (if no columns are required) or named parameters based on the column
    * name.
    *
    * @return thiae insert statement
    */
   public Insert insert() {
      return insert(InsertType.INSERT);
   }

   /**
    * Creates an Insert statement for this table automatically filling in the columns with those that are required and
    * setting the values to either be default (if no columns are required) or named parameters based on the column
    * name.
    *
    * @param insertType the type of insert to perform
    * @return the insert statement
    */
   public Insert insert(@NonNull InsertType insertType) {
      List<Column> c = columns.stream()
                              .filter(Column::isRequired)
                              .collect(Collectors.toList());
      if(c.isEmpty()) {
         return Insert.into(this).type(insertType).defaultValues();
      }
      return Insert.into(this).type(insertType).columns(c).namedParameters();
   }

   /**
    * Gets the qualified column name, i.e. <code>table.column</code>
    *
    * @param column the column name
    * @return the SQLOperable representing the qualified column name
    */
   public SQLOperable qualifiedColumnName(@NonNull Column column) {
      return SQL.C(getName(), column.getName());
   }

   /**
    * Gets the qualified column name, i.e. <code>table.column</code>
    *
    * @param column the column name
    * @return the SQLOperable representing the qualified column name
    */
   public SQLOperable qualifiedColumnName(@NonNull String column) {
      return SQL.C(getName(), column);
   }

   /**
    * Renames the column with the given name to the given new name generating an {@link AlterTable} statement to execute
    * on the database.
    *
    * @param name    the current column name
    * @param newName the new column name
    * @return the {@link AlterTable} statement needed to sync the code definition with the database definition
    */
   public AlterTable renameColumn(@NonNull String name, @NonNull String newName) {
      Column column = columns.stream()
                             .filter(c -> c.getName().equalsIgnoreCase(name))
                             .findFirst()
                             .orElseThrow(() -> new IllegalArgumentException("No column named +'" + name + "'"));
      column.setName(newName);
      return AlterTable.table(this).renameColumn(name, newName);
   }

   /**
    * Renames the table to the given new name generating an {@link AlterTable} statement to execute on the database.
    *
    * @param newName the new table name
    * @return the {@link AlterTable} statement needed to sync the code definition with the database definition
    */
   public AlterTable renameTable(@NonNull String newName) {
      this.name = newName;
      return AlterTable.table(this).renameTable(newName);
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
    * Creates a select statement selecting from this table returning the specified columns
    *
    * @param columns the columns to select
    * @return the select statement
    */
   public Select select(@NonNull String... columns) {
      return Select.from(this).columns(Stream.of(columns).map(SQL::sql).collect(Collectors.toList()));
   }

   /**
    * Creates a select statement selecting all columns from this table.
    *
    * @return the select statement
    */
   public Select selectAll() {
      return Select.from(this).columns(SQL.ALL);
   }

   @Override
   public String toString() {
      return name;
   }

   /**
    * Create a unique index with a given name on the table over the given columns.
    *
    * @param name    the name of the index
    * @param columns the columns in the index
    * @return the index
    */
   public Index uniqueIndex(String name, @NonNull SQLElement... columns) {
      return new Index(this, name, true, Arrays.asList(columns));
   }

   /**
    * Create a unique index on the table over the given columns.
    *
    * @param columns the columns in the index
    * @return the index
    */
   public Index uniqueIndex(@NonNull SQLElement... columns) {
      return new Index(this, "UNIQUE_" + Strings.randomHexString(5), true, Arrays.asList(columns));
   }

   /**
    * Creates an update statement for this table
    *
    * @return the update statement
    */
   public Update update() {
      return Update.table(this);
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

}//END OF TableDef
