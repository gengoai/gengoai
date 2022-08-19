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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Field {
   @NonNull String name;
   @NonNull FieldType type;

   protected Field(String name, @NonNull FieldType type) {
      this.name = Validation.notNullOrBlank(name, "Name must not be null or blank");
      this.type = type;
   }

   public static Field booleanField(String name) {
      return field(name, FieldType.Boolean);
   }

   public static Field field(String name, @NonNull FieldType fieldType) {
      return new Field(name, fieldType);
   }

   public static Field floatField(String name) {
      return field(name, FieldType.Float);
   }

   public static Field fullTextField(String name) {
      return field(name, FieldType.FullText);
   }

   public static Field geoSpatialField(String name) {
      return field(name, FieldType.GeoSpatial);
   }

   public static Field intField(String name) {
      return field(name, FieldType.Integer);
   }

   public static Field stringField(String name) {
      return field(name, FieldType.String);
   }

   @Override
   public int hashCode() {
      return name.hashCode();
   }

}//END OF Field