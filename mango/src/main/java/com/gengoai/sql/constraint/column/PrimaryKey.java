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
import com.gengoai.sql.constraint.ColumnConstraint;
import com.gengoai.sql.constraint.ConflictClause;
import com.gengoai.sql.constraint.Constraint;
import lombok.*;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class PrimaryKey extends Constraint implements ColumnConstraint {
   private static final long serialVersionUID = 1L;
   @Getter
   ConflictClause conflictClause;

   public PrimaryKey(String name, ConflictClause conflictClause) {
      super(name);
      this.conflictClause = conflictClause;
   }

   @Override
   public boolean providesValue() {
      return false;
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      StringBuilder builder = new StringBuilder(super.toSQL(dialect)).append(" PRIMARY KEY ");
      if(conflictClause != null) {
         builder.append(conflictClause.toString()).append(" ");
      }
      return builder.toString();
   }

   @Override
   public String toString() {
      return "PrimaryKey{" +
            "name=" + getName() +
            ", conflictClause=" + conflictClause +
            '}';
   }
}//END OF PrimaryKey
