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

package com.gengoai.apollo.ml;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.function.SerializableFunction;
import com.gengoai.function.Unchecked;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.sql.NamedPreparedStatement;
import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLExecutor;
import com.gengoai.sql.constraint.ColumnConstraint;
import com.gengoai.sql.object.*;
import com.gengoai.sql.sqlite.SQLiteConnectionRegistry;
import com.gengoai.sql.sqlite.SQLiteDialect;
import com.gengoai.sql.statement.InsertType;
import com.gengoai.sql.statement.Select;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;

public class SQLiteDataSet extends DataSet {
   private static final long serialVersionUID = 1L;
   private static final String SIZE_NAME = "__size__";
   private static final Column json = new Column("json", "JSON");
   private static final Column name = new Column("name", "TEXT", List.of(ColumnConstraint.primaryKey()));
   private static final Column value = new Column("value", "BLOB");
   private static final Table dataTable = new Table("data", List.of(json));
   private static final Table metadataTable = new Table("metadata", List.of(name, value));
   private final SQLExecutor executor;
   private boolean isShuffled = false;

   @SneakyThrows
   public SQLiteDataSet(@NonNull Stream<Datum> stream) {
      this(Resources.temporaryFile().deleteOnExit(), stream);
   }

   @SneakyThrows
   public SQLiteDataSet(@NonNull Resource location, @NonNull Stream<Datum> stream) {
      this(location);
      executor.batchUpdate(dataTable.insert(),
                           stream,
                           (d, nps) -> nps.setString(json.getName(), Json.dumps(d)), 1000);
   }

   @SneakyThrows
   public SQLiteDataSet(@NonNull Resource dataset) {
      this.executor = SQLExecutor.create(
            SQLiteConnectionRegistry.getConnection("jdbc:sqlite:" + Strings.prependIfNotPresent(dataset.path(),
                                                                                                "/")),
            SQLiteDialect.INSTANCE);
      if(!executor.exists(dataTable)) {
         executor.update(dataTable.create());
      }
      if(!executor.exists(metadataTable)) {
         executor.batchUpdate(metadataTable.create(),
                              metadataTable.insert(InsertType.INSERT_OR_REPLACE)
                                           .values(SQL.L(SIZE_NAME), SQL.N(0)),
                              Trigger.builder()
                                     .name("data_insert_size_inc")
                                     .table(dataTable)
                                     .operation(SQLDMLOperation.INSERT)
                                     .when(TriggerTime.AFTER)
                                     .updateStatement(metadataTable.update()
                                                                   .set(value,
                                                                        SQL.sql("cast(value as INTEGER)+1"))
                                                                   .where(name.eq(SQL.L(SIZE_NAME))))
                                     .build().create(),
                              Trigger.builder()
                                     .name("data_dete_size_dec")
                                     .table(dataTable)
                                     .operation(SQLDMLOperation.DELETE)
                                     .when(TriggerTime.AFTER)
                                     .updateStatement(metadataTable.update()
                                                                   .set(value,
                                                                        SQL.sql("cast(value as INTEGER)-1"))
                                                                   .where(name.eq(SQL.L(SIZE_NAME))))
                                     .build().create()
                             );
      } else {
         Stream<Map<String, ?>> stream = executor.query(metadataTable.select(name, value)
                                                                     .where(name.neq(SQL.L(SIZE_NAME))),
                                                        r -> Map.of(r.getString(name.getName()),
                                                                    r.getObject(value.getName())));
         stream.forEach(m -> m.forEach(Unchecked.biConsumer((source, metadata) -> {
            if(source.equals("ndArrayFactory")) {
               super.ndArrayFactory = NDArrayFactory.valueOf(metadata.toString());
            } else {
               super.metadata.put(source, Json.parse(metadata.toString(), ObservationMetadata.class));
            }
         })));
      }
   }

   @Override
   public Iterator<DataSet> batchIterator(int batchSize) {
      Validation.checkArgument(batchSize > 0);
      return new Iterator<>() {
         private final Iterator<Datum> itr = iterator();

         @Override
         public boolean hasNext() {
            return itr.hasNext();
         }

         @Override
         public DataSet next() {
            if(!itr.hasNext()) {
               throw new NoSuchElementException();
            }
            List<Datum> data = new ArrayList<>();
            while(itr.hasNext() && data.size() < batchSize) {
               data.add(itr.next());
            }
            return new InMemoryDataSet(data);
         }
      };
   }

   @Override
   public DataSet cache() {
      return this;
   }

   @Override
   public DataSetType getType() {
      return DataSetType.OnDisk;
   }

   @Override
   @SneakyThrows
   public Iterator<Datum> iterator() {
      return stream().iterator();
   }

