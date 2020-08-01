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

package com.gengoai.sql.constraint;

import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLFormattable;
import com.gengoai.sql.SQLObject;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 * The type Constraint.
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public abstract class Constraint extends SQLObject implements SQLFormattable {
   private static final long serialVersionUID = 1L;

   public Constraint() {
   }

   /**
    * Instantiates a new Constraint.
    *
    * @param name the name
    */
   public Constraint(String name) {
      super(name);
   }

   @Override
   public String getKeyword() {
      return "CONSTRAINT";
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      StringBuilder builder = new StringBuilder();
      if(getName() != null) {
         builder.append("CONSTRAINT \"")
                .append(getName())
                .append("\" ");
      }
      return builder.toString();
   }

}//END OF Constraint
