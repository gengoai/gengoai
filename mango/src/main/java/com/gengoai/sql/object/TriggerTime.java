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

package com.gengoai.sql.object;

import lombok.NonNull;
import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLFormattable;

/**
 * When a trigger is fired
 */
public enum TriggerTime implements SQLFormattable {
   /**
    * Before the update statement happens
    */
   BEFORE,
   /**
    * After the update statement happens
    */
   AFTER,
   /**
    * Instead of the update statement happening
    */
   INSTEAD_OF;

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      return name().replace('_', ' ');
   }
}//END OF TriggerTime