   @Override
   public DataSet map(@NonNull SerializableFunction<? super Datum, ? extends Datum> function) {
      final Connection connection = executor.getConnection();
      try(NamedPreparedStatement preparedStatement = new NamedPreparedStatement(connection, dataTable.update()
                                                                                                     .set(json,
                                                                                                          SQL.namedArgument(
                                                                                                                "json"))
                                                                                                     .where(SQL.C(
                                                                                                           "rowid")
                                                                                                               .eq(SQL.namedArgument(
                                                                                                                     "rowid")))
                                                                                                     .toSQL(executor.getDialect()))) {
         AtomicLong processed = new AtomicLong(0);

         boolean isAutoCommit = connection.getAutoCommit();
         connection.setAutoCommit(false);
         parallelIdStream().forEach(Unchecked.consumer(t -> {
            long id = t.v1;
            Datum datum = function.apply(t.v2);
            synchronized(this) {
               preparedStatement.setObject("json", Json.dumps(datum));
               preparedStatement.setLong("rowid", id);
               preparedStatement.addBatch();
               if(processed.incrementAndGet() % SQLExecutor.DEFAULT_BATCH_SIZE == 0) {
                  preparedStatement.executeBatch();
                  connection.commit();
                  processed.set(0);
               }
            }
         }));

         if(processed.get() > 0) {
            preparedStatement.executeBatch();
            connection.commit();
         }
         connection.setAutoCommit(isAutoCommit);
      } catch(SQLException e) {
         throw new RuntimeException(e);
      }
      return this;
   }

   @SneakyThrows
   public MStream<Tuple2<Long, Datum>> parallelIdStream() {
      return StreamingContext.local()
                             .stream(executor.<Tuple2<Long, Datum>>parallelQuery(dataTable.select("rowid",
                                                                                                  json.getName()),
                                                                                 resultSet -> {
                                                                                    long id = resultSet.getLong("rowid");
                                                                                    Datum datum = Json.parse(resultSet.getString(
                                                                                          json.getName()), Datum.class);
                                                                                    return $(id, datum);
                                                                                 },
                                                                                 "rowid"));
   }

   @Override
   @SneakyThrows
   public MStream<Datum> parallelStream() {
      return StreamingContext.local().stream(executor.<Datum>parallelQuery(select(),
                                                                           resultSet -> Json.parse(resultSet.getString(1),
                                                                                                   Datum.class),
                                                                           "rowid"));
   }

   @SneakyThrows
   @Override
   public DataSet persist(@NonNull Resource copy) {
      String cmd = String.format("VACUUM main INTO '%s'", copy.path());
      executor.update(SQL.update(cmd));
      return new SQLiteDataSet(copy);
   }

   @Override
   @SneakyThrows
   public DataSet putAllMetadata(@NonNull Map<String, ObservationMetadata> metadata) {
      super.putAllMetadata(metadata);
      executor.batchUpdate(metadataTable.insert(InsertType.INSERT_OR_REPLACE),
                           metadata.entrySet(),
                           (e, nps) -> {
                              nps.setString(name.getName(), e.getKey());
                              nps.setObject(value.getName(), Json.dumps(e.getValue()));
                           });
      return this;
   }

   @Override
   @SneakyThrows
   public DataSet removeMetadata(@NonNull String source) {
      super.removeMetadata(source);
      executor.update(metadataTable.delete(name.eq(SQL.L(source))));
      return this;
   }

   private Select select() {
      if(isShuffled) {
         return dataTable.selectAll().orderBy(SQL.F.random());
      }
      return dataTable.selectAll();
   }

   @Override
   @SneakyThrows
   public DataSet setNDArrayFactory(@NonNull NDArrayFactory ndArrayFactory) {
      super.setNDArrayFactory(ndArrayFactory);
      executor.update(metadataTable.insert(InsertType.INSERT_OR_REPLACE)
                                   .values(SQL.L("ndArrayFactory"), SQL.L(ndArrayFactory.name())));
      return this;
   }

   @Override
   public DataSet shuffle(Random random) {
      isShuffled = true;
      return this;
   }

   @Override
   @SneakyThrows
   public long size() {
      return executor.scalarLong(metadataTable.select(value)
                                              .where(name.eq(SQL.L(SIZE_NAME))));
   }

   @Override
   @SneakyThrows
   public MStream<Datum> stream() {
      return StreamingContext.local()
                             .stream(executor.<Datum>query(select(), resultSet -> Json.parse(resultSet.getString(1),
                                                                                             Datum.class)));
   }

   @Override
   @SneakyThrows
   public DataSet updateMetadata(@NonNull String source, @NonNull Consumer<ObservationMetadata> updater) {
      super.updateMetadata(source, updater);
      executor.update(metadataTable.insert(InsertType.INSERT_OR_REPLACE),
                      Map.of(name.getName(), source,
                             value.getName(), Json.dumps(getMetadata(source))));
      return this;
   }
}//END OF SQLiteDataSet
