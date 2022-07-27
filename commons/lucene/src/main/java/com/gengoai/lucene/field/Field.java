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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.lucene.IndexDocument;
import com.gengoai.lucene.field.types.FieldType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.IndexableField;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public class Field implements Serializable {
   private static final long serialVersionUID = 1L;
   @Getter
   @JsonProperty
   private String name;
   @Getter
   @JsonProperty
   private FieldType type;
   @Getter
   private boolean stored;

   /**
    * Instantiates a new Field.
    *
    * @param name   the name
    * @param type   the type
    * @param stored the stored
    */
   public Field(String name, @NonNull FieldType type, boolean stored) {
      this.name = Validation.notNullOrBlank(name, "The field name must not be null or blank");
      this.type = type;
      this.stored = stored;
   }


   /**
    * Instantiates a new Field.
    *
    * @param name the name
    * @param type the type
    */
   public Field(String name, @NonNull FieldType type) {
      this(name, type, false);
   }

   @JsonIgnore
   public Map<String, Analyzer> getAnalyzers() {
      return Map.of(name, type.getAnalyzer());
   }

   public Optional<Field> getChildField(@NonNull String name) {
      return Optional.empty();
   }

   @Override
   public boolean equals(Object obj) {
      if( obj instanceof Field){
         return name.equals(Cast.<Field>as(obj).name);
      }
      return false;
   }

   public <T> T getValue(@NonNull IndexDocument document) {
      Val val = document.get(getName());
      return val.isNull()
            ? null
            : Cast.as(val.as(type.getValueClass()));
   }

   @Override
   public int hashCode() {
      return name.hashCode();
   }

   /**
    * Process collection.
    *
    * @param value the value
    * @return the collection
    */
   public Collection<IndexableField> process(Object value) {
      return type.process(value, name, stored);
   }

   @Override
   public String toString() {
      return String.format("%s{name='%s', type='%s', stored=%s)",
                           getClass().getSimpleName(),
                           getName(),
                           getType().getClass().getSimpleName(),
                           isStored());
   }

   protected Field makeChildOf(@NonNull Field parent) {
      return new Field(parent.name + "." + name, type, stored);
   }
}//END OF Field
