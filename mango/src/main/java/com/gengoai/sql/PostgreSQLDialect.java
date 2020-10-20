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

import com.gengoai.LogUtils;
import com.gengoai.sql.object.Column;
import com.gengoai.sql.statement.InsertType;
import com.gengoai.sql.statement.UpdateType;
import com.gengoai.string.Strings;
import lombok.NonNull;
import lombok.extern.java.Log;

import static com.gengoai.sql.SQLConstants.*;

@Log
public class PostgreSQLDialect extends SQLDialect {
   private static final long serialVersionUID = 1L;

   @Override
   protected void defineColumn(Column column, StringBuilder builder) {
      builder.append(column.getName())
             .append(" ");
      if (column.isAutoIncrement()) {
         switch (column.getType().toUpperCase()) {
            case "LONG":
            case "BIGINT":
            case "BIGINTEGER":
               builder.append("BIGSERIAL");
               break;
            case "SHORT":
            case "SMALLINT":
               builder.append("SMALLSERIAL");
               break;
            default:
               builder.append("SERIAL");
         }
      } else {
         builder.append(column.getType());
      }
      if (column.isPrimaryKey()) {
         builder.append(" PRIMARY KEY");
      }
      if (column.getDefaultValue() != null) {
         builder.append(" DEFAULT ")
                .append(render(column.getDefaultValue()));
      }
      if (column.getStoredValue() != null) {
         builder.append(" GENERATED ALWAYS AS ( ")
                .append(render(column.getStoredValue()))
                .append(" ) STORED");
      }
      if (column.getVirtualValue() != null) {
         LogUtils.logWarning(log, "PostgreSQL does not support 'VIRTUAL' columns using 'STORED' instead");
         builder.append(" GENERATED ALWAYS AS ( ")
                .append(render(column.getVirtualValue()))
                .append(" ) STORED");
      }
      if (Strings.isNotNullOrBlank(column.getCollate())) {
         builder.append(" COLLATE ")
                .append(Strings.prependIfNotPresent(Strings.appendIfNotPresent(column.getCollate(), "\""), "\""));
      }
   }

   @Override
   protected String function(@NonNull SQLFunction function) {
      switch (function.getName()) {
         case JSON_EXTRACT:
            return String.format("(%s->>%s)",
                                 render(function.getArguments().get(0)),
                                 render(function.getArguments().get(1)));
         case REGEXP:
            return String.format("%s ~ %s",
                                 render(function.getArguments().get(0)),
                                 render(function.getArguments().get(1)));
         default:
            return super.function(function);
      }
   }

   @Override
   protected String insertType(InsertType insertType) {
      if (insertType == InsertType.INSERT_OR_FAIL) {
         return "INSERT";
      }
      LogUtils.logWarning(log, "PostgreSQL does not support ''{0}'' using ''INSERT'' instead", insertType);
      return "INSERT";
   }

   @Override
   protected String translate(String keyword) {
      switch (keyword) {
         case NOT_EQUALS:
            return "!=";
         case REGEXP:
            return "~";
         default:
            return super.translate(keyword);
      }

   }

   @Override
   protected String updateType(UpdateType updateType) {
      if (updateType == UpdateType.UPDATE) {
         return "UPDATE";
      }
      LogUtils.logWarning(log, "PostgreSQL does not support ''{0}'' using ''UPDATE'' instead", updateType);
      return "UPDATE";
   }
}//END OF PostgreSQLDialect
