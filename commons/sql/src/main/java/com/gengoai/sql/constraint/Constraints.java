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

import com.gengoai.sql.object.Column;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collections;

public final class Constraints {

    private Constraints() {
        throw new IllegalAccessError();
    }

    public static final Constraint PRIMARY_KEY_COLUMN = new PrimaryKeyConstraint("", Collections.emptyList());
    public static final Constraint UNIQUE_KEY_COLUMN = new UniqueConstraint("", Collections.emptyList());
    public static final Constraint NOT_NULL_COLUMN = new NotNullConstraint("", null);


    public static PrimaryKeyConstraint primaryKey(@NonNull Column... columns) {
        return new PrimaryKeyConstraint("", Arrays.asList(columns));
    }

    public static UniqueConstraint unique(@NonNull Column... columns) {
        return new UniqueConstraint("", Arrays.asList(columns));
    }

    public static NotNullConstraint notNull(@NonNull Column column) {
        return new NotNullConstraint("", column);
    }

    public static Constraint primaryKey(String name, @NonNull Column... columns) {
        return new PrimaryKeyConstraint(name, Arrays.asList(columns));
    }

    public static Constraint unique(String name, @NonNull Column... columns) {
        return new UniqueConstraint(name, Arrays.asList(columns));
    }

    public static Constraint notNull(String name, @NonNull Column column) {
        return new NotNullConstraint(name, column);
    }

}//END OF Constraints
