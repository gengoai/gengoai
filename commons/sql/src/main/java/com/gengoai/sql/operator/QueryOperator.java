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

import com.gengoai.Validation;
import com.gengoai.sql.statement.QueryStatement;
import lombok.*;

/**
 * A {@link SQLOperator} which works on {@link QueryStatement}s and result is also an {@link QueryStatement}
 */
@Value
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@EqualsAndHashCode(callSuper = true)
public class QueryOperator extends QueryStatement implements SQLOperable {
   private static final long serialVersionUID = 1L;
   @NonNull String name;
   @NonNull QueryStatement query1;
   @NonNull QueryStatement query2;

   /**
    * Instantiates a new SQLQueryOperator.
    *
    * @param operator the operator
    * @param query1   the first query
    * @param query2   the second query
    */
   public QueryOperator(String operator,
                        @NonNull QueryStatement query1,
                        @NonNull QueryStatement query2) {
      this.name = Validation.notNullOrBlank(operator);
      this.query1 = query1;
      this.query2 = query2;
   }

   @Override
   public String toString() {
      return "SQLQueryOperator{" +
            "operator='" + name + '\'' +
            ", query1=" + query1 +
            ", query2=" + query2 +
            '}';
   }
}//END OF QueryOperator
