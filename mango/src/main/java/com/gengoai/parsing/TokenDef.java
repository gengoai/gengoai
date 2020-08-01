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

import java.io.Serializable;

/**
 * <p>Defines a {@link Tag} and pattern representing a terminal token to be lexed. Variables (sub-tokens) can be
 * defined using an anonymous capture group, i.e. <code>(?&lt;&gt; ... )</code>.</p>
 *
 * @author David B. Bracewell
 */
public interface TokenDef extends Tag, Serializable {

   /**
    * Regular expression pattern for use in a {@link Lexer}
    *
    * @return the pattern
    */
   String getPattern();

   /**
    * Constructs a simple token definition using a {@link com.gengoai.StringTag}
    *
    * @param tag     the tag
    * @param pattern the pattern
    * @return the TokenDef
    */
   static TokenDef token(String tag, String pattern) {
      return new SimpleTokenDef(tag, pattern);
   }

}//END OF TokenDef
