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

import com.gengoai.Copyable;
import com.gengoai.sql.operator.SQLOperator;
import com.gengoai.sql.operator.SQLQueryOperator;
import lombok.NonNull;

/**
 * SQL Statement that performs a query over a database.
 */
public interface SQLQueryStatement extends SQLStatement, Copyable<SQLQueryStatement> {

   @Override
   default SQLQueryStatement copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Combines this query with the given second query using an EXCEPT operator
    *
    * @param query2 the second query.
    * @return the combined SQLQueryStatement
    */
   default SQLQueryStatement except(@NonNull SQLQueryStatement query2) {
      return new SQLQueryOperator(SQLOperator.EXCEPT, this, query2);
   }

   /**
    * Combines this query with the given second query using an INTERSECT operator
    *
    * @param query2 the second query.
    * @return the combined SQLQueryStatement
    */
   default SQLQueryStatement intersect(@NonNull SQLQueryStatement query2) {
      return new SQLQueryOperator(SQLOperator.INTERSECT, this, query2);
   }

   /**
    * Combines this query with the given second query using an UNION operator
    *
    * @param query2 the second query.
    * @return the combined SQLQueryStatement
    */
   default SQLQueryStatement union(@NonNull SQLQueryStatement query2) {
      return new SQLQueryOperator(SQLOperator.UNION, this, query2);
   }

   /**
    * Combines this query with the given second query using an UNION ALL operator
    *
    * @param query2 the second query.
    * @return the combined SQLQueryStatement
    */
   default SQLQueryStatement unionAll(@NonNull SQLQueryStatement query2) {
      return new SQLQueryOperator(SQLOperator.UNION_ALL, this, query2);
   }

}//END OF SQLQueryStatement
