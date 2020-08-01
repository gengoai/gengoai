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

package com.gengoai.sql.operator;

import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLElement;
import lombok.*;

/**
 * Specialized operator for SQL Between operators
 */
@Value
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@EqualsAndHashCode(callSuper = true)
public class SQLBetween extends SQLOperator {
   private static final long serialVersionUID = 1L;
   @NonNull SQLElement column;
   @NonNull SQLElement lower;
   @NonNull SQLElement higher;

   public SQLBetween(@NonNull SQLElement column,
                     @NonNull SQLElement lower,
                     @NonNull SQLElement higher) {
      super(SQLOperator.BETWEEN);
      this.column = column;
      this.lower = lower;
      this.higher = higher;
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      return dialect.toSQL(column) +
            " " +
            dialect.translateOperator(getOperator()) +
            " " +
            dialect.toSQL(lower) +
            " AND " +
            dialect.toSQL(higher);
   }

   @Override
   public String toString() {
      return "SQLBetween{" +
            "column=" + column +
            ", lower=" + lower +
            ", higher=" + higher +
            '}';
   }
}//END OF SQLBetween
