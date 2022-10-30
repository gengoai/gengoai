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
import com.gengoai.collection.Lists;
import com.gengoai.collection.Sets;
import com.gengoai.sql.NamedSQLElement;
import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLContext;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.constraint.Constraint;
import com.gengoai.sql.operator.SQLOperable;
import com.gengoai.sql.statement.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Table.
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Getter
public class Table extends SQLObject implements NamedSQLElement, SQLOperable {
   private static final long serialVersionUID = 1L;
   @NonNull
   private final List<Column> columns = new ArrayList<>();
   @NonNull
   private final List<Constraint> constraints = new ArrayList<>();
   private final SQLElement type;
   private final SQLElement using;

   /**
    * Instantiates a new Table.
    *
    * @param name      the name of the Table
    * @param tableType RDMS specific string defining type of table
    */
   public Table(String name, SQLElement tableType) {
      this(name, tableType, null, Collections.emptyList(), Collections.emptyList());
   }

   /**
    * Instantiates a new Table.
    *
    * @param name      the name of the Table
    * @param tableType RDMS specific string defining type of table
    */
   public Table(String name, SQLElement tableType, SQLElement using) {
      this(name, tableType, using, Collections.emptyList(), Collections.emptyList());
   }

   /**
    * Instantiates a new Table.
    *
    * @param name the name of the Table
    */
   public Table(String name) {
      this(name, null, null, Collections.emptyList(), Collections.emptyList());
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
                SQLElement tableType,
                SQLElement using,
                @NonNull List<Column> columns,
                @NonNull List<Constraint> constraints) {
      super(Validation.notNullOrBlank(name));
      this.type = tableType;
      this.columns.addAll(columns);
      this.constraints.addAll(constraints);
      this.using = using;
   }

