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

import com.gengoai.sql.NamedPreparedStatement;
import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.object.Table;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>SQL Insert Statement</p>
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class Insert implements UpdateStatement {
   private static final long serialVersionUID = 1L;
   @Getter
   private final SQLElement table;
   @Getter
   private final List<SQLElement> columns = new ArrayList<>();
   @Getter
   private final List<SQLElement> values = new ArrayList<>();
   @Getter
   private QueryStatement select;
   @Getter
   private boolean defaultValues = false;
   @Getter
   private InsertType insertType = InsertType.INSERT;
   @Getter
   private UpsertClause onConflict;

   private Insert(@NonNull SQLElement table) {
      this.table = table;
   }

   /**
    * Create an Insert statement that will insert into the given table.
    *
    * @param table the table to insert into
    * @return the Insert statement
    */
   public static Insert into(@NonNull Table table) {
      return new Insert(table);
   }

   /**
    * Create an Insert statement that will insert into the given {@link SQLElement}.
    *
    * @param table the table to insert into represented as a {@link SQLElement}
    * @return the Insert statement
    */
   public static Insert into(@NonNull SQLElement table) {
      return new Insert(table);
   }

   /**
    * Set the named columns being inserted (note that calling this removed previously specified columns).
    *
    * @param columns the columns to insert
    * @return this Insert statement
    */
   public Insert columns(@NonNull SQLElement... columns) {
      return columns(Arrays.asList(columns));
   }

   /**
    * Set the named columns being inserted (note that calling this removed previously specified columns).
    *
    * @param columns the columns to insert
    * @return this Insert statement
    */
   public Insert columns(@NonNull String... columns) {
      return columns(Stream.of(columns).map(SQL::sql).collect(Collectors.toList()));
   }

   /**
    * Set the named columns being inserted (note that calling this removed previously specified columns).
    *
    * @param columns the columns to insert
    * @return this Insert statement
    */
   public Insert columns(@NonNull Collection<? extends SQLElement> columns) {
      this.columns.clear();
      this.columns.addAll(columns);
      return this;
   }

   /**
    * Sets the values to be inserted as default (note that this will remove any previously set values or select
    * statement).
    *
    * @return this Insert statement
    */
   public Insert defaultValues() {
      this.defaultValues = true;
      this.select = null;
      this.values.clear();
      return this;
   }

   /**
    * Sets the values to be inserted as indexed parameters for use with {@link java.sql.PreparedStatement} (note that
    * this will remove any previously set values or select statement).
    *
    * @return this Insert statement
    */
   public Insert indexedParameters() {
      this.defaultValues = false;
      this.values.clear();
      this.select = null;
      columns.stream().map(c -> SQL.INDEXED_ARGUMENT).forEach(this.values::add);
      return this;
   }

   /**
    * Sets the values to be inserted as named parameters for use with {@link NamedPreparedStatement} (note that this
    * will remove any previously set values or select statement).
    *
    * @return this Insert statement
    */
   public Insert namedParameters() {
      this.defaultValues = false;
      this.values.clear();
      this.select = null;
      columns.stream()
             .map(SQL::namedArgument)
             .forEach(this.values::add);
      return this;
   }

   /**
    * Sets the upsert clause to use if an index conflict results from the insertion
    *
    * @param upsertClause the upsert clause
    * @return this Insert statement
    */
   public Insert onConflict(@NonNull UpsertClause upsertClause) {
      this.onConflict = upsertClause;
      return this;
   }

   /**
    * Sets the select statement to use for inserting values (note that this removes all previously set values or default
    * values).
    *
    * @param select the select statement to use for inserting values
    * @return this Insert statement
    */
   public Insert select(@NonNull QueryStatement select) {
      this.defaultValues = false;
      this.values.clear();
      this.select = select;
      return this;
   }

   /**
    * Sets the insertion type of the insert statement.
    *
    * @param insertType this Insert statement type
    * @return this Insert statement
    */
   public Insert type(@NonNull InsertType insertType) {
      this.insertType = insertType;
      return this;
   }

   /**
    * Sets the values to be inserted (note that this will remove any previously set values or select statement).
    *
    * @param values the values to insert
    * @return this Insert statement
    */
   public Insert values(@NonNull SQLElement... values) {
      return values(Arrays.asList(values));
   }

   /**
    * Sets the values to be inserted (note that this will remove any previously set values or select statement).
    *
    * @param values the values to insert
    * @return this Insert statement
    */
   public Insert values(@NonNull Collection<? extends SQLElement> values) {
      this.defaultValues = false;
      this.select = null;
      this.values.clear();
      this.values.addAll(values);
      return this;
   }

}//END OF Insert
