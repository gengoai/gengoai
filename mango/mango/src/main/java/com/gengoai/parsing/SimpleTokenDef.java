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

package com.gengoai.parsing;

import com.gengoai.Tag;

/**
 * Default {@link TokenDef} implementation.
 *
 * @author David B. Bracewell
 */
class SimpleTokenDef implements TokenDef {
   private static final long serialVersionUID = 1L;
   private final String pattern;
   private final String tag;

   /**
    * Instantiates a new SimpleTokenDef.
    *
    * @param tag     the tag
    * @param pattern the pattern
    */
   SimpleTokenDef(String tag, String pattern) {
      this.tag = tag;
      this.pattern = pattern;
   }


   @Override
   public String getPattern() {
      return pattern;
   }

   @Override
   public boolean isInstance(Tag tag) {
      return this.tag.equals(tag.name());
   }

   @Override
   public String name() {
      return tag;
   }
}//END OF SimpleTokenDef
