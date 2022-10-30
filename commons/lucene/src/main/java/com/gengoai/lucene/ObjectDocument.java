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

package com.gengoai.lucene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.LogUtils;
import com.gengoai.Validation;
import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.lucene.field.Fields;
import com.gengoai.string.Strings;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

@NoArgsConstructor
@EqualsAndHashCode
@Log
class ObjectDocument implements IndexDocument {
   @JsonValue
   private final ArrayListMultimap<String, Object> fields = new ArrayListMultimap<>();

   @JsonCreator
   protected ObjectDocument(Multimap<String, JsonEntry> values) {
      if (values != null) {
         fields.putAll(values);
      }
   }

   protected ObjectDocument(Map<String, JsonEntry> values) {
      if (values != null) {
         values.forEach(fields::put);
      }
   }

   @Override
   public IndexDocument add(@NonNull String name, @NonNull Object value) {
      fields.put(name, value);
      return this;
   }

   @Override
   public IndexDocument add(@NonNull IndexableField field) {
      return add(field.name(), field);
   }


   @Override
   public Iterable<? extends IndexableField> asIndexableFields(@NonNull IndexConfig config) {
      Document luceneDocument = new Document();
      for (String fieldName : fields.keySet()) {
         Optional<com.gengoai.lucene.field.Field> fieldInfo = config.getField(fieldName);
         for (Object value : fields.get(fieldName)) {
            if (value instanceof IndexableField) {
               Validation.checkArgument(fieldInfo.isEmpty(),
                                        () -> "Attempting to use an IndexableField for '" +
                                              fieldName +
                                              "' when it is defined in the schema");
               luceneDocument.add(Cast.as(value));
            } else {
               fieldInfo.ifPresent(f -> {
                  for (IndexableField pField : f.process(value)) {
                     LogUtils.logFiner(log, "Created IndexableField {0} for {1}", pField, f);
                     luceneDocument.add(pField);
                  }
               });
            }
         }
      }

      if (Strings.isNotNullOrBlank(config.getSerializedDocumentField())) {
         luceneDocument.add(new StoredField(config.getSerializedDocumentField(),
                                            Json.dumps(fields)));
      }
      return luceneDocument;
   }

   @Override
   @SneakyThrows
   public <T> T toObject(@NonNull Type type) {
      Map<String, Object> sv = new HashMap<>();
      for (String f : fields.keySet()) {
         List<?> values = fields.get(f);
         if (values.size() > 0 ) {
            sv.put(f, values.get(0));
         }
      }
      return Json.parse(Json.dumps(sv), type);
   }

   @Override
   public void clear() {
      fields.clear();
   }

   @Override
   public Val get(String name) {
      final var storedField = Fields.getStoredFieldName(name);
      if (fields.containsKey(storedField)) {
         return Val.of(fields.get(storedField).get(0));
      }
      if (fields.containsKey(name)) {
         return Val.of(fields.get(name).get(0));
      }
      return Val.NULL;
   }

   @Override
   public Val getAll(String name) {
      final var storedField = Fields.getStoredFieldName(name);
      if (fields.containsKey(storedField)) {
         Val.of(Collections.unmodifiableList(fields.get(storedField)));
      }
      return Val.of(Collections.unmodifiableList(fields.get(name)));
   }

   @Override
   @SneakyThrows
   public <T> List<T> getAllBlobField(@NonNull String name) {
      return Cast.cast(Collections.unmodifiableList(fields.get(name)));
   }

   @Override
   @SneakyThrows
   public <T> T getBlobField(@NonNull String name) {
      return get(name).cast();
   }

   @Override
   public Set<String> getFields() {
      return Collections.unmodifiableSet(fields.keySet());
   }

   @Override
   public boolean hasField(@NonNull String name) {
      return fields.containsKey(name);
   }

   @Override
   public void remove(String name) {
      if (fields.containsKey(name)) {
         fields.get(name).remove(0);
      }
      final var storedField = Fields.getStoredFieldName(name);
      if (fields.containsKey(storedField)) {
         fields.get(storedField).remove(0);
      }
   }

   @Override
   public void removeAll(String name) {
      fields.removeAll(name);
      fields.removeAll(Fields.getStoredFieldName(name));
   }

   @Override
   public void replaceField(@NonNull String name, @NonNull Object... values) {
      removeAll(name);
      for (Object value : values) {
         add(name, value);
      }
   }

   @Override
   public String toString() {
      return fields.toString();
   }

   @Override
   public boolean updateField(@NonNull String name, @NonNull Function<Val, ?> updateFunction) {
      List<Object> values = getAll(name).asList(Object.class);
      if (values.isEmpty()) {
         return false;
      }
      removeAll(name);
      values.forEach(v -> add(name, updateFunction.apply(Val.of(v))));
      return true;
   }
}//END OF ObjectDocument
