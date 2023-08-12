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

package com.gengoai.lucene.field.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.Copyable;
import com.gengoai.Validation;
import com.gengoai.json.Json;
import com.gengoai.lucene.IndexDocument;
import com.gengoai.lucene.LuceneIndex;
import com.gengoai.lucene.field.Fields;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;
import java.io.Serializable;

@Value
public class Vector implements Copyable<Vector>, Serializable {
    private static final long serialVersionUID = 1234567L;
    float[] data;

    @JsonCreator
    public Vector(@JsonProperty("data") float[] data) {
        this.data = data;
    }


    public static Vector getVector(@NonNull LuceneIndex index,
                                   @NonNull String vectorField,
                                   @NonNull String termField,
                                   @NonNull String term) throws IOException {
        IndexDocument id = index.get(termField, term);
        if (id.get(vectorField + Fields.STORED_FIELD_SUFFIX).isNull()) {
            return null;
        }
        return Json.parse(id.get(vectorField + Fields.STORED_FIELD_SUFFIX).asString(), Vector.class);
    }

    public Vector add(@NonNull Vector other) {
        Validation.checkArgument(other.data.length == data.length, "Mismatch in data length");
        for (int i = 0; i < data.length; i++) {
            data[i] += other.data[i];
        }
        return this;
    }

    public Vector sub(@NonNull Vector other) {
        Validation.checkArgument(other.data.length == data.length, "Mismatch in data length");
        for (int i = 0; i < data.length; i++) {
            data[i] -= other.data[i];
        }
        return this;
    }

    @Override
    public Vector copy() {
        return Copyable.deepCopy(this);
    }
}//END OF Vector
