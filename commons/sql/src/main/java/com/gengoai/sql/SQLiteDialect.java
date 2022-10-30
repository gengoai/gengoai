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

import com.gengoai.sql.constraint.Constraint;
import com.gengoai.sql.object.Column;
import com.gengoai.string.Strings;

public class SQLiteDialect extends SQLDialect {
   private static final long serialVersionUID = 1L;

   @Override
   protected void defineColumn(Column column, StringBuilder builder) {
      builder.append(column.getName())
             .append(" ")
             .append(column.getType());
      if (column.isPrimaryKey() || column.isAutoIncrement()) {
         builder.append(" PRIMARY KEY");
      }
      if (column.isAutoIncrement()) {
         builder.append(" AUTOINCREMENT");
      }
      if (column.getDefaultValue() != null) {
         builder.append(" DEFAULT ")
                .append(render(column.getDefaultValue()));
      }
      if (column.getStoredValue() != null) {
         builder.append(" AS (")
                .append(render(column.getStoredValue()))
                .append(") STORED");
      }
      if (column.getVirtualValue() != null) {
         builder.append(" AS (")
                .append(render(column.getVirtualValue()))
                .append(") VIRTUAL");
      }

      for (Constraint constraint : column.getConstraints()) {
         defineConstraint(constraint, false, builder);
      }

      if (Strings.isNotNullOrBlank(column.getCollate())) {
         builder.append(" COLLATE ")
                .append(Strings.prependIfNotPresent(Strings.appendIfNotPresent(column.getCollate(), "\""), "\""));
      }
   }


}//END OF SQLiteContext
