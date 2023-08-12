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

import com.gengoai.conversion.Cast;
import com.gengoai.json.Json;
import com.gengoai.lucene.field.Fields;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Log
@MetaInfServices
public class VectorFieldType extends FieldType {

    public VectorFieldType() {
        super(Vector.class);
    }

    @Override
    protected Collection<IndexableField> processImpl(Object value, String outField, boolean store) {
        if (value instanceof Vector) {
            Vector vector = Cast.as(value);
            KnnFloatVectorField vectorField = new KnnFloatVectorField(outField, vector.getData(), VectorSimilarityFunction.COSINE);
            if (store) {
                return List.of(vectorField,
                               new StoredField(outField + Fields.STORED_FIELD_SUFFIX, Json.dumps(vector)));
            }
            return Collections.singletonList(vectorField);
        }
        throw new IllegalArgumentException("Expecting a float[], but received " +
                                                   (value == null ? "null" : value.getClass().getName()));
    }

}
