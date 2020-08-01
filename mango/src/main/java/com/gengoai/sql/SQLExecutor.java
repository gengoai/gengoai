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

package com.gengoai.sql;

import com.gengoai.SystemInfo;
import com.gengoai.Validation;
import com.gengoai.concurrent.Broker;
import com.gengoai.concurrent.StreamProducer;
import com.gengoai.conversion.Converter;
import com.gengoai.function.CheckedBiConsumer;
import com.gengoai.function.CheckedConsumer;
import com.gengoai.function.Unchecked;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.sql.constraint.ColumnConstraint;
import com.gengoai.sql.constraint.TableConstraint;
import com.gengoai.sql.constraint.table.PrimaryKey;
import com.gengoai.sql.object.Column;
import com.gengoai.sql.object.Index;
import com.gengoai.sql.object.Table;
import com.gengoai.sql.operator.SQLOperable;
import com.gengoai.sql.statement.*;
import com.gengoai.stream.Streams;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;

/**
 * <p>An executor combines a JDBC Connection with an {@link SQLDialect} to perform updates and queries against a
 * database. The executor provides wrappers around JDBC elements that will auto close when they are no longer in
 * memory.</p>
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SQLExecutor implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The default batch size for batch operations when no batch size is given
    */
   public static int DEFAULT_BATCH_SIZE = 500;
   Connection connection;
   SQLDialect dialect;

   private static boolean columnNamesEqual(String c1, String c2) {
      if(c1 == null || c2 == null) {
         return false;
      }
      c1 = Strings.prependIfNotPresent(Strings.appendIfNotPresent(c1, "\""), "\"");
      c2 = Strings.prependIfNotPresent(Strings.appendIfNotPresent(c2, "\""), "\"");
      return c1.equalsIgnoreCase(c2);
   }

   /**
    * Creates an {@link SQLExecutor} from the given JDBC Connection and will generate SQL using the given {@link
    * SQLDialect}*.
    *
    * @param connection the connection to the database
    * @param dialect    the SQL dialect for converting Mango SQL objects to SQL statements.
    * @return the SQLExecutor
    */
   public static SQLExecutor create(@NonNull Connection connection, @NonNull SQLDialect dialect) {
      return new SQLExecutor(connection, dialect);
   }

   private static <T> int multiThreadedBatch(@NonNull Connection connection,
                                             @NonNull String sql,
                                             @NonNull Stream<? extends T> items,
                                             @NonNull CheckedBiConsumer<T, NamedPreparedStatement> consumer,
                                             int batchSize) throws SQLException {
      Validation.checkArgument(batchSize > 0, "Batch size must be > 0");
      boolean isAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
         StatementConsumer<T> statementConsumer = new StatementConsumer<>(connection,
                                                                          sql,
                                                                          consumer,
                                                                          batchSize);
         Broker<T> broker = Broker.<T>builder()
               .addProducer(new StreamProducer<>(items))
               .addConsumer(statementConsumer)
               .bufferSize((int) (batchSize * 1.5))
               .build();
         broker.run();
         statementConsumer.commit(0);
         return statementConsumer.updated;
      } catch(SQLException e) {
         throw e;
      } catch(Throwable throwable) {
         throw new SQLException(throwable.getCause());
      } finally {
         connection.setAutoCommit(isAutoCommit);
      }
   }

   /**
    * Performs a batch update over a Stream of items filling the {@link NamedPreparedStatement} using the given
    * CheckedBiConsumer.
    *
    * @param <T>             the type of object in the Stream
    * @param updateStatement the update statement
    * @param items           the items to use to fill in the values of the update statement
    * @param statementFiller a consumer that fills in a {@link NamedPreparedStatement} for an item in the Stream
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final <T> int batchUpdate(@NonNull SQLUpdateStatement updateStatement,
                                    @NonNull Stream<? extends T> items,
                                    @NonNull CheckedBiConsumer<T, NamedPreparedStatement> statementFiller) throws
                                                                                                           SQLException {
      return batchUpdate(updateStatement, items, statementFiller, DEFAULT_BATCH_SIZE);
   }

   /**
    * Performs a batch update over a Stream of items filling the {@link NamedPreparedStatement} using the given
    * CheckedBiConsumer.
    *
    * @param <T>             the type of object in the Stream
    * @param updateStatement the update statement
    * @param items           the items to use to fill in the values of the update statement
    * @param statementFiller a consumer that fills in a {@link NamedPreparedStatement} for an item in the Stream
    * @param batchSize       the size of the batch for updating the database
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final <T> int batchUpdate(@NonNull SQLUpdateStatement updateStatement,
                                    @NonNull Stream<? extends T> items,
                                    @NonNull CheckedBiConsumer<T, NamedPreparedStatement> statementFiller,
                                    int batchSize) throws SQLException {
      return multiThreadedBatch(connection, updateStatement.toSQL(dialect), items, statementFiller, batchSize);
   }

   /**
    * Performs a batch update over a Collection of items filling the {@link NamedPreparedStatement} using the given
    * CheckedBiConsumer.
    *
    * @param <T>             the type of object in the Stream
    * @param updateStatement the update statement
    * @param items           the items to use to fill in the values of the update statement
    * @param statementFiller a consumer that fills in a {@link NamedPreparedStatement} for an item in the Collection
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final <T> int batchUpdate(@NonNull SQLUpdateStatement updateStatement,
                                    @NonNull Collection<? extends T> items,
                                    @NonNull CheckedBiConsumer<T, NamedPreparedStatement> statementFiller) throws
                                                                                                           SQLException {
      return batchUpdate(updateStatement, items, statementFiller, DEFAULT_BATCH_SIZE);
   }

   /**
    * Performs a batch update over a Collection of items filling the {@link NamedPreparedStatement} using the given
    * CheckedBiConsumer.
    *
    * @param <T>             the type of object in the Stream
    * @param updateStatement the update statement
    * @param items           the items to use to fill in the values of the update statement
    * @param statementFiller a consumer that fills in a {@link NamedPreparedStatement} for an item in the Collection
    * @param batchSize       the size of the batch for updating the database
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final <T> int batchUpdate(@NonNull SQLUpdateStatement updateStatement,
                                    @NonNull Collection<? extends T> items,
                                    @NonNull CheckedBiConsumer<T, NamedPreparedStatement> statementFiller,
                                    int batchSize) throws SQLException {
      return multiThreadedBatch(connection,
                                updateStatement.toSQL(dialect),
                                items.parallelStream(),
                                statementFiller,
                                batchSize);
   }

   /**
    * Performs a batch update over a Collection of Maps whose keys are column names and values of the column.
    *
    * @param updateStatement the update statement
    * @param items           the items to use to fill in the values of the update statement
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final int batchUpdate(@NonNull SQLUpdateStatement updateStatement,
                                @NonNull Collection<Map<String, ?>> items) throws SQLException {
      return batchUpdate(updateStatement, items, DEFAULT_BATCH_SIZE);
   }

   /**
    * Performs a batch update over a Collection of Maps whose keys are column names and values of the column.
    *
    * @param updateStatement the update statement
    * @param items           the items to use to fill in the values of the update statement
    * @param batchSize       the size of the batch for updating the database
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final int batchUpdate(@NonNull SQLUpdateStatement updateStatement,
                                @NonNull Collection<Map<String, ?>> items,
                                int batchSize) throws SQLException {
      return multiThreadedBatch(connection,
                                updateStatement.toSQL(dialect),
                                items.stream(),
                                (m, nps) -> {
                                   for(Map.Entry<String, ?> e : m.entrySet()) {
                                      nps.setObject(NamedPreparedStatement.sanitize(e.getKey()), e.getValue());
                                   }
                                },
                                batchSize);
   }

   /**
    * Performs a batch update for a set of update statements that do not require any values to be specified. Note that
    * the entire collection of statements is performed as a single batch update.
    *
    * @param updateStatements the update statements
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final int batchUpdate(@NonNull SQLUpdateStatement... updateStatements) throws SQLException {
      return batchUpdate(Arrays.asList(updateStatements));
   }

   /**
    * Performs a batch update for a set of update statements that do not require any values to be specified. Note that
    * the entire collection of statements is performed as a single batch update.
    *
    * @param updateStatements the update statements
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final int batchUpdate(@NonNull Collection<? extends SQLUpdateStatement> updateStatements) throws
                                                                                                    SQLException {
      boolean isAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try(Statement statement = connection.createStatement()) {
         for(SQLUpdateStatement updateStatement : updateStatements) {
            statement.addBatch(updateStatement.toSQL(dialect));
         }
         int total = IntStream.of(statement.executeBatch()).sum();
         connection.commit();
         return total;
      } finally {
         connection.setAutoCommit(isAutoCommit);
      }
   }

   /**
    * Runs the given {@link SQLQueryStatement} returning true if there is at least on result.
    *
    * @param queryStatement the query statement to run
    * @return True if there is at least one result
    * @throws SQLException something happened trying to query
    */
   public boolean exists(@NonNull SQLQueryStatement queryStatement) throws SQLException {
      try(Statement statement = connection.createStatement()) {
         try(ResultSet rs = statement.executeQuery(queryStatement.toSQL(dialect))) {
            return rs.next();
         }
      }
   }

   /**
    * Checks if  the given {@link Table} exists in the database
    *
    * @param table the table whose existence we are checking for
    * @return True if table exists, False otherwise
    * @throws SQLException something happened trying to query for table existence
    */
   public boolean exists(@NonNull Table table) throws SQLException {
      return exists(table.getName());
   }

   /**
    * Checks if  the given {@link Table} exists in the database
    *
    * @param table the table whose existence we are checking for
    * @return True if table exists, False otherwise
    * @throws SQLException something happened trying to query for table existence
    */
   public boolean exists(@NonNull String table) throws SQLException {
      try(ResultSet rs = connection.getMetaData().getTables(null, null, table, new String[]{"TABLE"})) {
         return rs.next();
      }
   }

   private Tuple2<String, List<String>> getPrimaryKey(@NonNull String table) throws SQLException {
      try(ResultSet rs = connection.getMetaData().getPrimaryKeys(null, null, table)) {
         List<String> columns = new ArrayList<>();
         String name = null;
         while(rs.next()) {
            columns.add(rs.getString("COLUMN_NAME"));
            name = rs.getString("PK_NAME");
         }
         if(columns.size() > 0) {
            return $(name, columns);
         }
      }
      return null;
   }

   /**
    * Gets the list of columns (non-virtual) for the given table. Tries to infer the constraints on the columns and
    * whether or not the column is a primary key. In some cases the constraints will not be retrieved on the column, but
    * will be retrievable through a call to {@link #getTableIndices(Table)}.
    *
    * @param table the table name
    * @return the table columns
    * @throws SQLException Something went wrong learning about the table
    */
   public List<Column> getStoredColumns(@NonNull Table table) throws SQLException {
      return getStoredColumns(table.getName());
   }

   /**
    * Gets the list of columns (non-virtual) for the given table. Tries to infer the constraints on the columns and
    * whether or not the column is a primary key. In some cases the constraints will not be retrieved on the column, but
    * will be retrievable through a call to {@link #getTableIndices(Table)}.
    *
    * @param table the table name
    * @return the table columns
    * @throws SQLException Something went wrong learning about the table
    */
   public List<Column> getStoredColumns(@NonNull String table) throws SQLException {
      List<Column> columns = new ArrayList<>();
      final String primaryKey;
      Tuple2<String, List<String>> pk = getPrimaryKey(table);
      if(pk != null && pk.v2.size() == 1) {
         primaryKey = pk.v2.get(0);
      } else {
         primaryKey = null;
      }
      try(ResultSet rs = connection.getMetaData().getColumns(null, null, table, null)) {
         while(rs.next()) {
            String name = rs.getString("COLUMN_NAME");
            String typeName = rs.getString("TYPE_NAME");
            boolean isNullable = rs.getString("IS_NULLABLE").equalsIgnoreCase("YES");
            boolean isAutoIncrment = rs.getString("IS_AUTOINCREMENT").equalsIgnoreCase("YES");
            String generated = rs.getString("IS_GENERATEDCOLUMN");
            List<ColumnConstraint> constraints = new ArrayList<>();
            if(!isNullable) {
               constraints.add(ColumnConstraint.notNull());
            }
            if(Strings.isNotNullOrBlank(generated)) {
               constraints.add(ColumnConstraint.asStored(SQL.sql(generated)));
            }
            if(columnNamesEqual(name, primaryKey)) {
               constraints.add(ColumnConstraint.primaryKey(pk.v1));
            }
            if(isAutoIncrment) {
               constraints.add(ColumnConstraint.autoIncrement());
            }
            columns.add(new Column(name, typeName, constraints));
         }
      }
      return columns;
   }

   /**
    * Reverse engineers a {@link Table} definition from the database.
    *
    * @param name the name of the table to reverse engineer
    * @return the reverse engineered table
    * @throws SQLException Something went wrong building the table
    */
   public Table getTable(@NonNull String name) throws SQLException {
      Table table = new Table(name);
      getStoredColumns(name).forEach(table::addColumn);
      getVirtualColumns(name).forEach(table::addColumn);
      getTableConstraints(name).forEach(table::addConstraint);
      return table;
   }

   /**
    * Gets the table constraints for a given table
    *
    * @param table the table
    * @return the table constraints
    * @throws SQLException Something went wrong building the table constraints
    */
   public List<TableConstraint> getTableConstraints(@NonNull Table table) throws SQLException {
      return getTableConstraints(table.getName());
   }

   /**
    * Gets the table constraints for a given table
    *
    * @param table the table
    * @return the table constraints
    * @throws SQLException Something went wrong building the table constraints
    */
   public List<TableConstraint> getTableConstraints(@NonNull String table) throws SQLException {
      List<TableConstraint> tableConstraints = new ArrayList<>();
      Tuple2<String, List<String>> primaryKey = getPrimaryKey(table);
      if(primaryKey != null && primaryKey.v2.size() > 1) {
         tableConstraints.add(new PrimaryKey(primaryKey.v1, primaryKey.v2, null));
      }
      return tableConstraints;
   }

   /**
    * Retrieves the indices on table
    *
    * @param table the table whose indices we want
    * @return the list of {@link Index} on the table
    * @throws SQLException something happened trying to query for table indices
    */
   public List<Index> getTableIndices(@NonNull Table table) throws SQLException {
      return getTableIndices(table.getName());
   }

   /**
    * Retrieves the indices on table
    *
    * @param table the table whose indices we want
    * @return the list of {@link Index} on the table
    * @throws SQLException something happened trying to query for table indices
    */
   public List<Index> getTableIndices(@NonNull String table) throws SQLException {
      List<Index> indices = new ArrayList<>();
      try(ResultSet rs = connection.getMetaData().getIndexInfo(null, null, table, false, false)) {
         List<SQLElement> columns = new ArrayList<>();
         String name = null;
         boolean isUnique = false;
         while(rs.next()) {
            String column = rs.getString("COLUMN_NAME");
            int ordinal = rs.getInt("ORDINAL_POSITION");
            if(ordinal == 1 && columns.size() > 0) {
               indices.add(new Index(SQL.sql(table),
                                     name,
                                     isUnique,
                                     columns));
               columns.clear();
            }
            name = rs.getString("INDEX_NAME");
            isUnique = rs.getInt("NON_UNIQUE") == 0;
            SQLOperable c = SQL.C(column);
            if(Strings.safeEquals("A", rs.getString("ASC_OR_DESC"), false)) {
               c = c.asc();
            } else if(Strings.safeEquals("D", rs.getString("ASC_OR_DESC"), false)) {
               c = c.desc();
            }
            columns.add(c);
         }
         if(columns.size() > 0) {
            indices.add(new Index(SQL.sql(table),
                                  name,
                                  isUnique,
                                  columns));
         }
      }
      return indices;
   }

   /**
    * Gets a list of the table names in the database
    *
    * @return the table names
    * @throws SQLException Something went wrong getting the table names
    */
   public List<String> getTableNames() throws SQLException {
      List<String> tables = new ArrayList<>();
      try(ResultSet rs = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"})) {
         while(rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));
         }
      }
      return tables;
   }

   private List<Column> getVirtualColumns(@NonNull String table, @NonNull List<Column> columns) throws SQLException {
      List<Column> virtualColumns = new ArrayList<>();
      try(ResultSet rs = query(SQL.query("SELECT * FROM " + table))) {
         ResultSetMetaData metaData = rs.getMetaData();
         for(int i = 1; i <= metaData.getColumnCount(); i++) {
            String name = metaData.getColumnName(i);
            String type = metaData.getColumnTypeName(i);
            if(columns.stream().noneMatch(c -> columnNamesEqual(c.getName(), name))) {
               virtualColumns.add(new Column(name, type, ColumnConstraint.asVirtual(SQL.sql("?"))));
            }
         }
      }
      return virtualColumns;
   }

   /**
    * Attempts to the get the virtual columns for a given table. Note that it may not be possible to retrieve the
    * generated as statement that accompanies the virtual column.
    *
    * @param table the table name
    * @return the virtual columns
    * @throws SQLException Something went wrong getting the virtual names
    */
   public List<Column> getVirtualColumns(@NonNull Table table) throws SQLException {
      return getVirtualColumns(table.getName(), table.getColumns());
   }

   /**
    * Attempts to the get the virtual columns for a given table. Note that it may not be possible to retrieve the
    * generated as statement that accompanies the virtual column.
    *
    * @param table the table name
    * @return the virtual columns
    * @throws SQLException Something went wrong getting the virtual names
    */
   public List<Column> getVirtualColumns(@NonNull String table) throws SQLException {
      return getVirtualColumns(table, getStoredColumns(table));
   }

   /**
    * Performs the query specified in the given {@link SQLQueryStatement} filling in indexed value placeholders using
    * the given Map of values and returning the results as a {@link ResultSetIterator} using the given {@link
    * ResultSetMapper}* to map ResultSet to objects. This query method returns a parallel stream.
    *
    * @param <T>             the Iterator type parameter
    * @param select          the select statement
    * @param values          the values to use to fill the query statement (by Name)
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @param numPartitions   the number of partitions (controls the parallelization
    * @return the ResultSetIterator over the results of the given {@link SQLQueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> parallelQuery(@NonNull Select select,
                                            @NonNull List<Object> values,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn,
                                            int numPartitions
                                           ) throws SQLException {
      Validation.checkArgument(numPartitions > 0, "Number of partitions must be at least 1.");
      updateWithColumnMod(select, partitionColumn, numPartitions, "?");
      return IntStream.range(0, numPartitions)
                      .parallel()
                      .mapToObj(Unchecked.intFunction(i -> {
                         List<Object> v = new ArrayList<>(values);
                         v.add(i);
                         return query(select, v, mapper);
                      }))
                      .flatMap(s -> s);
   }

   /**
    * Performs the query specified in the given {@link SQLQueryStatement} filling in indexed value placeholders using
    * the given Map of values and returning the results as a {@link ResultSetIterator} using the given {@link
    * ResultSetMapper}* to map ResultSet to objects. This query method returns a parallel stream using 2 * the number
    * processors as the partition count.
    *
    * @param <T>             the Iterator type parameter
    * @param select          the select statement
    * @param values          the values to use to fill the query statement (by Name)
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @return the ResultSetIterator over the results of the given {@link SQLQueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> parallelQuery(@NonNull Select select,
                                            @NonNull List<Object> values,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn) throws SQLException {
      return parallelQuery(select, values, mapper, partitionColumn, SystemInfo.NUMBER_OF_PROCESSORS * 2);
   }

   /**
    * Performs the query specified in the given {@link SQLQueryStatement} filling in named value placeholders using the
    * given Map of values and returning the results as a {@link ResultSetIterator} using the given {@link
    * ResultSetMapper}* to map ResultSet to objects. This query method returns a parallel stream.
    *
    * @param <T>             the Iterator type parameter
    * @param select          the select statement
    * @param values          the values to use to fill the query statement (by Name)
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @param numPartitions   the number of partitions (controls the parallelization
    * @return the ResultSetIterator over the results of the given {@link SQLQueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> parallelQuery(@NonNull Select select,
                                            @NonNull Map<String, ?> values,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn,
                                            int numPartitions
                                           ) throws SQLException {
      Validation.checkArgument(numPartitions > 0, "Number of partitions must be at least 1.");
      final String pName = SQL.namedArgument(partitionColumn + "_mod").toString();
      updateWithColumnMod(select, partitionColumn, numPartitions, pName);
      return IntStream.range(0, numPartitions)
                      .parallel()
                      .mapToObj(Unchecked.intFunction(i -> {
                         Map<String, Object> m = new HashMap<>(values);
                         m.put(pName, i);
                         return query(select, m, mapper);
                      }))
                      .flatMap(s -> s);
   }

   /**
    * Performs the query specified in the given {@link SQLQueryStatement} filling in named value placeholders using the
    * given Map of values and returning the results as a {@link ResultSetIterator} using the given {@link
    * ResultSetMapper}* to map ResultSet to objects. This query method returns a parallel stream using 2 * the number
    * processors as the partition count.
    *
    * @param <T>             the Iterator type parameter
    * @param select          the select statement
    * @param values          the values to use to fill the query statement (by Name)
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @return the ResultSetIterator over the results of the given {@link SQLQueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> parallelQuery(@NonNull Select select,
                                            @NonNull Map<String, ?> values,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn) throws SQLException {
      return parallelQuery(select, values, mapper, partitionColumn, SystemInfo.NUMBER_OF_PROCESSORS * 2);
   }

   /**
    * Performs the query specified in the given {@link SQLQueryStatement}  using the given {@link  ResultSetMapper} to
    * map ResultSet to objects. This query method returns a parallel stream.
    *
    * @param <T>             the Iterator type parameter
    * @param select          the select statement
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @param numPartitions   the number of partitions (controls the parallelization
    * @return the ResultSetIterator over the results of the given {@link SQLQueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> parallelQuery(@NonNull Select select,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn,
                                            int numPartitions
                                           ) throws SQLException {
      Validation.checkArgument(numPartitions > 0, "Number of partitions must be at least 1.");
      updateWithColumnMod(select, partitionColumn, numPartitions, "?");
      return IntStream.range(0, numPartitions)
                      .parallel()
                      .mapToObj(Unchecked.intFunction(i -> query(select, Collections.singletonList(i), mapper)))
                      .flatMap(s -> s);
   }

   /**
    * Performs the query specified in the given {@link SQLQueryStatement}  using the given {@link  ResultSetMapper} to
    * map ResultSet to objects. This query method returns a parallel stream using 2 * the number processors as the
    * partition count.
    *
    * @param <T>             the Iterator type parameter
    * @param select          the select statement
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @return the ResultSetIterator over the results of the given {@link SQLQueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> parallelQuery(@NonNull Select select,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn) throws SQLException {
      return parallelQuery(select, mapper, partitionColumn, 2 * SystemInfo.NUMBER_OF_PROCESSORS);
   }

   public final <T> void parallelUpdate(@NonNull Update update,
                                        @NonNull CheckedConsumer<ResultSet> updater) {

   }

   private void print(ResultSet rs) throws SQLException {
      for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
         System.out.println(rs.getMetaData().getColumnName(i) + " (" + rs.getMetaData()
                                                                         .getColumnTypeName(i) + ") = " + rs.getObject(i));
      }
   }

   /**
    * Runs the given query returning a ResultSet
    *
    * @param queryStatement the query statement
    * @return the ResultSet
    * @throws SQLException Something went wrong querying
    */
   public final ResultSet query(@NonNull SQLQueryStatement queryStatement) throws SQLException {
      return connection.createStatement().executeQuery(queryStatement.toSQL(dialect));
   }

   /**
    * Performs the query specified in the given {@link SQLQueryStatement} returning the results as a {@link
    * ResultSetIterator}* using the given {@link ResultSetMapper} to map ResultSet to objects
    *
    * @param <T>            the Iterator type parameter
    * @param queryStatement the query statement
    * @param mapper         the mapper from ResultSet to Object
    * @return the ResultSetIterator over the results of the given {@link SQLQueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> query(@NonNull SQLQueryStatement queryStatement,
                                    @NonNull ResultSetMapper<? extends T> mapper) throws SQLException {
      return Streams.asStream(new ResultSetIterator<>(mapper,
                                                      connection.createStatement()
                                                                .executeQuery(queryStatement.toSQL(dialect))));
   }

   /**
    * Performs the query specified in the given {@link SQLQueryStatement} filling in indexed value placeholders using
    * the given List of values and returning the results as a {@link ResultSetIterator} using the given {@link
    * ResultSetMapper}* to map ResultSet to objects
    *
    * @param <T>            the Iterator type parameter
    * @param queryStatement the query statement
    * @param values         the values to use to fill the query statement (by Index)
    * @param mapper         the mapper from ResultSet to Object
    * @return the ResultSetIterator over the results of the given {@link SQLQueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> query(@NonNull SQLQueryStatement queryStatement,
                                    @NonNull List<Object> values,
                                    @NonNull ResultSetMapper<? extends T> mapper) throws SQLException {
      PreparedStatement preparedStatement = connection.prepareStatement(queryStatement.toSQL(dialect));
      for(int i = 0; i < values.size(); i++) {
         preparedStatement.setObject(i + 1, values.get(i));
      }
      return Streams.asStream(new ResultSetIterator<>(mapper, preparedStatement.executeQuery()));
   }

   /**
    * Performs the query specified in the given {@link SQLQueryStatement} filling in named value placeholders using the
    * given Map of values and returning the results as a {@link ResultSetIterator} using the given {@link
    * ResultSetMapper}* to map ResultSet to objects
    *
    * @param <T>            the Iterator type parameter
    * @param queryStatement the query statement
    * @param values         the values to use to fill the query statement (by Name)
    * @param mapper         the mapper from ResultSet to Object
    * @return the ResultSetIterator over the results of the given {@link SQLQueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> query(@NonNull SQLQueryStatement queryStatement,
                                    @NonNull Map<String, Object> values,
                                    @NonNull ResultSetMapper<? extends T> mapper) throws SQLException {
      NamedPreparedStatement preparedStatement = new NamedPreparedStatement(connection,
                                                                            queryStatement.toSQL(dialect));
      for(Map.Entry<String, Object> e : values.entrySet()) {
         preparedStatement.setObject(NamedPreparedStatement.sanitize(e.getKey()), e.getValue());
      }
      return Streams.asStream(new ResultSetIterator<>(mapper, preparedStatement.executeQuery()));
   }

   /**
    * Performs the given {@link SQLQueryStatement} which expects a single value for a single column to return. The
    * resultant value is converted into the given class type for return. If there is no value or the value cannot be
    * converted a <code>null</code> value is returned.
    *
    * @param <T>            the type parameter
    * @param queryStatement the query statement to perform
    * @param tClass         the class type desired for the scalar object
    * @return the scalar result
    * @throws SQLException something happened trying to query
    */
   public <T> T scalar(@NonNull SQLQueryStatement queryStatement,
                       @NonNull Class<T> tClass) throws SQLException {
      try(Statement statement = connection.createStatement()) {
         try(ResultSet rs = statement.executeQuery(queryStatement.toSQL(dialect))) {
            if(rs.next()) {
               return Converter.convertSilently(rs.getObject(1), tClass);
            }
            return null;
         }
      }
   }

   /**
    * Performs the given {@link SQLQueryStatement} which expects a single value for a single column to return. The
    * resultant value is converted into a Double return. If there is no value or the value cannot be converted a
    * <code>null</code> value is returned.
    *
    * @param queryStatement the query statement to perform
    * @return the scalar result
    * @throws SQLException something happened trying to query
    */
   public Double scalarDouble(@NonNull SQLQueryStatement queryStatement) throws SQLException {
      return scalar(queryStatement, Double.class);
   }

   /**
    * Performs the given {@link SQLQueryStatement} which expects a single value for a single column to return. The
    * resultant value is converted into a Long return. If there is no value or the value cannot be converted a
    * <code>null</code> value is returned.
    *
    * @param queryStatement the query statement to perform
    * @return the scalar result
    * @throws SQLException something happened trying to query
    */
   public Long scalarLong(@NonNull SQLQueryStatement queryStatement) throws SQLException {
      return scalar(queryStatement, Long.class);
   }

   /**
    * Performs the given {@link SQLQueryStatement} which expects a single value for a single column to return. The
    * resultant value is converted into a String return. If there is no value or the value cannot be converted a
    * <code>null</code> value is returned.
    *
    * @param queryStatement the query statement to perform
    * @return the scalar result
    * @throws SQLException something happened trying to query
    */
   public String scalarString(@NonNull SQLQueryStatement queryStatement) throws SQLException {
      return scalar(queryStatement, String.class);
   }

   /**
    * Convenience method for converting an {@link SQLStatement} into an SQL String.
    *
    * @param statement the statement to stringify
    * @return the SQL String
    */
   public String toSQL(@NonNull SQLStatement statement) {
      return statement.toSQL(dialect);
   }

   /**
    * Performs the update specified in the given {@link SQLUpdateStatement} filling in indexed value placeholders using
    * the given List of values.
    *
    * @param updateStatement the update statement
    * @param values          the values to use to fill the query statement (by Index)
    * @return the number of rows updated
    * @throws SQLException something happened trying to update
    */
   public final int update(@NonNull SQLUpdateStatement updateStatement,
                           @NonNull List<Object> values) throws SQLException {
      try(PreparedStatement statement = connection.prepareStatement(updateStatement.toSQL(dialect))) {
         for(int i = 0; i < values.size(); i++) {
            statement.setObject(i + 1, values.get(i));
         }
         return statement.executeUpdate();
      }
   }

   /**
    * Performs the update specified in the given {@link SQLUpdateStatement} filling in value placeholders using the
    * given value and the consumer to for filling the statement.
    *
    * @param <T>             the type parameter
    * @param updateStatement the update statement
    * @param value           the values to use for updating
    * @param statementFiller the consumer to use to fill the prepared statement.
    * @return the number of rows updated
    * @throws SQLException something happened trying to update
    */
   public final <T> int update(@NonNull SQLUpdateStatement updateStatement,
                               @NonNull T value,
                               @NonNull CheckedBiConsumer<? super T, NamedPreparedStatement> statementFiller) throws
                                                                                                              SQLException {
      try(NamedPreparedStatement statement = new NamedPreparedStatement(connection, updateStatement.toSQL(dialect))) {
         try {
            statementFiller.accept(value, statement);
         } catch(Throwable throwable) {
            throw new SQLException(throwable);
         }
         return statement.executeUpdate();
      }
   }

   /**
    * Performs the update specified in the given {@link SQLUpdateStatement} filling in named value placeholders using
    * the given Map of values.
    *
    * @param updateStatement the update statement
    * @param values          the values to use to fill the query statement (by Index)
    * @return the number of rows updated
    * @throws SQLException something happened trying to update
    */
   public final int update(@NonNull SQLUpdateStatement updateStatement,
                           @NonNull Map<String, Object> values) throws SQLException {
      try(NamedPreparedStatement statement = new NamedPreparedStatement(connection, updateStatement.toSQL(dialect))) {
         for(Map.Entry<String, Object> e : values.entrySet()) {
            statement.setObject(NamedPreparedStatement.sanitize(e.getKey()), e.getValue());
         }
         return statement.executeUpdate();
      }
   }

   /**
    * Performs the update specified in the given {@link SQLUpdateStatement}.
    *
    * @param updateStatement the update statement
    * @return the number of rows updated
    * @throws SQLException something happened trying to update
    */
   public final int update(@NonNull SQLUpdateStatement updateStatement) throws SQLException {
      try(Statement statement = connection.createStatement()) {
         return statement.executeUpdate(updateStatement.toSQL(dialect));
      }
   }

   private void updateWithColumnMod(Select select, String partitionColumn, int numPartitions, String pName) {
      SQLElement mod = SQL.sql(partitionColumn + " % " + numPartitions + " = " + pName);
      if(select.getWhere() == null) {
         select.where(mod);
      } else {
         select.where(SQL.and(select.getWhere(), mod));
      }
   }

   private static class StatementConsumer<T> implements Consumer<T> {
      /**
       * The Nps.
       */
      final MonitoredObject<NamedPreparedStatement> nps;
      /**
       * The Consumer.
       */
      final CheckedBiConsumer<T, NamedPreparedStatement> consumer;
      /**
       * The Counter.
       */
      final AtomicInteger counter = new AtomicInteger();
      /**
       * The Batch size.
       */
      final int batchSize;
      /**
       * The Con.
       */
      final Connection con;
      /**
       * The Updated.
       */
      int updated = 0;

      private StatementConsumer(Connection connection,
                                String sql,
                                CheckedBiConsumer<T, NamedPreparedStatement> consumer,
                                int batchSize) {
         try {
            this.con = connection;
            this.nps = ResourceMonitor.monitor(new NamedPreparedStatement(connection, sql));
            this.consumer = consumer;
            this.batchSize = batchSize;
         } catch(Throwable e) {
            throw new RuntimeException(e);
         }
      }

      @Override
      public void accept(T t) {
         try {
            consumer.accept(t, nps.object);
            nps.object.addBatch();
            commit(batchSize);
         } catch(Throwable e) {
            throw new RuntimeException(e);
         }
      }

      /**
       * Commit.
       *
       * @param min the min
       * @throws SQLException the sql exception
       */
      public void commit(int min) throws SQLException {
         if(counter.incrementAndGet() >= min) {
            synchronized(con) {
               updated += IntStream.of(nps.object.executeBatch()).sum();
               con.commit();
            }
            counter.set(0);
         }
      }
   }

}//END OF SqlExecutor
