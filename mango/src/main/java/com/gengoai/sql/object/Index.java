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

import com.gengoai.Validation;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.SQLObject;
import com.gengoai.string.Strings;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an index on one or more columns in a table.
 */
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class Index extends SQLObject {
   @Getter
   SQLElement table;
   @Getter
   List<SQLElement> columns = new ArrayList<>();
   @Getter
   boolean isUnique;

   /**
    * Instantiates a new Index.
    *
    * @param table   the table containing the columns to index
    * @param columns the columns to index
    */
   public Index(@NonNull Table table, @NonNull List<SQLElement> columns) {
      this(table, "NON_UNIQUE_" + Strings.randomHexString(5), false, columns);
   }

   /**
    * Instantiates a new Index.
    *
    * @param table    the table containing the columns to index
    * @param name     the name of the index
    * @param isUnique will the index have a unique constraint
    * @param columns  the columns to index
    */
   public Index(@NonNull SQLElement table, String name, boolean isUnique, @NonNull List<SQLElement> columns) {
      super(name);
      Validation.checkArgument(columns.size() > 0, "Must specify at least one column");
      this.table = table;
      this.isUnique = isUnique;
      this.columns.addAll(columns);
   }

   @Override
   public String getKeyword() {
      return "INDEX";
   }

   @Override
   public String toString() {
      return "Index{" +
            "name='" + name + '\'' +
            ", table=" + table +
            ", columns=" + columns +
            ", isUnique=" + isUnique +
            '}';
   }
}//END OF Index
