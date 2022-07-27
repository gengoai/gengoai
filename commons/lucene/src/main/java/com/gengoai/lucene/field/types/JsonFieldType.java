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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.json.JsonEntry;
import com.gengoai.lucene.field.Fields;
import lombok.EqualsAndHashCode;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.kohsuke.MetaInfServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@MetaInfServices
@EqualsAndHashCode(callSuper = true)
public class JsonFieldType extends FieldType {
   @JsonProperty
   private final boolean indexElements;

   public JsonFieldType() {
      super(JsonEntry.class);
      this.indexElements = false;
   }

   public JsonFieldType(boolean indexElements) {
      super(JsonEntry.class);
      this.indexElements = indexElements;
   }

   @Override
   protected Collection<IndexableField> processImpl(Object value, String outField, boolean store) {
      JsonEntry entry = JsonEntry.from(value);
      List<IndexableField> indexableFields = new ArrayList<>();
      if (store) {
         indexableFields.add(new StoredField(Fields.getStoredFieldName(outField), entry.toString()));
      }
      if (indexElements) {
         indexableFields.addAll(Fields.toIndexableField(entry, outField, store));
      }
      return indexableFields;
   }
}//END OF JsonFieldType
