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

package com.gengoai.sql.constraint.column;

import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.constraint.BaseForeignKey;
import com.gengoai.sql.constraint.ColumnConstraint;
import com.gengoai.sql.constraint.Deferrable;
import com.gengoai.sql.constraint.ForeignKeyAction;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Value
public class ForeignKey extends BaseForeignKey implements ColumnConstraint {
   private static final long serialVersionUID = 1L;

   public ForeignKey(String name,
                     @NonNull String foreignTable,
                     @NonNull List<String> foreignTableColumns,
                     ForeignKeyAction onUpdate,
                     ForeignKeyAction onDelete,
                     Deferrable deferred) {
      super(name, foreignTable, foreignTableColumns, onUpdate, onDelete, deferred);
   }

   @Override
   public boolean providesValue() {
      return false;
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      StringBuilder builder = new StringBuilder(super.toSQL(dialect))
            .append("REFERENCES ")
            .append(foreignTable)
            .append(" (")
            .append(foreignTableColumns.stream().map(s -> String.format("\"%s\"", s)).collect(Collectors.joining(", ")))
            .append(" )");
      if(onUpdate != null) {
         builder.append(" ON UPDATE ").append(onUpdate.toSQL(dialect));
      }
      if(onDelete != null) {
         builder.append(" ON DELETE ").append(onDelete.toSQL(dialect));
      }
      if(deferred != Deferrable.NOT_DEFERRABLE) {
         builder.append(" ").append(deferred.toSQL(dialect));
      }
      return builder.toString();
   }

}//END OF ForeignKey
