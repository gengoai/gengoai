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

import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class LocalDateTimeConverter implements TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof LocalDateTime) {
         return source;
      } else if (source instanceof LocalDate) {
         return Cast.<LocalDate>as(source).atStartOfDay();
      } else if (source instanceof Number) {
         try {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(((Number) source).longValue()),
                                           ZoneOffset.UTC);
         } catch (RuntimeException e){
            // pass
         }
      }
      Date date = Converter.convertSilently(source, Date.class);
      if (date != null) {
         return date.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
      }
      throw new TypeConversionException(source, LocalDateTime.class);
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return new Class[]{LocalDateTime.class};
   }
}//END OF LocalDateTimeConverter
