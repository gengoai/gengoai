/*
 * (c) 2005 David B. Bracewell
 *
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

package com.gengoai.graph;

import com.gengoai.Validation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.gengoai.Validation.notNull;

/**
 * A generic vertex class which has a label and set of properties. While this class can be used directly in a graph, its
 * main purpose is to act as an intermediary object when reading, writing, and rendering graphs.
 *
 * @author David B. Bracewell
 */
public class Vertex implements Serializable {
   private static final long serialVersionUID = 1L;
   private final String label;
   private final Map<String, String> properties;


   private Vertex(String label, Map<String, String> properties) {
      Validation.notNullOrBlank(label, "label cannot be null");
      this.label = label;
      this.properties = notNull(properties);
   }


//   /**
//    * Static method for deserializing Vertex objects from {@link JsonEntry}s
//    *
//    * @param entry  the json entry
//    * @param params the type parameters
//    * @return the vertex
//    */
//   public static Vertex fromJson(JsonEntry entry, Type... params) {
//      return new Vertex(entry.getStringProperty("label"),
//                        entry.getProperty("properties").getAsMap(String.class));
//   }
//
//
//   @Override
//   public JsonEntry toJson() {
//      return JsonEntry.object()
//                      .addProperty("label", label)
//                      .addProperty("properties", properties);
//   }

   /**
    * Builder vertex builder.
    *
    * @return the vertex builder
    */
   public static VertexBuilder builder() {
      return new VertexBuilder();
   }


   /**
    * Gets label.
    *
    * @return the label
    */
   public String getLabel() {
      return this.label;
   }

   /**
    * Gets properties.
    *
    * @return the properties
    */
   public Map<String, String> getProperties() {
      return this.properties;
   }

   @Override
   public int hashCode() {
      return Objects.hash(label);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {return true;}
      if (obj == null || getClass() != obj.getClass()) {return false;}
      final Vertex other = (Vertex) obj;
      return Objects.equals(this.label, other.label);
   }

   @Override
   public String toString() {
      return "Vertex(label=" + this.label + ", properties=" + this.properties + ")";
   }

   /**
    * The type Vertex builder.
    */
   public static class VertexBuilder {
      private String label;
      private Map<String, String> properties = new HashMap<>();

      /**
       * Instantiates a new Vertex builder.
       */
      VertexBuilder() {
      }

      /**
       * Build vertex.
       *
       * @return the vertex
       */
      public Vertex build() {
         return new Vertex(label, new HashMap<>(properties));
      }

      /**
       * Clear properties vertex builder.
       *
       * @return the vertex builder
       */
      public VertexBuilder clearProperties() {
         this.properties.clear();
         return this;
      }

      /**
       * Label vertex builder.
       *
       * @param label the label
       * @return the vertex builder
       */
      public VertexBuilder label(String label) {
         this.label = label;
         return this;
      }

      /**
       * Properties vertex builder.
       *
       * @param properties the properties
       * @return the vertex builder
       */
      public VertexBuilder properties(Map<? extends String, ? extends String> properties) {
         this.properties.clear();
         this.properties.putAll(properties);
         return this;
      }

      /**
       * Property vertex builder.
       *
       * @param propertyKey   the property key
       * @param propertyValue the property value
       * @return the vertex builder
       */
      public VertexBuilder property(String propertyKey, String propertyValue) {
         this.properties.put(propertyKey, propertyValue);
         return this;
      }

      @Override
      public String toString() {
         return "VertexBuilder{" +
                   "label='" + label + '\'' +
                   ", properties=" + properties +
                   '}';
      }
   }
}//END OF Vertex
