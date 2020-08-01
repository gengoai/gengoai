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

package com.gengoai.sql.constraint.table;

import com.gengoai.Validation;
import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.constraint.BaseForeignKey;
import com.gengoai.sql.constraint.Deferrable;
import com.gengoai.sql.constraint.ForeignKeyAction;
import com.gengoai.sql.constraint.TableConstraint;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class ForeignKey extends BaseForeignKey implements TableConstraint {
   private static final long serialVersionUID = 1L;
   List<String> columns;

   public ForeignKey(String name,
                     @NonNull List<String> columns,
                     @NonNull String foreignTable,
                     @NonNull List<String> foreignTableColumns,
                     ForeignKeyAction onUpdate,
                     ForeignKeyAction onDelete,
                     Deferrable deferred) {
      super(name, foreignTable, foreignTableColumns, onUpdate, onDelete, deferred);
      Validation.checkArgument(columns.size() > 0, "Must specify at least 1 native table column.");
      this.columns = columns;
   }

   public List<String> getColumns() {
      return Collections.unmodifiableList(columns);
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      StringBuilder builder = new StringBuilder(super.toSQL(dialect))
            .append("FOREIGN KEY(")
            .append(columns.stream().map(s -> String.format("\"%s\"", s)).collect(Collectors.joining(", ")))
            .append(")  REFERENCES ")
            .append(foreignTable)
            .append("( ")
            .append(foreignTableColumns.stream().map(s -> String.format("\"%s\"", s)).collect(Collectors.joining(", ")))
            .append(" )");
      if(onUpdate != null) {
         builder.append("\n\tON UPDATE ").append(onUpdate.toString());
      }
      if(onDelete != null) {
         builder.append("\n\tON DELETE ").append(onDelete.toString());
      }
      if(deferred != Deferrable.NOT_DEFERRABLE) {
         builder.append("\n\t").append(deferred.toSQL(dialect));
      }
      return builder.toString();
   }
}//END OF ForeignKey
