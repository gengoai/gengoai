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

package com.gengoai.lucene.field;

import com.gengoai.Primitives;
import com.gengoai.Validation;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.Val;
import com.gengoai.json.JsonEntry;
import com.gengoai.lucene.IndexDocument;
import com.gengoai.lucene.field.types.*;
import com.gengoai.string.StringFunctions;
import lombok.NonNull;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public final class Fields {
   public static final FieldType STRING = new StringFieldType();
   public static final FieldType FULL_TEXT = new FullTextFieldType();
   public static final FieldType DOUBLE = new DoubleFieldType();
   public static final FieldType INTEGER = new IntFieldType();
   public static final FieldType LONG = new LongFieldType();
   public static final FieldType FLOAT = new FloatFieldType();
   public static final FieldType BOOLEAN = new BooleanFieldType();
   public static final FieldType SORTED_STRING = new SortedFieldType();
   public static final FieldType UPPER_STRING = new TransformedStringFieldType(StringFunctions.UPPER_CASE);
   public static final FieldType LOWER_STRING = new TransformedStringFieldType(StringFunctions.LOWER_CASE);
   public static final FieldType LOCAL_DATE = new LocalDateFieldType();
   public static final FieldType OFFSET_DATE_TIME = new OffsetDateTimeFieldType();
   public static final FieldType EMBEDDING = new EmbeddingFieldType();
   public static final FieldType BLOB = new BlobFieldType(Object.class);

   public static final FieldType LAT_LONG = new LatLongFieldType();
   public static final String STORED_FIELD_SUFFIX = ".stored";

   private static final Map<Class<?>, FieldType> classToFieldType = new ConcurrentHashMap<>();

   static {
      ServiceLoader.load(FieldType.class)
                   .forEach(fieldType -> classToFieldType.put(fieldType.getValueClass(), fieldType));
   }

   public static FieldType createBlobOfType(@NonNull Class<?> blobClass) {
      return new BlobFieldType(blobClass);
   }

   public static com.gengoai.lucene.field.Field field(@NonNull String name, @NonNull FieldType fieldType, boolean store) {
      return new com.gengoai.lucene.field.Field(name, fieldType, store);
   }

   public static com.gengoai.lucene.field.Field field(@NonNull String name, @NonNull FieldType fieldType) {
      return new com.gengoai.lucene.field.Field(name, fieldType);
   }

   public static Optional<com.gengoai.lucene.field.Field> findField(@NonNull String fieldName,
                                                                    @NonNull Set<com.gengoai.lucene.field.Field> fields) {
      Optional<com.gengoai.lucene.field.Field> parentField = fields.stream()
                                                                   .filter(field -> fieldName
                                                                         .startsWith(field.getName()))
                                                                   .max(Comparator
                                                                              .comparingInt(f -> f.getName().length()));
      if (parentField.isPresent()) {
         com.gengoai.lucene.field.Field parentF = parentField.get();
         if (fieldName.length() > parentF.getName().length()) {
            return parentF.getChildField(fieldName.substring(parentF.getName().length()));
         }
      }
      return parentField;
   }

   public static String getStoredFieldName(String name) {
      return Validation.notNullOrBlank(name) + STORED_FIELD_SUFFIX;
   }

   public static Collection<IndexableField> toIndexableField(Object value, String name, boolean stored) {
      if (value == null) {
         return Collections.emptyList();
      }
      if (value instanceof Val) {
         return toIndexableField(Cast.<Val>as(value).get(), name, stored);
      }
      org.apache.lucene.document.Field.Store store = stored ? org.apache.lucene.document.Field.Store.YES : Field.Store.NO;

      Class<?> valueClass = value.getClass();
      if (value instanceof JsonEntry) {
         JsonEntry je = Cast.as(value);
         if (je.isString()) {
            valueClass = String.class;
         } else if (je.isObject()) {
            valueClass = Map.class;
            value = je.asMap();
         } else if (je.isBoolean()) {
            valueClass = Boolean.class;
         } else if (je.isNumber()) {
            valueClass = Double.class;
         } else if (je.isArray()) {
            valueClass = List.class;
            value = je.asArray();
         }
      }


      FieldType type = classToFieldType.get(valueClass);
      if (type != null) {
         return type.process(value, name, stored);
      }

      if (Iterable.class.isAssignableFrom(valueClass)) {
         List<IndexableField> values = new ArrayList<>();
         for (Object a : Cast.<Iterable<?>>as(value)) {
            values.addAll(toIndexableField(a, name, stored));
         }
         return values;
      }

      if (Iterator.class.isAssignableFrom(valueClass)) {
         List<IndexableField> values = new ArrayList<>();
         for (Object a : Iterables.asIterable(Cast.<Iterator<?>>as(value))) {
            values.addAll(toIndexableField(a, name, stored));
         }
         return values;
      }

      if (Map.class.isAssignableFrom(valueClass)) {
         List<IndexableField> values = new ArrayList<>();
         Map<?, ?> m = Cast.as(value);
         for (Map.Entry<?, ?> entry : m.entrySet()) {
            String newName = name + "." + Converter.convertSilently(entry.getKey(), String.class);
            values.addAll(toIndexableField(entry.getValue(), newName, stored));
         }
         return values;
      }

      if (Multimap.class.isAssignableFrom(valueClass)) {
         List<IndexableField> values = new ArrayList<>();
         Multimap<?, ?> m = Cast.as(value);
         for (Object key : m.keySet()) {
            String newName = name + "." + Converter.convertSilently(key, String.class);
            for (Object o : m.get(key)) {
               values.addAll(toIndexableField(o, newName, stored));
            }
         }
         return values;
      }

      if (CharSequence.class.isAssignableFrom(valueClass)) {
         return Collections.singleton(new StringField(name, value.toString(), store));
      }

      return new BeanFieldType(value.getClass(), stored)
            .process(value, name, stored);
   }

   public static FieldType typeOf(@NonNull Class<?> valueClass) {
      valueClass = Primitives.wrap(valueClass);
      FieldType type = classToFieldType.get(valueClass);
      if (type == null) {
         return new BlobFieldType(valueClass);
      }
      return type;
   }

   public static void updateIndexableField(@NonNull IndexDocument document, @NonNull String name, @NonNull Object value, boolean stored) {
      document.removeAll(name);
      toIndexableField(value, name, stored).forEach(document::add);
   }
}//END OF Fields
