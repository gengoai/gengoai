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

import com.gengoai.Validation;
import com.gengoai.concurrent.Broker;
import com.gengoai.concurrent.StreamProducer;
import com.gengoai.config.Config;
import com.gengoai.function.CheckedBiConsumer;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.json.Json;
import com.gengoai.sql.object.SQLDMLOperation;
import com.gengoai.sql.object.Trigger;
import com.gengoai.sql.object.TriggerTime;
import com.gengoai.sql.sqlite.SQLiteConnectionRegistry;
import com.gengoai.sql.statement.UpdateStatement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gengoai.sql.SQL.table;


/**
 * <p>A context combines a JDBC Connection with an {@link SQLDialect} to perform updates and queries against a
 * database. The executor provides wrappers around JDBC elements that will auto close when they are no longer in
 * memory.</p>
 */
@Value(staticConstructor = "create")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SQLContext implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The default batch size for batch operations when no batch size is given
    */
   public static int DEFAULT_BATCH_SIZE = 500;
   Connection connection;
   SQLDialect dialect;

   /**
    * The entry point of application.
    *
    * @param args the input arguments
    * @throws Exception the exception
    */
   public static void main(String[] args) throws Exception {
      Config.initializeTest();
      SQLContext context = SQLContext.create(ResourceMonitor.monitor(DriverManager
                                                                           .getConnection("jdbc:postgresql://192.168.0.27:5433/hermes?user=postgres&password=qazwsx&stringtype=unspecified")),
                                             new PostgreSQLDialect());

      SQLContext.create(SQLiteConnectionRegistry.getConnection("jdbc:sqlite:/data/fried.db"),
                        new SQLiteDialect());

      SQLDialect.LOG_LEVEL = Level.INFO;


      var docsTable = table("documents", $ -> {
         //Every document has a unique - auto incremented id as its primary key
         $.column("id", "INTEGER").primaryKey().autoIncrement();

         //Documents are stored in json
         $.column("json", "JSON");
      });

      var metadataTable = table("metadata", $ -> {
         $.column("name", "TEXT").primaryKey();
         $.column("value", "BIGINT");
      });


      docsTable.dropIfExists(context);
      docsTable.createIfNotExists(context);
      metadataTable.dropIfExists(context);
      metadataTable.createIfNotExists(context);

      Trigger.builder()
             .table(docsTable)
             .name("add_document")
             .operation(SQLDMLOperation.INSERT)
             .when(TriggerTime.AFTER)
             .updateStatement(metadataTable.update()
                                           .set("value", SQL.C("value").add(1))
                                           .where(SQL.C("name").eq(SQL.L("size"))))
             .build()
             .createIfNotExists(context);


      metadataTable.insert(context, Map.of("name", "size", "value", 1));


      docsTable.batchInsert(context,
                            Stream.of(
                                  Map.of("json", Json.dumps(Map.of("name", "David"))),
                                  Map.of("json", Json.dumps(Map.of("name", "Larry"))),
                                  Map.of("json", Json.dumps(Map.of("name", "Curly"))),
                                  Map.of("json", Json.dumps(Map.of("name", "Moe")))
                            ));

   }

   /**
    * Creates an {@link SQLContext} from the given JDBC Connection and will generate SQL using the given {@link
    * SQLDialect}**.
    *
    * @param connection the connection to the database
    * @param dialect    the SQL dialect for converting Mango SQL objects to SQL statements.
    * @return the SQLExecutor
    */
   public static SQLContext create(@NonNull Connection connection, @NonNull SQLDialect dialect) {
      return new SQLContext(connection, dialect);
   }

   /**
    * Performs a batch update for a set of update statements that do not require any values to be specified. Note that
    * the entire collection of statements is performed as a single batch update.
    *
    * @param updateStatements the update statements
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final int batch(@NonNull UpdateStatement... updateStatements) throws SQLException {
      return batch(Arrays.asList(updateStatements));
   }

   /**
    * Performs a batch update for a set of update statements that do not require any values to be specified. Note that
    * the entire collection of statements is performed as a single batch update.
    *
    * @param updateStatements the update statements
    * @return the total number of items that were updated
    * @throws SQLException something happened trying to update
    */
   public final int batch(@NonNull Collection<? extends UpdateStatement> updateStatements) throws SQLException {
      boolean isAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try (Statement statement = connection.createStatement()) {
         for (UpdateStatement updateStatement : updateStatements) {
            statement.addBatch(dialect.render(updateStatement));
         }
         int total = IntStream.of(statement.executeBatch()).sum();
         connection.commit();
         return total;
      } finally {
         connection.setAutoCommit(isAutoCommit);
      }
   }


   /**
    * Multi threaded batch int.
    *
    * @param <T>       the type parameter
    * @param sql       the sql
    * @param items     the items
    * @param consumer  the consumer
    * @param batchSize the batch size
    * @return the int
    * @throws SQLException the sql exception
    */
   public <T> int multiThreadedBatch(@NonNull SQLElement sql,
                                     @NonNull Stream<? extends T> items,
                                     @NonNull CheckedBiConsumer<T, NamedPreparedStatement> consumer,
                                     int batchSize) throws SQLException {
      Validation.checkArgument(batchSize > 0, "Batch size must be > 0");
      boolean isAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
         StatementConsumer<T> statementConsumer = new StatementConsumer<>(connection,
                                                                          render(sql),
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
      } catch (SQLException e) {
         throw e;
      } catch (Throwable throwable) {
         throw new SQLException(throwable.getCause());
      } finally {
         connection.setAutoCommit(isAutoCommit);
      }
   }

   /**
    * Render string.
    *
    * @param sql the sql
    * @return the string
    */
   public String render(@NonNull SQLElement sql) {
      return dialect.render(sql);
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
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      @Override
      public void accept(T t) {
         try {
            consumer.accept(t, nps.object);
            nps.object.addBatch();
            commit(batchSize);
         } catch (Throwable e) {
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
         try {
            if (counter.incrementAndGet() >= min) {
               synchronized (con) {
                  updated += IntStream.of(nps.object.executeBatch()).sum();
                  con.commit();
               }
               counter.set(0);
            }
         } catch (SQLException e) {
            if (e.getNextException() != null) {
               throw e.getNextException();
            } else {
               throw e;
            }
         }
      }
   }


}//END OF SQLContext
