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

import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.constraint.ConflictClause;
import com.gengoai.sql.constraint.Constraint;
import com.gengoai.sql.constraint.TableConstraint;
import lombok.*;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Unique extends Constraint implements TableConstraint {
   private static final long serialVersionUID = 1L;
   List<SQLElement> columns;
   ConflictClause conflictClause;

   public Unique(String name, @NonNull List<SQLElement> columns, ConflictClause conflictClause) {
      super(name);
      this.columns = columns;
      this.conflictClause = conflictClause;
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      StringBuilder builder = new StringBuilder(super.toSQL(dialect))
            .append(" UNIQUE (")
            .append(dialect.join(", ", columns))
            .append(") ");
      if(conflictClause != null) {
         builder.append(conflictClause.toSQL(dialect));
      }
      return builder.toString();
   }

   @Override
   public String toString() {
      return "Unique{" +
            "name=" + getName() +
            ", columns=" + columns +
            ", conflictClause=" + conflictClause +
            '}';
   }
}//END OF PrimaryKey
