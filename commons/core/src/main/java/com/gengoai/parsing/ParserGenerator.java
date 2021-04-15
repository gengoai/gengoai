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
 * Generates a {@link Parser} objects that uses a pre-defined {@link Grammar} to parse the {@link ParserToken}s
 * extracted using the pre-defined {@link Lexer}.
 *
 * @author David B. Bracewell
 */
public class ParserGenerator implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Grammar grammar;
   private final Lexer lexer;

   /**
    * Creates a {@link ParserGenerator} with the given {@link Grammar} amd {@link Lexer}.
    *
    * @param grammar the grammar
    * @param lexer   the lexer
    * @return the parser generator
    */
   public static ParserGenerator parserGenerator(Grammar grammar, Lexer lexer) {
      return new ParserGenerator(grammar, lexer);
   }

   private ParserGenerator(Grammar grammar, Lexer lexer) {
      this.grammar = grammar;
      this.lexer = lexer;
   }

   /**
    * Creates a {@link Parser} over the lexed tokens in the give input
    *
    * @param input the input to parser
    * @return the Parser
    */
   public Parser create(String input) {
      return new Parser(grammar, lexer.lex(input));
   }

   /**
    * Creates a {@link Parser} over the lexed tokens in the give input
    *
    * @param input the input to parser
    * @return the Parser
    */
   public Parser create(Resource input) throws IOException {
      return new Parser(grammar, lexer.lex(input));
   }

}//END OF ParserGenerator