   public static Builder builder(@NonNull String name) {
      return new Builder(name);
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
    * Adds the given {@link Constraint} to the table definition generating an {@link AlterTable} statement to perform on
    * the database.
    *
    * @param constraint the table constraint to add
    * @return the {@link AlterTable} statement needed to sync the code definition with the database definition
    */
   public AlterTable addConstraint(@NonNull Constraint constraint) {
      this.constraints.add(constraint);
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
    * Batch insert int.
    *
    * @param context the context
    * @param values  the values
    * @return the int
    * @throws SQLException the sql exception
    */
   public int batchInsert(@NonNull SQLContext context,
                          @NonNull Stream<Map<String, ?>> values) throws SQLException {
      return insert().columns(columns.stream()
                                     .filter(Column::isRequired)
                                     .collect(Collectors.toList()))
                     .namedParameters()
                     .batch(context, values, (m, nps) -> nps.fromMap(m));
   }

   /**
    * Batch upsert int.
    *
    * @param context        the context
    * @param indexedColumns the indexed columns
    * @param values         the values
    * @return the int
    * @throws SQLException the sql exception
    */
   public int batchUpsert(@NonNull SQLContext context,
                          @NonNull List<String> indexedColumns,
                          @NonNull Stream<Map<String, ?>> values) throws SQLException {
      Validation.checkArgument(indexedColumns.size() > 0, "Must define at least one index column");
      UpsertClause upsertClause = UpsertClause.upsert()
                                              .indexedColumns(Lists.transform(indexedColumns, SQL::C))
                                              .doUpdateSet();
      columns.stream()
             .filter(Column::isRequired)
             .filter(c -> indexedColumns.stream().noneMatch(ic -> SQL.columnNamesEqual(ic, c.getName())))
             .forEach(c -> upsertClause.set(c, SQL.C("excluded", c.getName())));
      return insert().columns(columns.stream()
                                     .filter(Column::isRequired)
                                     .collect(Collectors.toList()))
                     .namedParameters()
                     .onConflict(upsertClause)
                     .batch(context, values, (m, nps) -> nps.fromMap(m));
   }

   /**
    * Column column.
    *
    * @param name the name
    * @return the column
    */
   public Column column(String name) {
      Validation.notNullOrBlank(name);
      return columns.stream()
                    .filter(c -> SQL.columnNamesEqual(c.getName(), name))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
   }

   /**
    * Performs a <code>select count(0)</code> from this table.
    *
    * @param context the context to perform the query on
    * @return the result of the select count query
    * @throws SQLException something happened trying to query
    */
   public long count(@NonNull SQLContext context) throws SQLException {
      return Select.from(this).columns(SQL.F.count("0")).queryScalarLong(context);
   }

   /**
    * Deletes all rows matching the given where criteria
    *
    * @param context the {@link SQLContext} to perform the delete on
    * @param where   the criteria for row deletion
    * @return the number of items deleted
    * @throws SQLException something happened trying to update
    */
   public int delete(@NonNull SQLContext context, @NonNull SQLElement where) throws SQLException {
      return Delete.from(this).where(where).update(context);
   }

   /**
    * Deletes all rows from the table
    *
    * @param context the {@link SQLContext} to perform the delete on
    * @return the number of items deleted
    * @throws SQLException something happened trying to update
    */
   public int deleteAll(@NonNull SQLContext context) throws SQLException {
      return Delete.from(this).update(context);
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

   /**
    * Drop index boolean.
    *
    * @param context the context
    * @param name    the name
    * @return the boolean
    * @throws SQLException the sql exception
    */
   public boolean dropIndex(@NonNull SQLContext context, String name) throws SQLException {
      return new Index(this, Validation.notNullOrBlank(name), false, Collections.emptyList()).drop(context);
   }

   /**
    * Drop index if exists boolean.
    *
    * @param context the context
    * @param name    the name
    * @return the boolean
    * @throws SQLException the sql exception
    */
   public boolean dropIndexIfExists(@NonNull SQLContext context, String name) throws SQLException {
      return new Index(this, Validation.notNullOrBlank(name), false, Collections.emptyList()).dropIfExists(context);
   }

   /**
    * Checks if this table exists in the database using the given {@link SQLContext}
    *
    * @param context the context to use to check for the table's existence.
    * @return True if table exists, False otherwise
    * @throws SQLException something happened trying to query for table existence
    */
   public boolean exists(@NonNull SQLContext context) throws SQLException {
      try (ResultSet rs = context.getConnection().getMetaData()
                                 .getTables(null, null, getName(), new String[]{"TABLE"})) {
         return rs.next();
      }
   }

   public Column getColumn(@NonNull String name) {
      return columns.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
   }

   @Override
   public String getKeyword() {
      return "TABLE";
   }

   /**
    * Index index builder.
    *
    * @param name the name
    * @return the index builder
    */
   public IndexBuilder index(String name) {
      return Index.index(name, this);
   }

   /**
    * Creates an Insert statement for this table automatically filling in the columns with those that are required and
    * setting the values to either be default (if no columns are required) or named parameters based on the column
    * name.
    *
    * @return the insert statement
    */
   public Insert insert() {
      return insert(InsertType.INSERT_OR_FAIL);
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
      List<Column> c = columns.stream().filter(Column::isRequired).collect(Collectors.toList());
      if (c.isEmpty()) {
         return Insert.into(this).type(insertType).defaultValues();
      }
      return Insert.into(this).type(insertType).columns(c).namedParameters();
   }

   /**
    * Insert int.
    *
    * @param context the context
    * @param values  the values
    * @return the int
    * @throws SQLException the sql exception
    */
   public int insert(@NonNull SQLContext context, @NonNull Map<String, Object> values) throws SQLException {
      return insert().columns(Sets.transform(values.keySet(), SQL::C))
                     .namedParameters()
                     .update(context, values);
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
      AlterTable at = AlterTable.table(SQL.sql(this.name)).renameTable(newName);
      this.name = newName;
      return at;
   }

   /**
    * Creates a select statement selecting from this table returning the specified columns
    *
    * @param columns the columns to select
    * @return the select statement
    */
   public Select select(@NonNull String... columns) {
      return Select.from(this).columns(Lists.transform(Arrays.asList(columns), SQL::C));
   }

   /**
    * Creates a select statement selecting from this table returning the specified columns
    *
    * @param columns the columns to select
    * @return the select statement
    */
   public Select select(@NonNull SQLElement... columns) {
      return Select.from(this).columns(Arrays.asList(columns));
   }

   /**
    * Creates a select statement selecting from this table with nothing else specified
    *
    * @return the select statement
    */
   public Select select() {
      return Select.from(name);
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

   public int update(@NonNull SQLContext context,
                     @NonNull UpdateType updateType,
                     @NonNull Map<String, SQLElement> setParameters,
                     @NonNull SQLElement where) throws SQLException {
      Update update = update(updateType);
      setParameters.forEach(update::set);
      return update.where(where).update(context);
   }

   public int update(@NonNull SQLContext context,
                     @NonNull Map<String, SQLElement> setParameters,
                     @NonNull SQLElement where) throws SQLException {
      Update update = update();
      setParameters.forEach(update::set);
      return update.where(where).update(context);
   }

   /**
    * Perform an upsert on the table using the given context, list of columns to base the upsert on, and values to
    * upserted.
    *
    * @param context       the context to perform the upsert over
    * @param indexedColumn the column used to determine insert or upsert
    * @param values        the values of the row to be upserted
    * @return True if one or more rows were modified.
    * @throws SQLException the sql exception
    */
   public boolean upsert(@NonNull SQLContext context,
                         String indexedColumn,
                         @NonNull Map<String, Object> values) throws SQLException {
      return upsert(context, List.of(Validation.notNullOrBlank(indexedColumn)), values);
   }

   /**
    * Perform an upsert on the table using the given context, list of columns to base the upsert on, and values to
    * upserted.
    *
    * @param context        the context to perform the upsert over
    * @param indexedColumns the columns used to determine insert or upsert
    * @param values         the values of the row to be upserted
    * @return True if one or more rows were modified.
    * @throws SQLException the sql exception
    */
   public boolean upsert(@NonNull SQLContext context,
                         List<String> indexedColumns,
                         @NonNull Map<String, Object> values) throws SQLException {
      Validation.checkArgument(indexedColumns.size() > 0, "Must define at least one index column");
      UpsertClause upsertClause = UpsertClause.upsert()
                                              .indexedColumns(Lists.transform(indexedColumns, SQL::C))
                                              .doUpdateSet();
      for (String s : values.keySet()) {
         if (indexedColumns.stream().noneMatch(c -> SQL.columnNamesEqual(c, s))) {
            upsertClause.set(s, SQL.C("excluded", s));
         }
      }
      return insert().columns(Sets.transform(values.keySet(), SQL::C))
                     .namedParameters()
                     .onConflict(upsertClause)
                     .update(context, values) > 0;
   }

   @Data
   @Accessors(fluent = true)
   public static class Builder {
      protected final String name;
      protected List<Column> columns = new ArrayList<>();
      protected List<Constraint> constraints = new ArrayList<>();
      protected SQLElement type;
      protected SQLElement using;


      public Builder(String name) {
         this.name = Validation.notNullOrBlank(name);
      }

      public Table build() {
         return new Table(name, type, using, columns, constraints);
      }

      public Builder column(@NonNull Column column) {
         this.columns.add(column);
         return this;
      }

      public Builder constraint(@NonNull Constraint constraint) {
         this.constraints.add(constraint);
         return this;
      }

   }//END OF Builder


}//END OF Table
