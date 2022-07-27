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

import com.gengoai.sql.object.SQLObject;
import lombok.*;

/**
 * <p>SQL create statement for SQL objects (Table, Index, Trigger, etc).</p>
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class Create implements UpdateStatement {
   private static final long serialVersionUID = 1L;
   @Getter
   private final SQLObject object;
   @Getter
   private boolean ifNotExists = false;

   /**
    * Instantiates a new Create.
    *
    * @param object the object being created
    */
   public Create(@NonNull SQLObject object) {
      this.object = object;
   }

   /**
    * Fail if the object trying to be created already exists
    *
    * @return this Create object
    */
   public Create failIfExists() {
      ifNotExists = true;
      return this;
   }

   /**
    * Only create the object if it does not exist
    *
    * @return this Create object
    */
   public Create ifNotExists() {
      ifNotExists = true;
      return this;
   }

   /**
    * Set whether or not to fail or not when creating an object that already exists
    *
    * @param ifNotExists True - only create the object if it does not exist, false - fail if the object exists.
    * @return this Create object
    */
   public Create ifNotExists(boolean ifNotExists) {
      this.ifNotExists = ifNotExists;
      return this;
   }

}//END OF Create
