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
import lombok.*;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@Value(staticConstructor = "indexedField")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class IndexedField {
   @NonNull
   String name;
   @NonNull
   FieldType type;

   public static IndexedField doubleField(String name) {
      return new IndexedField(Validation.notNullOrBlank(name), FieldType.Double);
   }

   public static IndexedField floatField(String name) {
      return new IndexedField(Validation.notNullOrBlank(name), FieldType.Float);
   }

   public static IndexedField fullTextField(String name) {
      return new IndexedField(Validation.notNullOrBlank(name), FieldType.FullText);
   }

   public static IndexedField intField(String name) {
      return new IndexedField(Validation.notNullOrBlank(name), FieldType.Integer);
   }

   public static IndexedField longField(String name) {
      return new IndexedField(Validation.notNullOrBlank(name), FieldType.Long);
   }

   public static IndexedField stringField(String name) {
      return new IndexedField(Validation.notNullOrBlank(name), FieldType.String);
   }

}//END OF IndexedField
