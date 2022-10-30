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

import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;


public abstract class Constraint implements Serializable {
   private static final long serialVersionUID = 1L;
   @Getter
   protected final String name;


   protected Constraint() {
      this.name = Strings.EMPTY;
   }

   protected Constraint(String name) {
      this.name = name;
   }


   public static ConstraintBuilder constraint(@NonNull String name) {
      return new ConstraintBuilderImpl(name);
   }


   private static class ConstraintBuilderImpl implements ConstraintBuilder {
      public final String name;

      public ConstraintBuilderImpl(String name) {
         this.name = name;
      }


      @Override
      public String getName() {
         return name;
      }


   }
}//END OF Constraint
