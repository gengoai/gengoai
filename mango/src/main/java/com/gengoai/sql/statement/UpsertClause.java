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

import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.SQLFormattable;
import com.gengoai.string.Strings;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Defines an upsert clause when there is a key conflict on an {@link Insert} statement</p>
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class UpsertClause implements SQLFormattable {
   private static final long serialVersionUID = 1L;
   @Getter
   private final List<SQLElement> indexedColumns = new ArrayList<>();
   @Getter
   private final List<SQLElement> updateColumns = new ArrayList<>();
   @Getter
   private final List<SQLElement> updateValues = new ArrayList<>();
   @Getter
   private UpsertAction action = UpsertAction.NOTHING;
   @Getter
   private SQLElement indexedWhere;
   @Getter
   private SQLElement updateWhere;

   /**
    * Creates a new Upsert clause
    *
    * @return the upsert clause
    */
   public static UpsertClause upsert() {
      return new UpsertClause();
   }

   /**
    * Creates a new Upsert clause with given columns as the keys (indexed columns)
    *
    * @param columns the key (index) columns
    * @return this upsert clause
    */
   public static UpsertClause upsert(@NonNull String... columns) {
      return new UpsertClause().indexedColumns(columns);
   }

   /**
    * Creates a new Upsert clause with given columns as the keys (indexed columns)
    *
    * @param columns the key (index) columns
    * @return this upsert clause
    */
   public static UpsertClause upsert(@NonNull SQLElement... columns) {
      return new UpsertClause().indexedColumns(columns);
   }

   /**
    * The action to perform on upsert
    *
    * @param action the action to perform on upsert
    * @return this upsert clause
    */
   public UpsertClause action(@NonNull UpsertAction action) {
      this.action = action;
      return this;
   }

   /**
    * Do nothing on upsert
    *
    * @return this upsert clause
    */
   public UpsertClause doNothing() {
      this.action = UpsertAction.NOTHING;
      return this;
   }

   /**
    * Perform an update on upsert
    *
    * @return this upsert clause
    */
   public UpsertClause doUpdateSet() {
      this.action = UpsertAction.UPDATE_SET;
      return this;
   }

   /**
    * Sets the columns that specifies the keys (indexed columns) for the upsert
    *
    * @param columns the key (index) columns
    * @return this upsert clause
    */
   public UpsertClause indexedColumns(@NonNull String... columns) {
      return indexedColumns(Stream.of(columns).map(SQL::sql).collect(Collectors.toList()));
   }

   /**
    * Sets the columns that specifies the keys (indexed columns) for the upsert
    *
    * @param columns the key (index) columns
    * @return this upsert clause
    */
   public UpsertClause indexedColumns(@NonNull SQLElement... columns) {
      return indexedColumns(Arrays.asList(columns));
   }

   /**
    * Sets the columns that specifies the keys (indexed columns) for the upsert
    *
    * @param columns the key (index) columns
    * @return this upsert clause
    */
   public UpsertClause indexedColumns(@NonNull Collection<? extends SQLElement> columns) {
      this.indexedColumns.clear();
      this.indexedColumns.addAll(columns);
      return this;
   }

   /**
    * Indexed parameters upsert clause.
    *
    * @return this upsert clause
    */
   public UpsertClause indexedParameters() {
      this.updateValues.clear();
      updateColumns.stream().map(c -> SQL.INDEXED_ARGUMENT).forEach(this.updateValues::add);
      return this;
   }

   /**
    * Sets the criteria (i.e. WHERE) for the UpsertClause.
    *
    * @param whereClause the where clause
    * @return this UpsertClause object
    */
   public UpsertClause indexedWhere(String whereClause) {
      if(Strings.isNullOrBlank(whereClause)) {
         this.indexedWhere = null;
      } else {
         this.indexedWhere = SQL.sql(whereClause);
      }
      return this;
   }

   /**
    * Sets the criteria (i.e. WHERE) for the UpsertClause.
    *
    * @param whereClause the where clause
    * @return this UpsertClause object
    */
   public UpsertClause indexedWhere(SQLElement whereClause) {
      this.indexedWhere = whereClause;
      return this;
   }

   /**
    * Adds a set parameter to the update defined as a column and value.
    *
    * @param column the column to update
    * @param value  the value to update the column to
    * @return this UpsertClause statement
    */
   public UpsertClause set(@NonNull SQLElement column, @NonNull SQLElement value) {
      this.updateColumns.add(column);
      this.updateValues.add(value);
      return this;
   }

   /**
    * Adds a set parameter to the update defined as a column and value.
    *
    * @param column the column to update
    * @param value  the value to update the column to
    * @return this UpsertClause statement
    */
   public UpsertClause set(@NonNull String column, @NonNull SQLElement value) {
      return set(SQL.C(column), value);
   }

   /**
    * Adds a set parameter for the given column and using an indexed parameter value for use in {@link
    * java.sql.PreparedStatement}
    *
    * @param column the column to update
    * @return this UpsertClause statement
    */
   public UpsertClause setIndexedParameter(@NonNull SQLElement column) {
      return set(column, SQL.INDEXED_ARGUMENT);
   }

   /**
    * Adds a set parameter for the given column and using a named parameter value for use in {@link
    * com.gengoai.sql.NamedPreparedStatement}
    *
    * @param column the column to update
    * @return this UpsertClause statement
    */
   public UpsertClause setNamedParameter(@NonNull SQLElement column) {
      return set(column, SQL.namedArgument(column));
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      return dialect.toSQL(this);
   }

   /**
    * Sets the criteria (i.e. WHERE) for the UpsertClause.
    *
    * @param whereClause the where clause
    * @return this UpsertClause object
    */
   public UpsertClause updateWhere(String whereClause) {
      if(Strings.isNullOrBlank(whereClause)) {
         this.updateWhere = null;
      } else {
         this.updateWhere = SQL.sql(whereClause);
      }
      return this;
   }

   /**
    * Sets the criteria (i.e. WHERE) for the UpsertClause.
    *
    * @param whereClause the where clause
    * @return this UpsertClause object
    */
   public UpsertClause updateWhere(SQLElement whereClause) {
      this.updateWhere = whereClause;
      return this;
   }

}//END OF UpsertClause
