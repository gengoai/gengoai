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
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.object.Table;
import com.gengoai.string.Strings;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>SQL Update statement</p>
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class Update implements UpdateStatement {
   private static final long serialVersionUID = 1L;
   @Getter
   private final SQLElement table;
   @Getter
   private final List<SQLElement> columns = new ArrayList<>();
   @Getter
   private final List<SQLElement> values = new ArrayList<>();
   @Getter
   private UpdateType updateType = UpdateType.UPDATE;
   @Getter
   private SQLElement where;

   private Update(@NonNull SQLElement table) {
      this.table = table;
   }

   /**
    * Creates a new Update statement which will update the given table
    *
    * @param table the table
    * @return the Update statement
    */
   public static Update table(@NonNull Table table) {
      return new Update(table);
   }

   /**
    * Creates a new Update statement which will update the given table
    *
    * @param table the table
    * @return the Update statement
    */
   public static Update table(@NonNull SQLElement table) {
      return new Update(table);
   }

   /**
    * Adds a set parameter to the update defined as a column and value.
    *
    * @param column the column to update
    * @param value  the value to update the column to
    * @return this Update statement
    */
   public Update set(@NonNull SQLElement column, @NonNull SQLElement value) {
      this.columns.add(column);
      this.values.add(value);
      return this;
   }

   /**
    * Adds a set parameter to the update defined as a column and value.
    *
    * @param column the column to update
    * @param value  the value to update the column to
    * @return this Update statement
    */
   public Update set(@NonNull String column, @NonNull SQLElement value) {
      return set(SQL.C(column), value);
   }

   /**
    * Adds a set parameter for the given column and using an indexed parameter value for use in {@link
    * java.sql.PreparedStatement}
    *
    * @param column the column to update
    * @return this Update statement
    */
   public Update setIndexedParameter(@NonNull SQLElement column) {
      return set(column, SQL.INDEXED_ARGUMENT);
   }

   /**
    * Adds a set parameter for the given column and using a named parameter value for use in {@link
    * com.gengoai.sql.NamedPreparedStatement}
    *
    * @param column the column to update
    * @return this Update statement
    */
   public Update setNamedParameter(@NonNull SQLElement column) {
      return set(column, SQL.namedArgument(column));
   }

   /**
    * Sets the type of update to perform.
    *
    * @param type the type of update to perform
    * @return this Update statement
    */
   public Update type(@NonNull UpdateType type) {
      this.updateType = type;
      return this;
   }

   /**
    * Sets the criteria (i.e. WHERE) for the update.
    *
    * @param whereClause the where clause
    * @return this Update statement
    */
   public Update where(SQLElement whereClause) {
      this.where = whereClause;
      return this;
   }

   /**
    * Sets the criteria (i.e. WHERE) for the update.
    *
    * @param whereClause the where clause
    * @return this Update statement
    */
   public Update where(String whereClause) {
      if (Strings.isNullOrBlank(whereClause)) {
         this.where = null;
      } else {
         this.where = SQL.sql(whereClause);
      }
      return this;
   }

}//END OF Update
