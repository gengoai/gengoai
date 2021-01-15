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

package com.gengoai.string;

import com.gengoai.conversion.Cast;
import com.gengoai.reflection.BeanMap;
import lombok.NonNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringResolver {
   private static final Pattern STRING_SUBSTITUTION = Pattern.compile("\\$\\{(.+?)}");

   public static String resolve(@NonNull String string, @NonNull Map<String, String> variables) {
      String rval = string;
      Matcher m = STRING_SUBSTITUTION.matcher(string);
      while(m.find()) {
         if(variables.containsKey(m.group(1))) {
            rval = rval.replace(m.group(0), variables.get(m.group(1)));
         }
      }
      return rval;
   }

   public static String resolve(@NonNull String string, @NonNull Object bean) {
      final BeanMap beanMap = bean instanceof BeanMap
                              ? Cast.as(bean)
                              : new BeanMap(bean);
      String rval = string;
      Matcher m = STRING_SUBSTITUTION.matcher(string);
      while(m.find()) {
         if(beanMap.containsKey(m.group(1))) {
            Object obj = beanMap.get(m.group(1));
            String str = obj == null
                         ? "null"
                         : obj.toString();
            rval = rval.replace(m.group(0), str);
         }
      }
      return rval;
   }

   private StringResolver() {
      throw new IllegalAccessError();
   }

}//END OF StringResolver
