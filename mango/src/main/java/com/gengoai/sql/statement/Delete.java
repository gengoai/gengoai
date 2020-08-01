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

import com.gengoai.string.Strings;
import lombok.*;
import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.object.Table;

/**
 * <p>SQL DELETE statement for delete rows from a table.</p>
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Getter
public class Delete implements SQLUpdateStatement {
   private final SQLElement table;
   private SQLElement where;

   /**
    * Creates a Delete statement to delete from the given table
    *
    * @param table the table
    * @return the delete statement
    */
   public static Delete from(@NonNull String table) {
      return new Delete(SQL.C(table));
   }

   /**
    * Creates a Delete statement to delete from the given table
    *
    * @param table the table
    * @return the delete statement
    */
   public static Delete from(@NonNull Table table) {
      return new Delete(table);
   }

   /**
    * Creates a Delete statement to delete from the given table
    *
    * @param table the table
    * @return the delete statement
    */
   public static Delete from(@NonNull SQLElement table) {
      return new Delete(table);
   }

   private Delete(@NonNull SQLElement table) {
      this.table = table;
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      return dialect.delete(this);
   }

   /**
    * Sets the criteria (i.e. WHERE) for the deletion.
    *
    * @param whereClause the where clause
    * @return this Delete object
    */
   public Delete where(SQLElement whereClause) {
      this.where = whereClause;
      return this;
   }

   /**
    * Sets the criteria (i.e. WHERE) for the deletion.
    *
    * @param whereClause the where clause
    * @return this Delete object
    */
   public Delete where(String whereClause) {
      if(Strings.isNullOrBlank(whereClause)) {
         this.where = null;
      } else {
         this.where = SQL.sql(whereClause);
      }
      return this;
   }
}//END OF Delete
