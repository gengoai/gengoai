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

import com.gengoai.SystemInfo;
import com.gengoai.Validation;
import com.gengoai.function.Unchecked;
import com.gengoai.sql.*;
import com.gengoai.string.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * <p>SQL statement for selecting data from a table</p>
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Select extends QueryStatement {
   private static final long serialVersionUID = 1L;
   @Getter
   private final List<SQLElement> columns = new ArrayList<>();
   @Getter
   private final List<SQLElement> from = new ArrayList<>();
   @Getter
   private final List<SQLElement> orderBy = new ArrayList<>();
   @Getter
   private final List<SQLElement> groupBy = new ArrayList<>();
   @Getter
   private boolean distinct = false;
   @Getter
   private SQLElement where;
   @Getter
   private SQLElement window;
   @Getter
   private SQLElement having;
   @Getter
   private SQLElement limit;

   private Select(@NonNull SQLElement from) {
      this.from.add(from);
   }

   /**
    * Creates a new Select statement with the given table for the from clause
    *
    * @param table the table to select from
    * @return this Select object
    */
   public static Select from(@NonNull SQLElement table) {
      return new Select(table);
   }

   /**
    * Creates a new Select statement with the given table for the from clause
    *
    * @param table the table to select from
    * @return this Select object
    */
   public static Select from(@NonNull String table) {
      return new Select(SQL.sql(table)).columns(SQL.ALL);
   }

   /**
    * Sets the columns being selected. Note that this clears any previously specified columns.
    *
    * @param columns the columns being selected
    * @return this Select object
    */
   public Select columns(@NonNull SQLElement... columns) {
      return columns(Arrays.asList(columns));
   }

   /**
    * Sets the columns being selected. Note that this clears any previously specified columns.
    *
    * @param columns the columns being selected
    * @return this Select object
    */
   public Select columns(@NonNull Collection<? extends SQLElement> columns) {
      this.columns.clear();
      this.columns.addAll(columns);
      return this;
   }

   /**
    * Performs a select distinct
    *
    * @return this Select object
    */
   public Select distinct() {
      this.distinct = true;
      return this;
   }

   /**
    * Sets whether or not the selct will be distinct
    *
    * @param distinct true - select distinct, false - normal select
    * @return this Select object
    */
   public Select distinct(boolean distinct) {
      this.distinct = distinct;
      return this;
   }

   /**
    * Sets the group by clause to the given strings. Note that this clears any previously specified groupBy.
    *
    * @param groupBy the group by statements
    * @return this Select object
    */
   public Select groupBy(@NonNull String... groupBy) {
      return groupBy(Stream.of(groupBy)
                           .map(SQL::sql)
                           .collect(Collectors.toList()));
   }

   /**
    * Sets the group by clause to the given strings. Note that this clears any previously specified groupBy.
    *
    * @param groupBy the group by statements
    * @return this Select object
    */
   public Select groupBy(@NonNull SQLElement... groupBy) {
      return groupBy(Arrays.asList(groupBy));
   }

   /**
    * Sets the group by clause to the given strings. Note that this clears any previously specified groupBy.
    *
    * @param groupBy the group by statements
    * @return this Select object
    */
   public Select groupBy(@NonNull Collection<? extends SQLElement> groupBy) {
      this.groupBy.clear();
      this.groupBy.addAll(groupBy);
      return this;
   }

   /**
    * Sets the criteria for group bys (i.e. HAVING) for the selection.
    *
    * @param havingClause the having clause
    * @return this Select object
    */
   public Select having(SQLElement havingClause) {
      this.having = havingClause;
      return this;
   }

   /**
    * Sets the criteria for group bys (i.e. HAVING) for the selection.
    *
    * @param havingClause the having clause
    * @return this Select object
    */
   public Select having(String havingClause) {
      if (Strings.isNullOrBlank(havingClause)) {
         this.having = null;
      } else {
         this.having = SQL.sql(havingClause);
      }
      return this;
   }

   /**
    * Adds an inner join with the given table on the given criteria to the from clause
    *
    * @param table    the table or expression to join with
    * @param criteria the criteria for joining
    * @return this Select object
    */
   public Select innerJoin(@NonNull SQLElement table, @NonNull SQLElement criteria) {
      return join(JoinType.INNER, table, criteria);
   }

   /**
    * Adds a join with the given table on the given criteria to the from clause
    *
    * @param type     the join type
    * @param table    the table or expression to join with
    * @param criteria the criteria for joining
    * @return this Select object
    */
   public Select join(@NonNull JoinType type, @NonNull SQLElement table, @NonNull SQLElement criteria) {
      this.from.add(new Join(type, table, criteria));
      return this;
   }

   /**
    * Adds a left outer join with the given table on the given criteria to the from clause
    *
    * @param table    the table or expression to join with
    * @param criteria the criteria for joining
    * @return this Select object
    */
   public Select leftOuterJoin(@NonNull SQLElement table, @NonNull SQLElement criteria) {
      return join(JoinType.RIGHT_OUTER, table, criteria);
   }

   /**
    * Sets the limit clause for selection
    *
    * @param limitClause the limit clause
    * @return this Select object
    */
   public Select limit(SQLElement limitClause) {
      this.limit = limitClause;
      return this;
   }

   /**
    * Sets the order by clause to the given strings. Note that this clears any previously specified orderBy.
    *
    * @param orderBy the order by statements
    * @return this Select object
    */
   public Select orderBy(@NonNull String... orderBy) {
      return orderBy(Stream.of(orderBy)
                           .map(SQL::sql)
                           .collect(Collectors.toList()));
   }

   /**
    * Sets the order by clause to the given strings. Note that this clears any previously specified orderBy.
    *
    * @param orderBy the order by statements
    * @return this Select object
    */
   public Select orderBy(@NonNull SQLElement... orderBy) {
      return orderBy(Arrays.asList(orderBy));
   }

   /**
    * Sets the order by clause to the given strings. Note that this clears any previously specified orderBy.
    *
    * @param orderBy the order by statements
    * @return this Select object
    */
   public Select orderBy(@NonNull Collection<? extends SQLElement> orderBy) {
      this.orderBy.clear();
      this.orderBy.addAll(orderBy);
      return this;
   }

   /**
    * Performs this Select query using the given {@link SQLContext} using the given {@link  ResultSetMapper} to map
    * ResultSet to objects. This query method returns a parallel stream using 2 * the number processors as the partition
    * count.
    *
    * @param <T>             the Iterator type parameter
    * @param context         the context to perform the select over
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @return the ResultSetIterator over the results of the given {@link QueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> queryParallel(@NonNull SQLContext context,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn) throws SQLException {
      return queryParallel(context, mapper, partitionColumn, 2 * SystemInfo.NUMBER_OF_PROCESSORS);
   }

   /**
    * Performs this Select query using the given {@link SQLContext}  filling in named value placeholders using the given
    * Map of values and returning the results as a {@link ResultSetIterator} using the given {@link ResultSetMapper}* to
    * map ResultSet to objects. This query method returns a parallel stream using 2 * the number processors as the
    * partition count.
    *
    * @param <T>             the Iterator type parameter
    * @param context         the context to perform the select over
    * @param values          the values to use to fill the query statement (by Name)
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @return the ResultSetIterator over the results of the given {@link QueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> queryParallel(@NonNull SQLContext context,
                                            @NonNull Map<String, ?> values,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn) throws SQLException {
      return queryParallel(context, values, mapper, partitionColumn, SystemInfo.NUMBER_OF_PROCESSORS * 2);
   }

   /**
    * Performs this Select query using the given {@link SQLContext} using the given {@link  ResultSetMapper} to map
    * ResultSet to objects. This query method returns a parallel stream.
    *
    * @param <T>             the Iterator type parameter
    * @param context         the context to perform the select over
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @param numPartitions   the number of partitions (controls the parallelization
    * @return the ResultSetIterator over the results of the given {@link QueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> queryParallel(@NonNull SQLContext context,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn,
                                            int numPartitions) throws SQLException {
      Validation.checkArgument(numPartitions > 0, "Number of partitions must be at least 1.");
      updateWithColumnMod(partitionColumn, numPartitions, "?");
      return IntStream.range(0, numPartitions)
                      .parallel()
                      .mapToObj(Unchecked.intFunction(i -> query(context, Collections.singletonList(i), mapper)))
                      .flatMap(s -> s);
   }

   /**
    * Performs this Select query using the given {@link SQLContext} filling in indexed value placeholders using the
    * given Map of values and returning the results as a {@link ResultSetIterator} using the given {@link
    * ResultSetMapper} to map ResultSet to objects. This query method returns a parallel stream using 2 times the number
    * processors as the partition count.
    *
    * @param <T>             the Iterator type parameter
    * @param context         the context to perform the select over
    * @param values          the values to use to fill the query statement (by Name)
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @return the ResultSetIterator over the results of the given {@link QueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> queryParallel(@NonNull SQLContext context,
                                            @NonNull List<Object> values,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn) throws SQLException {
      return queryParallel(context, values, mapper, partitionColumn, SystemInfo.NUMBER_OF_PROCESSORS * 2);
   }

   /**
    * Performs this Select query using the given {@link SQLContext} filling in named value placeholders using the given
    * Map of values and returning the results as a {@link ResultSetIterator} using the given {@link ResultSetMapper} to
    * map ResultSet to objects. This query method returns a parallel stream.
    *
    * @param <T>             the Iterator type parameter
    * @param context         the context to perform the select over
    * @param values          the values to use to fill the query statement (by Name)
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @param numPartitions   the number of partitions (controls the parallelization
    * @return the ResultSetIterator over the results of the given {@link QueryStatement}
    * @throws SQLException something happened trying to query
    */
   public <T> Stream<T> queryParallel(@NonNull SQLContext context,
                                      @NonNull Map<String, ?> values,
                                      @NonNull ResultSetMapper<? extends T> mapper,
                                      @NonNull String partitionColumn,
                                      int numPartitions
   ) throws SQLException {
      Validation.checkArgument(numPartitions > 0, "Number of partitions must be at least 1.");
      final String pName = SQL.namedArgument(partitionColumn + "_mod").toString();
      updateWithColumnMod(partitionColumn, numPartitions, pName);
      return IntStream.range(0, numPartitions)
                      .parallel()
                      .mapToObj(Unchecked.intFunction(i -> {
                         Map<String, Object> m = new HashMap<>(values);
                         m.put(pName, i);
                         return query(context, m, mapper);
                      }))
                      .flatMap(s -> s);
   }

   /**
    * Performs this Select query using the given {@link SQLContext} filling  in indexed value placeholders using the
    * given Map of values and returning the results as a {@link ResultSetIterator} using the given {@link
    * ResultSetMapper}* to map ResultSet to objects. This query method returns a parallel stream.
    *
    * @param <T>             the Iterator type parameter
    * @param context         the context to perform the select over
    * @param values          the values to use to fill the query statement (by Name)
    * @param mapper          the mapper from ResultSet to Object
    * @param partitionColumn the column to use for portioning (should be numeric)
    * @param numPartitions   the number of partitions (controls the parallelization
    * @return the ResultSetIterator over the results of the given {@link QueryStatement}
    * @throws SQLException something happened trying to query
    */
   public final <T> Stream<T> queryParallel(@NonNull SQLContext context,
                                            @NonNull List<?> values,
                                            @NonNull ResultSetMapper<? extends T> mapper,
                                            @NonNull String partitionColumn,
                                            int numPartitions
   ) throws SQLException {
      Validation.checkArgument(numPartitions > 0, "Number of partitions must be at least 1.");
      updateWithColumnMod(partitionColumn, numPartitions, "?");
      return IntStream.range(0, numPartitions)
                      .parallel()
                      .mapToObj(Unchecked.intFunction(i -> {
                         List<Object> v = new ArrayList<>(values);
                         v.add(i);
                         return query(context, v, mapper);
                      }))
                      .flatMap(s -> s);
   }

   /**
    * Adds a right outer join with the given table on the given criteria to the from clause
    *
    * @param table    the table or expression to join with
    * @param criteria the criteria for joining
    * @return this Select object
    */
   public Select rightOuterJoin(@NonNull SQLElement table, @NonNull SQLElement criteria) {
      return join(JoinType.LEFT_OUTER, table, criteria);
   }


   private void updateWithColumnMod(String partitionColumn, int numPartitions, String pName) {
      SQLElement mod = SQL.sql(partitionColumn + " % " + numPartitions + " = " + pName);
      if (getWhere() == null) {
         where(mod);
      } else {
         where(SQL.and(getWhere(), mod));
      }
   }

   /**
    * Sets the criteria (i.e. WHERE) for the selection.
    *
    * @param whereClause the where clause
    * @return this Select object
    */
   public Select where(SQLElement whereClause) {
      this.where = whereClause;
      return this;
   }

   /**
    * Sets the criteria (i.e. WHERE) for the selection.
    *
    * @param whereClause the where clause
    * @return this Select object
    */
   public Select where(String whereClause) {
      if (Strings.isNullOrBlank(whereClause)) {
         this.where = null;
      } else {
         this.where = SQL.sql(whereClause);
      }
      return this;
   }

   /**
    * Sets the Window function for the selection.
    *
    * @param window the window clause
    * @return this Select object
    */
   public Select window(SQLElement window) {
      this.window = window;
      return this;
   }

   /**
    * Sets the Window function for the selection.
    *
    * @param window the window clause
    * @return this Select object
    */
   public Select window(String window) {
      if (Strings.isNullOrBlank(window)) {
         this.window = null;
      } else {
         this.window = SQL.sql(window);
      }
      return this;
   }

}//END OF Select
