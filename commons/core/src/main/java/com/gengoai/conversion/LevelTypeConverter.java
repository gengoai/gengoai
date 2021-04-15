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

package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.logging.Level;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class LevelTypeConverter implements TypeConverter {
   static final String SIMPLE_NAME = Level.class.getSimpleName() + ".";
   static final String FULL_NAME = Level.class.getName() + ".";

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      try {
         String str = source.toString();
         if (str.startsWith(SIMPLE_NAME)) {
            str = str.substring(SIMPLE_NAME.length() + 1);
         } else if (str.startsWith(FULL_NAME)) {
            str = str.substring(FULL_NAME.length() + 1);
         }
         return Level.parse(str);
      } catch (IllegalArgumentException e) {
         throw new TypeConversionException(source, Level.class);
      }
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return new Class[]{Level.class};
   }
}//END OF LevelTypeConverter
