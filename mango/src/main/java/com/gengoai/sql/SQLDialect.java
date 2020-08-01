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

import com.gengoai.sql.constraint.ConflictClause;
import com.gengoai.sql.object.Column;
import com.gengoai.sql.statement.*;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>A dialect converts {@link SQLElement} into a specific SQL dialect corresponding to a specific RDBMS (e.g. SQLite,
 * PostgreSQL, etc.)</p>
 */
public interface SQLDialect extends Serializable {

   /**
    * Static helper method for properly escaping SQL strings.
    *
    * @param string the string to escape
    * @return the escaped string
    */
   static String escape(@NonNull String string) {
      return string.replaceAll("'", "''").replaceAll("\"", "\\\"");
   }

   private String addArg(SQLElement arg, boolean isRequiresParenthesis) {
      StringBuilder builder = new StringBuilder(" ");
      if(isRequiresParenthesis) {
         builder.append("( ");
      }
      builder.append(toSQL(arg));
      if(isRequiresParenthesis) {
         builder.append(" )");
      }
      return builder.toString();
   }

   /**
    * Generates the SQL needed to define a column for creating or altering a table.
    *
    * @param column the column definition
    * @return the SQL
    */
   String columnDefinition(@NonNull Column column);

   /**
    * Generates the SQL needed to create an {@link SQLObject} (e.g. Table, Trigger, Index, etc.).
    *
    * @param create the create object
    * @return the SQL
    */
   String create(@NonNull Create create);

   /**
    * Generates the SQL to delete from a table.
    *
    * @param delete the delete object
    * @return the SQL
    */
   String delete(@NonNull Delete delete);

   /**
    * Generates the SQL to drop an object
    *
    * @param drop the drop object
    * @return the SQL
    */
   String drop(@NonNull Drop drop);

   /**
    * Returns a function template for a function with the given name and number of arguments.
    *
    * @param name     the name of the function
    * @param argCount the number of arguments the function takes
    * @return the function template with "${argN}" representing an argument where N is 1 to argCount.
    */
   default String getFunctionTemplate(@NonNull String name, int argCount) {
      return name + "( " + IntStream.range(0, argCount)
                                    .mapToObj(i -> "${arg" + (i + 1) + "}")
                                    .collect(Collectors.joining(", ")) + " )";
   }

   /**
    * Generates the SQL to insert a row into a table
    *
    * @param insert the insert object
    * @return the SQL
    */
   String insert(@NonNull Insert insert);

   /**
    * Joins one or more {@link SQLElement} as SQL with the given delimiter
    *
    * @param delimiter the delimiter to use to separate the SQLElements
    * @param items     the items to join
    * @return the SQL
    */
   default String join(@NonNull String delimiter, @NonNull Collection<? extends SQLElement> items) {
      return items.stream().map(this::toSQL).collect(Collectors.joining(delimiter));
   }

   /**
    * Generates the NULL value representation for this dialect
    *
    * @return the SQL representing NULL
    */
   default String nullValue() {
      return "NULL";
   }

   /**
    * Generates the SQL for defining what happens on conflict
    *
    * @param conflictClause the conflict clause
    * @return the SQL
    */
   default String onConflict(@NonNull ConflictClause conflictClause) {
      return "ON CONFLICT " + conflictClause.name();
   }

   /**
    * Generates the SQL for select statement
    *
    * @param select the select statement
    * @return the SQL
    */
   String select(@NonNull Select select);

   /**
    * Helper method to convert any {@link SQLElement} into SQL
    *
    * @param element the element to convert
    * @return the SQL
    */
   default String toSQL(@NonNull SQLElement element) {
      if(element instanceof SQLObject) {
         return ((SQLObject) element).getName();
      } else if(element instanceof SQLFormattable) {
         return ((SQLFormattable) element).toSQL(this);
      }
      return element.toString();
   }

   default String translateOperator(String operator) {
      return operator;
   }

   /**
    * Generates the SQL For an update statement
    *
    * @param update the update statement
    * @return the SQL
    */
   String update(@NonNull Update update);

}//END OF SQLDialect
