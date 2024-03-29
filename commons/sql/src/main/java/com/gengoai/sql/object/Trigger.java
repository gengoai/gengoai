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

import com.gengoai.sql.SQLElement;
import com.gengoai.sql.statement.UpdateStatement;
import com.gengoai.string.Strings;
import lombok.*;

import java.io.Serializable;

/**
 * <p>Defines an SQL Trigger</p>
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Builder
@Data
public class Trigger extends SQLObject {
   private static final long serialVersionUID = 1L;
   private boolean isTemporary;
   private @NonNull TriggerTime when;
   private @NonNull SQLDMLOperation operation;
   private @NonNull SQLElement table;
   private @NonNull UpdateStatement updateStatement;
   private boolean isRowLevelTrigger;
   private SQLElement where;

   private Trigger(String name,
                   boolean isTemporary,
                   @NonNull TriggerTime when,
                   @NonNull SQLDMLOperation operation,
                   @NonNull SQLElement table,
                   @NonNull UpdateStatement updateStatement,
                   boolean isRowLevelTrigger,
                   SQLElement where) {
      super(name);
      this.isTemporary = isTemporary;
      this.when = when;
      this.operation = operation;
      this.table = table;
      this.updateStatement = updateStatement;
      this.isRowLevelTrigger = isRowLevelTrigger;
      this.where = where;
   }

   @Override
   public String getKeyword() {
      return "TRIGGER";
   }

   /**
    * Builder class for constructing triggers
    */
   public static class TriggerBuilder implements Serializable {
      private static final long serialVersionUID = 1L;
      private String name = null;

      /**
       * Build the Trigger instance
       *
       * @return the trigger
       */
      public Trigger build() {
         if (Strings.isNullOrBlank(name)) {
            name = "TRIGGER_" + Strings.randomHexString(10);
         }
         if (when == null) {
            when = TriggerTime.BEFORE;
         }
         if (operation == null) {
            operation = SQLDMLOperation.INSERT;
         }
         return new Trigger(name,
                            isTemporary,
                            this.when,
                            this.operation,
                            table,
                            updateStatement,
                            this.isRowLevelTrigger,
                            where);
      }

      /**
       * Set the name of the trigger (if not given one will be autogenerated)
       *
       * @param name the name
       * @return the trigger builder
       */
      public TriggerBuilder name(String name) {
         this.name = name;
         return this;
      }

   }

}//END OF Trigger
