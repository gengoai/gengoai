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

import com.gengoai.function.CheckedBiConsumer;
import com.gengoai.sql.NamedPreparedStatement;
import com.gengoai.sql.SQLContext;
import com.gengoai.sql.SQLElement;
import lombok.NonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * SQL Statement that updates / modifies the database.
 */
public interface UpdateStatement extends SQLElement {
   /**
    * Performs a batch update over a Stream of items filling the {@link NamedPreparedStatement} using the given
    * CheckedBiConsumer.
    *
    * @param <T>             the type of object in the Stream
    * @param context         the context to perform this update statement on
    * @param items           the items to use to fill in the values of the update statement
    * @param statementFiller a consumer that fills in a {@link NamedPreparedStatement} for an item in the Stream
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   default <T> int batch(@NonNull SQLContext context,
                         @NonNull Stream<? extends T> items,
                         @NonNull CheckedBiConsumer<T, NamedPreparedStatement> statementFiller) throws SQLException {
      return batch(context, items, statementFiller, SQLContext.DEFAULT_BATCH_SIZE);
   }

   /**
    * Performs a batch update over a Stream of items filling the {@link NamedPreparedStatement} using the given
    * CheckedBiConsumer.
    *
    * @param <T>             the type of object in the Stream
    * @param context         the context to perform this update statement on
    * @param items           the items to use to fill in the values of the update statement
    * @param statementFiller a consumer that fills in a {@link NamedPreparedStatement} for an item in the Stream
    * @param batchSize       the size of the batch for updating the database
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   default <T> int batch(@NonNull SQLContext context,
                         @NonNull Stream<? extends T> items,
                         @NonNull CheckedBiConsumer<T, NamedPreparedStatement> statementFiller,
                         int batchSize) throws SQLException {
      return context.multiThreadedBatch(this,
                                        items,
                                        statementFiller,
                                        batchSize);
   }

   /**
    * Performs a batch update over a Collection of items filling the {@link NamedPreparedStatement} using the given
    * CheckedBiConsumer.
    *
    * @param <T>             the type of object in the Stream
    * @param context         the context to perform this update statement on
    * @param items           the items to use to fill in the values of the update statement
    * @param statementFiller a consumer that fills in a {@link NamedPreparedStatement} for an item in the Collection
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   default <T> int batch(@NonNull SQLContext context,
                         @NonNull Collection<? extends T> items,
                         @NonNull CheckedBiConsumer<T, NamedPreparedStatement> statementFiller) throws SQLException {
      return batch(context, items, statementFiller, SQLContext.DEFAULT_BATCH_SIZE);
   }

   /**
    * Performs a batch update over a Collection of items filling the {@link NamedPreparedStatement} using the given
    * CheckedBiConsumer.
    *
    * @param <T>             the type of object in the Stream
    * @param context         the context to perform this update statement on
    * @param items           the items to use to fill in the values of the update statement
    * @param statementFiller a consumer that fills in a {@link NamedPreparedStatement} for an item in the Collection
    * @param batchSize       the size of the batch for updating the database
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   default <T> int batch(@NonNull SQLContext context,
                         @NonNull Collection<? extends T> items,
                         @NonNull CheckedBiConsumer<T, NamedPreparedStatement> statementFiller,
                         int batchSize) throws SQLException {
      return context.multiThreadedBatch(this,
                                        items.parallelStream(),
                                        statementFiller,
                                        batchSize);
   }

   /**
    * Performs a batch update over a Collection of Maps whose keys are column names and values of the column.
    *
    * @param context the context to perform this update statement on
    * @param items   the items to use to fill in the values of the update statement
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   default int batch(@NonNull SQLContext context,
                     @NonNull Collection<Map<String, ?>> items) throws SQLException {
      return batch(context, items, SQLContext.DEFAULT_BATCH_SIZE);
   }

   /**
    * Performs a batch update over a Collection of Maps whose keys are column names and values of the column.
    *
    * @param context   the context to perform this update statement on
    * @param items     the items to use to fill in the values of the update statement
    * @param batchSize the size of the batch for updating the database
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   default int batch(@NonNull SQLContext context,
                     @NonNull Collection<Map<String, ?>> items,
                     int batchSize) throws SQLException {
      return context.multiThreadedBatch(this,
                                        items.parallelStream(),
                                        (m, nps) -> {
                                           for (Map.Entry<String, ?> e : m.entrySet()) {
                                              nps.setObject(NamedPreparedStatement.sanitize(e.getKey()), e
                                                    .getValue());
                                           }
                                        },
                                        batchSize);
   }

   /**
    * Performs the update using the given {@link SQLContext}
    *
    * @param context the context to perform this update statement on
    * @return the number of rows updated
    * @throws SQLException something happened trying to update
    */
   default int update(@NonNull SQLContext context) throws SQLException {
      try (Statement statement = context.getConnection().createStatement()) {
         return statement.executeUpdate(context.render(this));
      }
   }

   /**
    * Performs the update using the given {@link SQLContext} filling in named value placeholders using the given Map of
    * values.
    *
    * @param context the context to perform this update statement on
    * @param values  the values to use to fill the query statement (by Index)
    * @return the number of rows updated
    * @throws SQLException something happened trying to update
    */
   default int update(@NonNull SQLContext context,
                      @NonNull Map<String, Object> values) throws SQLException {
      try (NamedPreparedStatement statement = new NamedPreparedStatement(context.getConnection(), context
            .render(this))) {
         for (Map.Entry<String, Object> e : values.entrySet()) {
            statement.setObject(NamedPreparedStatement.sanitize(e.getKey()), e.getValue());
         }
         return statement.executeUpdate();
      }
   }

   /**
    * Performs the update using the given {@link SQLContext} filling in indexed value placeholders using the given List
    * of values.
    *
    * @param context the context to perform this update statement on
    * @param values  the values to use to fill the query statement (by Index)
    * @return the number of rows updated
    * @throws SQLException something happened trying to update
    */
   default int update(@NonNull SQLContext context,
                      @NonNull List<Object> values) throws SQLException {
      try (PreparedStatement statement = context.getConnection().prepareStatement(context.render(this))) {
         for (int i = 0; i < values.size(); i++) {
            statement.setObject(i + 1, values.get(i));
         }
         return statement.executeUpdate();
      }
   }

   /**
    * Performs the update s using the given {@link SQLContext} filling in value placeholders using the given value and
    * the consumer to for filling the statement.
    *
    * @param <T>             the type parameter
    * @param context         the context to perform this update statement on
    * @param value           the values to use for updating
    * @param statementFiller the consumer to use to fill the prepared statement.
    * @return the number of rows updated
    * @throws SQLException something happened trying to update
    */
   default <T> int update(@NonNull SQLContext context,
                          @NonNull T value,
                          @NonNull CheckedBiConsumer<? super T, NamedPreparedStatement> statementFiller) throws SQLException {
      try (NamedPreparedStatement statement = new NamedPreparedStatement(context.getConnection(), context
            .render(this))) {
         try {
            statementFiller.accept(value, statement);
         } catch (Throwable throwable) {
            throw new SQLException(throwable);
         }
         return statement.executeUpdate();
      }
   }
}//END OF UpdateStatement
