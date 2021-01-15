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

import com.gengoai.io.resource.Resource;

import java.io.IOException;
import java.io.Serializable;

/**
 * <p>A Lexer tokenizes a string or resource into tokens.</p>
 *
 * @author David B. Bracewell
 */
public interface Lexer extends Serializable {

   /**
    * Tokenizes the input string into tokens
    *
    * @param input the input to tokenize
    * @return A token stream wrapping the tokenization results
    */
   TokenStream lex(String input);

   /**
    * Reads from the given resource and tokenizes it into tokens
    *
    * @param resource the resource to read and tokenize
    * @return A token stream wrapping the tokenization results
    * @throws IOException Something went wrong reading from the input resource
    */
   default TokenStream lex(Resource resource) throws IOException {
      return lex(resource.readToString());
   }

   /**
    * Creates a regular expression based lexer over the given token definitions.
    *
    * @param tokens the token definitions that include the token type (tag) and the regular expression pattern for
    *               matching.
    * @return the constructed lexer
    */
   static Lexer create(TokenDef... tokens) {
      return new RegexLexer(tokens);
   }


}//END OF Lexer
