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

package com.gengoai.lucene.field.types;

import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.Collections;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@Log
@EqualsAndHashCode(callSuper = true)
@MetaInfServices
public class BooleanFieldType extends FieldType {

   public BooleanFieldType() {
      super(Boolean.class);
   }

   @Override
   @SneakyThrows
   protected Collection<IndexableField> processImpl(Object value, String outField, boolean store) {
      Boolean bool;
      if (value instanceof Boolean) {
         bool = Cast.as(value);
      } else {
         bool = Converter.convert(value, Boolean.class);
      }
      return Collections.singleton(new StringField(outField,
                                                   bool.toString(),
                                                   store ? org.apache.lucene.document.Field.Store.YES : Field.Store.NO));
   }

}//END OF BooleanFieldType
