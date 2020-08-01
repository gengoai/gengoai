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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gengoai.Validation;
import com.gengoai.sql.constraint.ColumnConstraint;
import com.gengoai.sql.operator.SQLOperable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>A column represents an attribute or property of the data stored within a row of a database. The column class
 * defines the column's name, data type, and any constraints on the column.</p>
 */
@Data
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Column implements SQLOperable {
   private static final long serialVersionUID = 1L;
   private final List<ColumnConstraint> constraints;
   /**
    * The Data type.
    */
   @NonNull String dataType;
   /**
    * The Name.
    */
   @NonNull String name;

   /**
    * Instantiates a new Column.
    *
    * @param name        the name of the column
    * @param dataType    the data type of the column
    * @param constraints the constraints on the column if any
    */
   public Column(String name, String dataType, @NonNull List<ColumnConstraint> constraints) {
      this.name = Validation.notNullOrBlank(name);
      this.dataType = Validation.notNullOrBlank(dataType);
      this.constraints = new ArrayList<>(constraints);
   }

   /**
    * Instantiates a new Column.
    *
    * @param name        the name of the column
    * @param dataType    the data type of the column
    * @param constraints the constraints on the column if any
    */
   public Column(String name, String dataType, @NonNull ColumnConstraint... constraints) {
      this(name, dataType, Arrays.asList(constraints));
   }

   /**
    * Adds a constraint to the column.
    *
    * @param constraint the constraint to add
    */
   public void addConstraint(@NonNull ColumnConstraint constraint) {
      this.constraints.add(constraint);
   }

   /**
    * Clears all constraints on the column.
    */
   public void clearConstraints() {
      this.constraints.clear();
   }

   /**
    * Checks if a constraint of the give type is on the column
    *
    * @param constraintClass the constraint class
    * @return True if the column has at least one constraint of the given type
    */
   public boolean hasConstraintOfType(@NonNull Class<? extends ColumnConstraint> constraintClass) {
      return constraints.stream().anyMatch(constraintClass::isInstance);
   }

   /**
    * Determines if the column is requires a value to be specified during insertion.
    *
    * @return true if the column requires a value to be specified during insertion, false not required.
    */
   @JsonIgnore
   public boolean isRequired() {
      return constraints.stream().noneMatch(ColumnConstraint::providesValue);
   }

   @Override
   public String toString() {
      return "\"" + name + "\"";
   }

}//END OF Column
