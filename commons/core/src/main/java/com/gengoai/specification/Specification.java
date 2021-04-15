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
 *
 */

package com.gengoai.specification;

import com.gengoai.Validation;
import com.gengoai.collection.Iterables;
import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.config.Config;
import com.gengoai.conversion.Converter;
import com.gengoai.reflection.RField;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import com.gengoai.reflection.TypeUtils;
import com.gengoai.string.Strings;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gengoai.string.Re.*;

/**
 * A specification defines a <code>Schema</code>, <code>Protocol</code>, <code>SubProtocols</code>, <code>Path</code>,
 * and <code>Query Parameters</code> that define a resource, connection, etc. The specification form is as follows:
 * <code>SCHEMA:(PROTOCOL(:SUB-PROTOCOL)*)?(::PATH)?(;query=value)</code> and example is:
 * <code>kv:mem:people</code> defining an in-memory key-value store with the namespace "people" or
 * <code>kv:disk:people::~/people.db;readOnly=true</code> defining a disk-based key-value store with the namespace
 * "people" stored at ~/people.db and being accessed as read only. Note that the Path and Query Arguments can will be
 * resolved against the current Config allowing for dynamic paths like <code>${BASE_DIR}/myFile</code> for paths and
 * <code>parameter=${parameter.defaultValue}</code> where <code>${BASE_DIR}</code> and
 * <code>${parameter.defaultValue}</code> will be set via the Config.
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = false)
@Builder
public final class Specification implements Serializable {
   private static final Pattern SPEC_PATTERN = Pattern.compile(line(namedGroup("SCHEMA",
         oneOrMore(chars("\\w", "_"))),

         zeroOrOne(":",
               negLookahead(chars("\\w", "_", ":"))),

         namedGroup("PROTOCOL",
               zeroOrMore(q(":"),
                     oneOrMore(
                           chars("\\w", "_")))),

         zeroOrOne(q("::"),
               namedGroup("PATH",
                     oneOrMore(notChars(q(";"))))),

         zeroOrOne(namedGroup("QUERY",
               oneOrMore(q(";"),
                     oneOrMore(ANY))))
         ),
         Pattern.CASE_INSENSITIVE);
   private static final long serialVersionUID = 1L;
   @Getter
   private final String path;
   @NonNull
   @Getter
   private final String protocol;
   @NonNull
   private final ArrayListMultimap<String, String> queryParameters = new ArrayListMultimap<>();
   @NonNull
   @Getter
   private final String schema;
   @NonNull
   private final List<String> subProtocol;

   private Specification(@NonNull String schema,
                         @NonNull String protocol,
                         @NonNull List<String> subProtocol,
                         String path) {
      this.schema = schema;
      this.protocol = protocol;
      this.subProtocol = subProtocol;
      this.path = path;
   }

   private static void getQueryParameters(String query, Multimap<String, String> map) {
      if (Strings.isNotNullOrBlank(query)) {
         Pattern.compile(";")
               .splitAsStream(query.substring(1))
               .map(s -> Arrays.copyOf(s.split("="), 2))
               .forEach(s -> map.put(s[0].trim(), Config.resolveVariables(s[1])));
      }
   }

   /**
    * Parses the specification as a the given {@link Specifiable} type.
    *
    * @param <T>           the Specifiable type parameter
    * @param specification the specification string
    * @param tClass        the Specifiable class
    * @return the Specifiable
    */
   public static <T extends Specifiable> T parse(String specification, @NonNull Class<T> tClass) {
      Specification spec = parse(specification);
      try {
         Reflect r = Reflect.onClass(tClass)
               .allowPrivilegedAccess()
               .create();
         Validation.checkArgument(r.<Specifiable>get().getSchema().equals(spec.getSchema()),
               "Invalid Schema: " + specification);

         for (RField field : r.getFieldsWithAnnotation(Protocol.class, SubProtocol.class,
               Path.class, QueryParameter.class)) {
            field.withAnnotation(Protocol.class,
                  p -> field.set(spec.getProtocol()))
                  .withAnnotation(Path.class,
                        p -> field.set(spec.getPath()))
                  .withAnnotation(SubProtocol.class,
                        p -> {
                           if (p.value() >= 0) {
                              field.set(spec.getSubProtocol(p.value()));
                           } else {
                              field.set(spec.getAllSubProtocol());
                           }
                        })
                  .withAnnotation(QueryParameter.class,
                        qp -> {
                           String key = Strings.isNullOrBlank(qp.value())
                                 ? field.getName()
                                 : qp.value();
                           Type type = field.getType();
                           if (TypeUtils.isAssignable(Iterable.class, type) || TypeUtils.asClass(type)
                                 .isArray()) {
                              field.set(spec.getAllQueryValues(key));
                           } else {
                              Object val = spec.getQueryValue(key, null);
                              if (val != null) {
                                 field.set(val);
                              }
                           }
                        });
         }
         return r.get();
      } catch (ReflectionException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Parses the specification string.
    *
    * @param specificationString the specification string
    * @return the specification
    */
   public static Specification parse(String specificationString) {
      Matcher m = SPEC_PATTERN.matcher(Validation.notNullOrBlank(specificationString));
      if (m.find()) {
         String schema = m.group("SCHEMA");
         String protocolSpec = m.group("PROTOCOL");
         if (Strings.isNullOrBlank(protocolSpec)) {
            protocolSpec = Strings.EMPTY;
         } else {
            protocolSpec = protocolSpec.substring(1);
         }
         String[] protocols = protocolSpec.split(":");
         String protocol = protocols.length > 0
               ? protocols[0]
               : null;
         List<String> subProtocol = protocols.length >= 2
               ? Arrays.asList(Arrays.copyOfRange(protocols, 1, protocols.length))
               : Collections.emptyList();
         Specification specification = new Specification(schema,
               protocol,
               subProtocol,
               Config.resolveVariables(m.group("PATH")));
         getQueryParameters(m.group("QUERY"), specification.queryParameters);
         return specification;
      }
      throw new IllegalArgumentException("Invalid Specification: " + specificationString);
   }

   /**
    * Gets all query values associated with a given parameter
    *
    * @param parameter the parameter
    * @return the query parameter values
    */
   public List<String> getAllQueryValues(String parameter) {
      return Collections.unmodifiableList(queryParameters.get(parameter));
   }

   /**
    * Gets all subprotocol elements defined on the specification
    *
    * @return the list of sub-protocol elements
    */
   public List<String> getAllSubProtocol() {
      return Collections.unmodifiableList(subProtocol);
   }

   /**
    * Gets the set of query parameter names and values set on the specification.
    *
    * @return the set of query parameter names and values set on the specification.
    */
   public Set<Map.Entry<String, String>> getQueryParameters() {
      return Collections.unmodifiableSet(queryParameters.entries());
   }

   /**
    * Gets the first query parameter value associated with a parameter or the default value if the query parameter is
    * not defined.
    *
    * @param parameter    the parameter
    * @param defaultValue the default value
    * @return the query parameter value
    */
   public String getQueryValue(String parameter, String defaultValue) {
      return Iterables.getFirst(queryParameters.get(parameter), defaultValue);
   }

   /**
    * Gets the sub protocol at the given index or null if the index is invalid.
    *
    * @param index the index of the sub-protocol element
    * @return the sub protocol element or null if the index is invalid
    */
   public String getSubProtocol(int index) {
      return index >= 0 && index < subProtocol.size()
            ? subProtocol.get(index)
            : null;
   }

   public Specification setQueryParameter(@NonNull String name, @NonNull Object value) {
      queryParameters.put(name, Converter.convertSilently(value, String.class));
      return this;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(schema);
      if (Strings.isNotNullOrBlank(protocol)) {
         builder.append(":").append(protocol);
      }
      if (subProtocol != null) {
         subProtocol.forEach(sp -> builder.append(":").append(sp));
      }
      if (Strings.isNotNullOrBlank(path)) {
         builder.append("::").append(path);
      }
      for (Map.Entry<String, String> entry : queryParameters.entries()) {
         builder.append(";").append(entry.getKey()).append("=").append(entry.getValue());
      }
      return builder.toString();
   }

   /**
    * Builder for Specifications
    */
   public final static class SpecificationBuilder implements Serializable {
      private static final long serialVersionUID = 1L;
      private Multimap<String, String> parameters = new ArrayListMultimap<>();
      private List<String> subProtocol = new ArrayList<>();

      /**
       * Builds the specification.
       *
       * @return the specification
       */
      public Specification build() {
         Specification specification = new Specification(schema, protocol, subProtocol, path);
         specification.queryParameters.putAll(parameters);
         return specification;
      }

      /**
       * Clears the set query parameters from builder.
       *
       * @return this specification builder
       */
      public SpecificationBuilder clearQueryParameters() {
         parameters.clear();
         return this;
      }

      /**
       * Sets a query parameter on this builder.
       *
       * @param name  the parameter name
       * @param value the parameter value
       * @return this specification builder
       */
      public SpecificationBuilder queryParameter(String name, String value) {
         parameters.put(name, value);
         return this;
      }

      /**
       * Sets the sub-protocol of this builder.
       *
       * @param index       the position of the sub-protocol
       * @param subProtocol the sub-protocol name
       * @return this specification builder
       */
      public SpecificationBuilder subProtocol(int index, String subProtocol) {
         if (Strings.isNullOrBlank(subProtocol)) {
            return this;
         }
         while (index >= this.subProtocol.size()) {
            this.subProtocol.add(null);
         }
         this.subProtocol.add(index, subProtocol);
         return this;
      }

      /**
       * Sets the only sub-protocol on the specification to the given value
       *
       * @param subProtocol the sub-protocol name
       * @return this specification builder
       */
      public SpecificationBuilder subProtocol(String subProtocol) {
         return subProtocol(Collections.singletonList(subProtocol));
      }

      /**
       * Sets the sub-protocol on the specification to the given list
       *
       * @param subProtocol the list of sub-protocols
       * @return this specification builder
       */
      public SpecificationBuilder subProtocol(@NonNull List<String> subProtocol) {
         this.subProtocol.clear();
         this.subProtocol.addAll(subProtocol);
         return this;
      }

   }//END OF SpecificationBuilder

}//END OF Specification
