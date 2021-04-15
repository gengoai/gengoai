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

package com.gengoai.documentdb;

import com.gengoai.Validation;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class DocumentDBSchema {
   @Getter
   private final String primaryKey;
   private LinkedHashMap<String, FieldType> fieldTypes = new LinkedHashMap<>();

   private DocumentDBSchema(String primaryKey, FieldType type) {
      this.primaryKey = primaryKey;
      this.fieldTypes.put(primaryKey, type);
   }


   public static DocumentDBSchema primaryKey(String name, @NonNull FieldType type) {
      return new DocumentDBSchema(Validation.notNullOrBlank(name), type);
   }

   public static DocumentDBSchema primaryKey(String name) {
      return new DocumentDBSchema(Validation.notNullOrBlank(name), FieldType.String);
   }

   public DocumentDBSchema doubleField(String name) {
      return field(name, FieldType.Double);
   }

   public DocumentDBSchema embeddingField(String name) {
      return field(name, FieldType.Embedding);
   }

   public DocumentDBSchema field(String name, @NonNull FieldType type) {
      this.fieldTypes.put(Validation.notNullOrBlank(name), type);
      return this;
   }

   public DocumentDBSchema floatField(String name) {
      return field(name, FieldType.Float);
   }

   public DocumentDBSchema fullTextField(String name) {
      return field(name, FieldType.FullText);
   }

   public DocumentDBSchema geoSpatialField(String name) {
      return field(name, FieldType.GeoSpatial);
   }

   public Map<String, FieldType> getFieldTypes() {
      return Collections.unmodifiableMap(fieldTypes);
   }

   public DocumentDBSchema intField(String name) {
      return field(name, FieldType.Integer);
   }

   public DocumentDBSchema stringField(String name) {
      return field(name, FieldType.String);
   }

   public DocumentDBSchema variableField(String name) {
      return field(name, FieldType.None);
   }

   public DocumentDBSchema booleanField(String name) {
      return field(name,FieldType.Boolean);
   }


}//END OF DocumentDBSchema
