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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.Language;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.json.Json;
import com.gengoai.lucene.processor.*;
import com.gengoai.string.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.lucene.document.Document;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class IndexSchema implements Serializable {
   private final Map<String, FieldProcessor> processors = new HashMap<>();
   @JsonProperty
   @Getter
   private final String uniqueTerm;
   @JsonProperty
   @Getter
   private final String serializedDocumentField;


   @JsonCreator
   protected IndexSchema(@JsonProperty("uniqueTerm") String uniqueTerm,
                         @JsonProperty("serializedDocumentField") String serializedDocumentField,
                         @JsonProperty("processors") List<FieldProcessor> processors) {
      this.uniqueTerm = uniqueTerm;
      this.serializedDocumentField = serializedDocumentField;
      for (FieldProcessor processor : processors) {
         this.processors.put(processor.getFieldName(), processor);
      }
   }


   public static IndexSchemaBuilder builder() {
      return new IndexSchemaBuilder(null);
   }

   public static IndexSchemaBuilder builder(String uniqueTerm) {
      return new IndexSchemaBuilder(uniqueTerm);
   }

   public Document createDocument(@NonNull Multimap<String, ?> fieldValues) {
      org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
      forEach(processor -> {
         for (Object value : fieldValues.get(processor.getFieldName())) {
            processor.singleValue(document, value);
         }
      });
      if (Strings.isNotNullOrBlank(serializedDocumentField)) {
         FieldType.STORED.addField(document, serializedDocumentField, Json.dumps(fieldValues));
      }
      return document;
   }


   public Set<String> fields() {
      return Collections.unmodifiableSet(processors.keySet());
   }

   public void forEach(@NonNull Consumer<? super FieldProcessor> consumer) {
      processors.values().forEach(consumer);
   }

   @JsonIgnore
   public FieldProcessor getFieldProcessor(String field) {
      return processors.get(field);
   }


   @JsonIgnore
   public boolean isFieldDefined(String field) {
      return processors.containsKey(field);
   }

   @JsonProperty
   public Collection<FieldProcessor> processors() {
      return Collections.unmodifiableCollection(processors.values());
   }


   public static class IndexSchemaBuilder {
      private final List<FieldProcessor> processors = new ArrayList<>();
      private final String uniqueTerm;
      private String serializedDocumentField;

      public IndexSchemaBuilder(String uniqueTerm) {
         this.uniqueTerm = uniqueTerm;
      }

      public IndexSchema build() {
         return new IndexSchema(uniqueTerm,
                                serializedDocumentField,
                                processors);
      }

      public IndexSchemaBuilder doubleField(String name, boolean stored) {
         return add(new DoubleFieldProcessor(name, stored));
      }

      public IndexSchemaBuilder intField(String name, boolean stored) {
         return add(new IntegerFieldProcessor(name, stored));
      }

      public IndexSchemaBuilder serializedDocumentField(String serializedDocumentField) {
         this.serializedDocumentField = Strings.emptyToNull(serializedDocumentField);
         return this;
      }

      public IndexSchemaBuilder stringField(String name, boolean stored) {
         return add(new StringFieldProcessor(name, stored));
      }

      public IndexSchemaBuilder textField(String name, boolean stored, boolean removeStopWords) {
         return add(new TextFieldProcessor(name,
                                           stored,
                                           Language.fromLocale(Locale.getDefault()),
                                           removeStopWords));
      }

      public IndexSchemaBuilder textField(String name, boolean stored) {
         return add(new TextFieldProcessor(name,
                                           stored,
                                           Language.fromLocale(Locale.getDefault()),
                                           true));
      }

      public IndexSchemaBuilder textField(String name, boolean stored, @NonNull Language language, boolean removeStopWords) {
         return add(new TextFieldProcessor(name,
                                           stored,
                                           language,
                                           removeStopWords));
      }

      private IndexSchemaBuilder add(FieldProcessor processor) {
         if (Strings.safeEquals(processor.getFieldName(), uniqueTerm, true)) {
            throw new RuntimeException("Attempting to redefine the primary key.");
         }
         processors.add(processor);
         return this;
      }

   }

}//END OF IndexSchema
