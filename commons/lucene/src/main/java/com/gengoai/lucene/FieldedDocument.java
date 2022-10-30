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

import com.gengoai.conversion.Val;
import com.gengoai.io.resource.ByteArrayResource;
import com.gengoai.lucene.field.Fields;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>A fielded document will only be returned when a serialized form is not avaiable</p>
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode
@Log
public class FieldedDocument implements IndexDocument {
   private final Document document;


   public FieldedDocument(@NonNull Document document) {
      this.document = document;
   }

   @Override
   public IndexDocument add(String name, @NonNull Object value) {
      Fields.toIndexableField(value, name, false)
            .forEach(this::add);
      return this;
   }


   @Override
   public IndexDocument add(@NonNull IndexableField field) {
      document.add(field);
      return this;
   }

   @Override
   public Iterable<? extends IndexableField> asIndexableFields(@NonNull IndexConfig config) {
      return document;
   }

   @Override
   public void clear() {
      document.clear();
   }

   @Override
   public Val get(String name) {
      final var storedField = Fields.getStoredFieldName(name);
      if (hasField(storedField)) {
         return Val.of(document.get(storedField));
      }
      return Val.of(document.get(name));
   }

   @Override
   public Val getAll(String name) {
      final var storedField = Fields.getStoredFieldName(name);
      if (hasField(storedField)) {
         return Val.of(document.getValues(storedField));
      }
      return Val.of(document.getValues(name));
   }

   @Override
   @SneakyThrows
   public <T> List<T> getAllBlobField(@NonNull String name) {
      if (hasField(name)) {
         List<T> objects = new ArrayList<>();
         for (BytesRef blob : document.getBinaryValues(name)) {
            ByteArrayResource bar = new ByteArrayResource(blob.bytes);
            objects.add(bar.readObject());
         }
         return objects;
      }
      return Collections.emptyList();
   }

   @Override
   @SneakyThrows
   public <T> T getBlobField(@NonNull String name) {
      if (hasField(name)) {
         ByteArrayResource bar = new ByteArrayResource(document.getBinaryValue(name).bytes);
         return bar.readObject();
      }
      return null;
   }

   @Override
   public Set<String> getFields() {
      return document.getFields().stream().map(IndexableField::name).collect(Collectors.toSet());
   }

   @Override
   public boolean hasField(@NonNull String name) {
      return document.getField(name) != null;
   }

   @Override
   public void remove(String name) {
      document.removeField(name);
      document.removeField(Fields.getStoredFieldName(name));
   }

   @Override
   public void removeAll(String name) {
      document.removeFields(name);
      document.removeFields(Fields.getStoredFieldName(name));
   }

   @Override
   public void replaceField(@NonNull String name, @NonNull Object... values) {
      boolean isStored = hasField(Fields.getStoredFieldName(name));
      removeAll(name);
      for (Object value : values) {
         Fields.toIndexableField(value, name, isStored)
               .forEach(this::add);
      }
   }

   @Override
   public <T> T toObject(@NonNull Type type) {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString() {
      return document.toString();
   }

   @Override
   public boolean updateField(@NonNull String name, @NonNull Function<Val, ?> updateFunction) {
      IndexableField firstField = document.getField(name);
      boolean isStored = (firstField != null && firstField.fieldType().stored()) ||
                         hasField(Fields.getStoredFieldName(name));
      List<Object> values = getAll(name).asList(Object.class);

      if (values.isEmpty()) {
         return false;
      }

      removeAll(name);
      boolean isFullText = firstField != null &&
                           firstField.fieldType().tokenized() &&
                           firstField.fieldType().indexOptions() == IndexOptions.DOCS_AND_FREQS_AND_POSITIONS;
      values.forEach(v -> {
         Object o = updateFunction.apply(Val.of(v));
         if (isFullText) {
            add(new TextField(name, o.toString(), isStored ? Field.Store.YES : Field.Store.NO));
         } else {
            Fields.toIndexableField(updateFunction.apply(Val.of(v)), name, isStored)
                  .forEach(this::add);
         }
      });

      return true;
   }
}//END OF FieldedDocument
