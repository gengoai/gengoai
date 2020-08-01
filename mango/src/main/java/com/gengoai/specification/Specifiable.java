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

import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.reflection.RField;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import com.gengoai.string.Strings;

import java.lang.reflect.Array;
import java.util.List;

import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * Classes implementing Specifiable can be constructed and described using {@link Specification} syntax. Note: that all
 * implementing classes should have a no-argument constructor.
 *
 * @author David B. Bracewell
 */
public interface Specifiable {

   /**
    * Gets the schema of this specifiable. Will be used for validation when parsing the specification string.
    *
    * @return the schema
    */
   String getSchema();

   /**
    * Generates a specification strings from this instance.
    *
    * @return the specification string
    */
   default String toSpecification() {
      Specification.SpecificationBuilder b = Specification.builder();
      b.schema(getSchema());
      try {
         for (RField field : Reflect.onObject(this)
                                    .allowPrivilegedAccess()
                                    .getFieldsWithAnnotation(Protocol.class, SubProtocol.class,
                                                             Path.class, QueryParameter.class)) {
            field.withAnnotation(Protocol.class,
                                 (p) -> b.protocol(Converter.convert(field.get(), String.class)))
                 .withAnnotation(Path.class,
                                 (p) -> b.path(Converter.convert(field.get(), String.class)))
                 .withAnnotation(SubProtocol.class,
                                 (subProtocol) -> {
                                    if (subProtocol.value() >= 0) {
                                       b.subProtocol(subProtocol.value(),
                                                     Converter.convert(field.get(), String.class));
                                    } else {
                                       b.subProtocol(
                                          Converter.<List<String>>convert(field.get(),
                                                                          parameterizedType(List.class,
                                                                                            String.class)));
                                    }
                                 })
                 .withAnnotation(QueryParameter.class,
                                 (qp) -> {
                                    String key = Strings.isNullOrBlank(qp.value()) ? field.getName() : qp.value();
                                    Object o = field.get();
                                    if (o == null) {
                                       return;
                                    }
                                    if (o instanceof Iterable) {
                                       for (Object a : Cast.<Iterable<?>>as(o)) {
                                          b.queryParameter(key, Converter.convert(a, String.class));
                                       }
                                    } else if (o.getClass().isArray()) {
                                       for (int i = 0; i < Array.getLength(o); i++) {
                                          b.queryParameter(key, Converter.convert(Array.get(o, i), String.class));
                                       }
                                    } else {
                                       b.queryParameter(key, Converter.convert(o, String.class));
                                    }
                                 });
         }
      } catch (ReflectionException e) {
         throw new RuntimeException(e);
      }
      return b.build().toString();
   }

}//END OF Specifiable
