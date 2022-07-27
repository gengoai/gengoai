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

package com.gengoai.conversion;

import com.gengoai.Primitives;
import com.gengoai.json.JsonEntry;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@MetaInfServices(TypeConverter.class)
public class OffsetDateTimeConverter implements TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {

      if (source instanceof OffsetDateTime) {
         return source;
      }

      if (source instanceof JsonEntry) {
         JsonEntry je = Cast.as(source);
         return convert(je.get());
      }

      if (source instanceof LocalDateTime) {
         LocalDateTime ldt = Cast.as(source);
         return ldt.atOffset(ZoneOffset.UTC);
      }

      if (source instanceof LocalDate) {
         LocalDate ld = Cast.as(source);
         return ld.atStartOfDay().atOffset(ZoneOffset.UTC);
      }

      if (source instanceof CharSequence) {
         try {
            return OffsetDateTime.parse(source.toString());
         } catch (DateTimeParseException e) {
            //pass
         }
         Long l = Primitives.tryParseLong(source.toString());
         if (l != null) {
            source = l;
         }
      }

      if (source instanceof Number) {
         Number n = Cast.as(source);
         try {
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(n.longValue()), ZoneOffset.UTC);
         } catch (DateTimeException e) {
            //pass
         }
      }

      Date date = Converter.convertSilently(source, Date.class);
      if (date != null) {
         return date.toInstant().atOffset(ZoneOffset.UTC);
      }

      throw new TypeConversionException(source, OffsetDateTime.class);
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return new Class[]{OffsetDateTime.class};
   }
}//END OF OffsetDateTimeConverter
