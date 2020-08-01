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

import lombok.*;
import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLObject;

/**
 * <p>SQL Drop statement for SQL Objects (Table, Index, Trigger, etc)</p>
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class Drop implements SQLUpdateStatement {
   private static final long serialVersionUID = 1L;
   @Getter
   private final SQLObject object;
   @Getter
   private boolean ifExists;

   /**
    * Instantiates a new Drop Statement.
    *
    * @param object the object to be dropped
    */
   public Drop(@NonNull SQLObject object) {
      this.object = object;
   }

   /**
    * Fail if the item being dropped does not exist
    *
    * @return this Drop Statement
    */
   public Drop failIfNotExists() {
      this.ifExists = false;
      return this;
   }

   /**
    * Set whether or not to fail if trying to drop an object that does not exist
    *
    * @param ifExists True - only drop if the object exists, False - fail if the object does not exist
    * @return this Drop Statement
    */
   public Drop ifExists(boolean ifExists) {
      this.ifExists = ifExists;
      return this;
   }

   /**
    * Only perform the drop the item being dropped exists.
    *
    * @return this Drop Statement
    */
   public Drop ifExists() {
      this.ifExists = true;
      return this;
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      return dialect.drop(this);
   }

}//END OF Drop
