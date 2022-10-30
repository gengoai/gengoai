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

import com.gengoai.collection.multimap.Multimap;
import com.gengoai.conversion.Val;
import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.BeanMap;
import lombok.NonNull;
import org.apache.lucene.index.IndexableField;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * <p>A collection of fields and values that will be stored in a Lucene index as a logical document. Fields are defined
 * by adding them with their associated value to the document.</p>
 *
 * @author David B. Bracewell
 */
public interface IndexDocument {

   /**
    * Creates a new IndexDocument
    *
    * @return the IndexDocument
    */
   static IndexDocument create() {
      return new ObjectDocument();
   }

   /**
    * Creates a new IndexDocument by converting the given object to Json.
    *
    * @param object the object
    * @return the IndexDocument
    */
   static IndexDocument from(@NonNull Object object) {
      return new ObjectDocument(JsonEntry.from(object).asMap());
   }

   /**
    * Creates a new IndexDocument from the map of field names and values
    *
    * @param map the map of field names in values
    * @return the IndexDocument
    */
   static IndexDocument from(@NonNull Map<String, ?> map) {
      ObjectDocument doc = new ObjectDocument();
      map.forEach(doc::add);
      return doc;
   }

   /**
    * Creates a new IndexDocument from the map of field names and values
    *
    * @param map the map of field names in values
    * @return the IndexDocument
    */
   static IndexDocument from(@NonNull Multimap<String, ?> map) {
      ObjectDocument doc = new ObjectDocument();
      map.entries().forEach(e -> doc.add(e.getKey(), e.getValue()));
      return doc;
   }

   /**
    * Creates an IndexDocument treating the given object as a bean and determining the best field types for each
    * property. Note: all properties will be stored.
    *
    * @param object the Java Bean
    * @return the IndexDocument
    */
   static IndexDocument fromBean(@NonNull Object object) {
      ObjectDocument doc = new ObjectDocument();
      new BeanMap(object).forEach(doc::add);
      return doc;
   }

   /**
    * Adds a value to the field with the given name.
    *
    * @param name  the field name
    * @param value the value of the field
    * @return this IndexDocument
    */
   IndexDocument add(@NonNull String name, @NonNull Object value);

   /**
    * Adds an IndexableField to the document. Note that field must not be defined in the schema.
    *
    * @param field the field
    * @return this IndexDocument
    */
   IndexDocument add(@NonNull IndexableField field);

   <T> T toObject(@NonNull Type type);

   /**
    * Converts this document into an Iterable of IndexableField utilizing the given {@link IndexConfig}.
    *
    * @param config the schema to use for converting this document
    * @return the Iterable of IndexableField
    */
   Iterable<? extends IndexableField> asIndexableFields(@NonNull IndexConfig config);

   /**
    * Clears all fields from this document.
    */
   void clear();

   /**
    * Gets the first value for the field with the give name.
    *
    * @param name the name of the field
    * @return the value of the field
    */
   Val get(@NonNull String name);

   /**
    * Gets all values for the field with the give name.
    *
    * @param name the name of the field
    * @return the value of the field
    */
   Val getAll(@NonNull String name);

   /**
    * Gets the first value for the blob field with the give name.
    *
    * @param <T>  the object type
    * @param name the name of the field
    * @return the value of the field
    */
   <T> List<T> getAllBlobField(@NonNull String name);

   /**
    * Gets the first value for the blob field with the give name.
    *
    * @param <T>  the object type
    * @param name the name of the field
    * @return the value of the field
    */
   <T> T getBlobField(@NonNull String name);

   /**
    * Gets the field names specified on this document
    *
    * @return the field names specified on this document
    */
   Set<String> getFields();


   /**
    * Checks if a value exists for the field with the given name
    *
    * @param name the field name
    * @return True - the field exists on the document, False - otherwise
    */
   boolean hasField(@NonNull String name);

   /**
    * Removes the first value of the field with the given name.
    *
    * @param name the field name
    */
   void remove(@NonNull String name);

   /**
    * Removes all values of the field with the given name.
    *
    * @param name the  field name
    */
   void removeAll(@NonNull String name);

   /**
    * Replaces all values for the field with given name with the given values.
    *
    * @param name   the field name
    * @param values the new values
    */
   void replaceField(@NonNull String name, @NonNull Object... values);

   /**
    * Updates the values of the field with the given name using the given function. Returns true if and only if there
    * were any values to update.
    *
    * @param name           the field name
    * @param updateFunction the update function
    * @return True if values were updated, False otherwise
    */
   boolean updateField(@NonNull String name, @NonNull Function<Val, ?> updateFunction);

}//END OF IndexDocument