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

package com.gengoai.hermes.workflow;

import com.gengoai.hermes.Hermes;
import com.gengoai.io.Resources;
import com.gengoai.parsing.*;
import com.gengoai.string.Strings;

import static com.gengoai.string.Re.*;

/**
 * @author David B. Bracewell
 */
public enum WorkflowTypes implements GrammarRegistrable, TokenDef {
   keyword(re(q("@"), oneOrMore(chars("a-zA-Z")))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(keyword, (parser, token) -> new ValueExpression(keyword, token.getText()));
      }
   },
   COMMENT(re("#", oneOrMore(notChars("\r\n")))) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            return new ValueExpression(COMMENT, Strings.EMPTY);
         });
      }
   },
   COMMA(","),
   BEGIN_DEF(q("{")) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(BEGIN_DEF, ListExpression.handler(BEGIN_DEF, END_DEF, COMMA, "{", "}", ","));
      }
   },
   QUOTED_STRING(re("\"",
                    namedGroup("", oneOrMore(or(ESC_BACKSLASH + ".", notChars("\"")))),
                    "\"")) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            final String unescaped = Strings.unescape(token.getVariable(0), '\\');
            return new ValueExpression(QUOTED_STRING, unescaped);
         });
      }
   },
   IDENTIFIER_STRING(Hermes.IDENTIFIER) {
      @Override
      public void register(Grammar grammar) {
         grammar.prefix(this, (parser, token) -> {
            return new ValueExpression(QUOTED_STRING, token.getText());
         });
      }
   },
   END_DEF(q("}")),
   EQ("=") {
      @Override
      public void register(Grammar grammar) {
         grammar.postfix(EQ, (parser, token, left) -> new BinaryInfixOperatorExpression(token, left, parser
               .parseExpression()));
      }
   };


   private final String pattern;

   WorkflowTypes(String pattern) {
      this.pattern = pattern;
   }

   public static void main(String[] args) throws Exception {
      Lexer lexer = Lexer.create(WorkflowTypes.values());
      Grammar grammar = new Grammar(WorkflowTypes.values());
      grammar.skip(COMMENT);
      ParserGenerator pg = ParserGenerator.parserGenerator(grammar, lexer);
      Parser parser = pg.create(Resources.from("/home/ik/test.workflow"));
      while (parser.hasNext()) {
         Expression exp = parser.parseExpression();
         if (exp.isInstance(ListExpression.class)) {
            ListExpression le = exp.as(ListExpression.class);
            for (Expression expression : le) {
               System.out.println(expression);
            }
         }
      }
   }

   @Override
   public String getPattern() {
      return pattern;
   }

   @Override
   public void register(Grammar grammar) {
   }

}//END OF WorkflowTypes
