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

package com.gengoai.sql.constraint.table;

import com.gengoai.sql.SQLElement;
import com.gengoai.sql.constraint.Constraint;
import com.gengoai.sql.constraint.TableConstraint;
import lombok.*;

@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Check extends Constraint implements TableConstraint {
   private static final long serialVersionUID = 1L;
   SQLElement expression;

   public Check(String name, @NonNull SQLElement expression) {
      super(name);
      this.expression = expression;
   }

   @Override
   public String toString() {
      return "Check{" +
            "name=" + getName() +
            ", expression=" + expression +
            "}";
   }

}//END OF Check
