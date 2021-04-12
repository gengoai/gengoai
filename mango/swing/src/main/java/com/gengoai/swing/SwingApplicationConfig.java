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

package com.gengoai.swing;

import com.gengoai.conversion.Val;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Properties;

public class SwingApplicationConfig implements Serializable {
   private static final long serialVersionUID = 1L;
   protected final Properties properties = new Properties();
   protected Resource file;

   public Val get(@NonNull String name) {
      return Val.of(properties.get(name));
   }

   public void load(@NonNull Resource resource) throws IOException {
      this.file = resource;
      if(resource.exists()) {
         try(Reader reader = resource.reader()) {
            properties.load(reader);
         }
      }
   }

   public void save() throws IOException {
      try(Writer writer = file.writer()) {
         properties.store(writer, Strings.EMPTY);
      }
   }

   public void set(@NonNull String name, @NonNull Object value) {
      properties.put(name, value);
   }

}//END OF SwingApplicationConfig
