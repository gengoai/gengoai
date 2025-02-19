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

import com.gengoai.StringTag;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;

@MetaInfServices
public class StringTagConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if(source instanceof StringTag) {
         return Cast.as(source);
      }
      return new StringTag(source.toString());
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return new Class[]{StringTag.class};
   }
}//END OF StringTagConverter
