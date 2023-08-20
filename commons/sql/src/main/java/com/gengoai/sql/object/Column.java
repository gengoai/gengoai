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
import com.gengoai.sql.NamedSQLElement;
import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.constraint.ConflictClause;
import com.gengoai.sql.constraint.Constraint;
import com.gengoai.sql.constraint.PrimaryKeyConstraint;
import com.gengoai.sql.operator.SQLOperable;
import com.gengoai.string.Strings;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a column in a database table.
 */
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@ToString()
public class Column implements Serializable, NamedSQLElement, SQLOperable {
    private static final long serialVersionUID = 1L;
    private final List<Constraint> constraints = new ArrayList<>();
    @Setter
    private String type;
    @Setter
    private String name;
    private boolean primaryKey = false;
    private boolean autoIncrement = false;
    private SQLElement defaultValue = null;
    private SQLElement storedValue = null;
    private SQLElement virtualValue = null;
    private String collate = null;

    /**
     * Instantiates a new Column.
     *
     * @param name the name of the column
     * @param type the data type of the column
     */
    public Column(String name, String type) {
        this.name = Validation.notNullOrBlank(name);
        this.type = Validation.notNullOrBlank(type);
    }

    /**
     * Instantiates a new Column.
     *
     * @param name        the name of the column
     * @param type        the data type of the column
     * @param constraints the column constraints
     */
    public Column(String name, String type, @NonNull Collection<Constraint> constraints) {
        this.name = Validation.notNullOrBlank(name);
        this.type = Validation.notNullOrBlank(type);
        this.constraints.addAll(constraints);
    }

    /**
     * Sets a value that is auto generated for the column on insertion time and is stored in the database.
     *
     * @param element the element / expression to generate the column's value
     * @return this column
     */
    public Column asStored(SQLElement element) {
        this.storedValue = element;
        this.virtualValue = null;
        return this;
    }

    /**
     * Sets a value that is auto generated for the column on query time and not stored in the database.
     *
     * @param element the element / expression to generate the column's value
     * @return this column
     */
    public Column asVirtual(SQLElement element) {
        this.virtualValue = element;
        this.storedValue = null;
        return this;
    }

    /**
     * Sets this column as having its value auto incremented on assertion.
     *
     * @return this column
     */
    public Column autoIncrement() {
        this.autoIncrement = true;
        return this;
    }

    /**
     * Sets whether or not the column has an auto incremented value
     *
     * @param isAutoIncrement True - auto increment, False - do not auto increment
     * @return this column
     */
    public Column autoIncrement(boolean isAutoIncrement) {
        this.autoIncrement = isAutoIncrement;
        return this;
    }

    /**
     * Adds a Check constraint on this column.
     *
     * @param element the check
     * @return this column
     */
    public Column check(@NonNull SQLElement element) {
        constraints.add(Constraint.constraint("").check(element));
        return this;
    }

    /**
     * Adds a Check constraint with the given name on this column.
     *
     * @param name    the name of the constraint
     * @param element the check
     * @return this column
     */
    public Column check(String name, @NonNull SQLElement element) {
        constraints.add(Constraint.constraint(name).check(element));
        return this;
    }

    /**
     * Clears all constraints on the column.
     */
    public void clearConstraints() {
        this.constraints.clear();
    }

    /**
     * Sets the collation used for this column
     *
     * @param collate the collation
     * @return this column
     */
    public Column collate(String collate) {
        this.collate = collate;
        return this;
    }

    /**
     * Sets the default value to be assigned to the column when no value is given on an insert.
     *
     * @param defaultValue the default value
     * @return this column
     */
    public Column defaultValue(SQLElement defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Gets an unmodifiable view of the constraints on the column.
     *
     * @return the constraints
     */
    public List<Constraint> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    /**
     * Checks if a constraint of the give type is on the column
     *
     * @param constraintClass the constraint class
     * @return True if the column has at least one constraint of the given type
     */
    public boolean hasConstraintOfType(@NonNull Class<? extends Constraint> constraintClass) {
        if (PrimaryKeyConstraint.class.isAssignableFrom(constraintClass)) {
            return primaryKey;
        }
        return constraints.stream().anyMatch(constraintClass::isInstance);
    }

    /**
     * Determines if the column is requires a value to be specified during insertion.
     *
     * @return true if the column requires a value to be specified during insertion, false not required.
     */
    @JsonIgnore
    public boolean isRequired() {
        return defaultValue == null && storedValue == null && virtualValue == null && !autoIncrement;
    }

    /**
     * Sets the column to not allow null values.
     *
     * @return this column
     */
    public Column notNull() {
        this.constraints.add(Constraint.constraint(Strings.EMPTY).notNull(this));
        return this;
    }

    /**
     * Sets the column to not allow null values optionally specifying what happens when insertion of a null value is
     * attempted.
     *
     * @param conflictClause the {@link ConflictClause} defining what happens when insertion of a null value is
     *                       attempted.
     * @return this column
     */
    public Column notNull(ConflictClause conflictClause) {
        this.constraints.add(Constraint.constraint(Strings.EMPTY).notNull(this).onConflict(conflictClause));
        return this;
    }

    /**
     * Sets this column as the primary key of the table.
     *
     * @return this column
     */
    public Column primaryKey() {
        this.primaryKey = true;
        return this;
    }


    public SQLOperable from(@NonNull Table table) {
        return SQL.C(table, getName());
    }

    public SQLOperable from(@NonNull String table) {
        return SQL.C(table, getName());
    }

    /**
     * Sets the column to have a uniqueness constraint
     *
     * @return this column
     */
    public Column unique() {
        return unique(Strings.EMPTY, null);
    }

    /**
     * Sets the column to have a uniqueness constraint
     *
     * @param name the name of the constraint
     * @return this column
     */
    public Column unique(String name) {
        return unique(name, null);
    }

    /**
     * Sets the column to have a uniqueness constraint optionally specifying what happens when insertion of a duplicate
     * value is attempted.
     *
     * @param conflictClause the {@link ConflictClause} defining what happens when insertion of a duplicate value is
     *                       attempted.
     * @return this column
     */
    public Column unique(ConflictClause conflictClause) {
        return unique(Strings.EMPTY, conflictClause);
    }

    /**
     * Sets the column to have a uniqueness constraint optionally specifying what happens when insertion of a duplicate
     * value is attempted.
     *
     * @param name           the name of the constraint
     * @param conflictClause the {@link ConflictClause} defining what happens when insertion of a duplicate value is
     *                       attempted.
     * @return this column
     */
    public Column unique(@NonNull String name, ConflictClause conflictClause) {
        this.constraints.add(Constraint.constraint(name).unique(this).onConflict(conflictClause));
        return this;
    }


}//END OF Column
