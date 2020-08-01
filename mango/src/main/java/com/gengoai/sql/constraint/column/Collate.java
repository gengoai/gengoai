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

import com.gengoai.Validation;
import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.constraint.ColumnConstraint;
import com.gengoai.sql.constraint.Constraint;
import lombok.*;

@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Collate extends Constraint implements ColumnConstraint {
   private static final long serialVersionUID = 1L;
   String collationName;

   public Collate(String collationName) {
      this.collationName = Validation.notNullOrBlank(collationName);
   }

   public Collate(String name, String collationName) {
      super(name);
      this.collationName = Validation.notNullOrBlank(collationName);
   }

   @Override
   public boolean providesValue() {
      return false;
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      return super.toSQL(dialect) + "COLLATE " + collationName;
   }

}//END OF Collate
