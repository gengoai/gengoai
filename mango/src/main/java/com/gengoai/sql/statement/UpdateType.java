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

import lombok.NonNull;
import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLFormattable;

/**
 * Defines the type of Update to perform
 */
public enum UpdateType implements SQLFormattable {
   /**
    * Standard update
    */
   UPDATE,
   /**
    * Update or replace on primary key conflict
    */
   UPDATE_OR_REPLACE,
   /**
    * Update or rollback on primary key conflict
    */
   UPDATE_OR_ROLLBACK,
   /**
    * Update or abort on primary key conflict
    */
   UPDATE_OR_ABORT,
   /**
    * Update or fail on primary key conflict
    */
   UPDATE_OR_FAIL,
   /**
    * Update or ignore on primary key conflict
    */
   UPDATE_OR_IGNORE;

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      return name().replace('_', ' ');
   }

}//END OF UpdateType
