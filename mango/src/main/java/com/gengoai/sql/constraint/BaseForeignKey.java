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

import com.gengoai.Validation;
import lombok.*;

import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true)
public abstract class BaseForeignKey extends Constraint {
   private static final long serialVersionUID = 1L;
   @NonNull
   @Getter
   protected final String foreignTable;
   @Singular
   @NonNull
   protected final List<String> foreignTableColumns;
   @Getter
   protected final ForeignKeyAction onUpdate;
   @Getter
   protected final ForeignKeyAction onDelete;
   @Getter
   protected final Deferrable deferred;

   public BaseForeignKey(String name,
                         @NonNull String foreignTable,
                         @NonNull List<String> foreignTableColumns,
                         ForeignKeyAction onUpdate,
                         ForeignKeyAction onDelete,
                         @NonNull Deferrable deferred) {
      super(name);
      this.foreignTable = foreignTable;
      Validation.checkArgument(foreignTableColumns.size() > 0, "Must specify at least 1 foreign table column.");
      this.foreignTableColumns = foreignTableColumns;
      this.onUpdate = onUpdate;
      this.onDelete = onDelete;
      this.deferred = deferred;
   }

   public List<String> getForeignTableColumns() {
      return Collections.unmodifiableList(foreignTableColumns);
   }
}//END OF ForeignKey
