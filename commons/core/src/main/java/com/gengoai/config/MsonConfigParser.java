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

package com.gengoai.config;

import com.gengoai.io.resource.Resource;
import com.gengoai.parsing.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import static com.gengoai.config.ConfigTokenType.*;

/**
 * Wraps the parsing of Mango Configuration files (MSON) files.
 *
 * @author David B. Bracewell
 */
class MsonConfigParser {
   private static final Lexer MSON_LEXER = input -> {
      ConfigScanner scanner = new ConfigScanner(new StringReader(input));
      return new AbstractTokenStream() {
         @Override
         protected List<ParserToken> next() {
            try {
               ParserToken pt = scanner.next();
               if (pt == null) {
                  return Collections.singletonList(TokenStream.EOF_TOKEN);
               }
               return Collections.singletonList(pt);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         }
      };
   };
   private static final Grammar MSON_GRAMMAR = new Grammar() {
      {
         skip(COMMENT);
         prefix(STRING, ValueExpression.STRING_HANDLER);
         prefix(KEY, ValueExpression.STRING_HANDLER);
         prefix(BEAN, ValueExpression.STRING_HANDLER);
         prefix(BOOLEAN, ValueExpression.BOOLEAN_HANDLER);
         prefix(NULL, ValueExpression.NULL_HANDLER);
         prefix(VALUE_SEPARATOR, ValueExpression.STRING_HANDLER);
         postfix(BEGIN_OBJECT, (parser, token, left) -> {
                    ListExpression list = new ListExpression(token.getType(),
                                                             parser.parseExpressionList(END_OBJECT, null),
                                                             "\n",
                                                             "{",
                                                             "}");
                    return new BinaryInfixOperatorExpression(token, left, list);
                 }, 10,
                 (BinaryInfixOperatorExpression e) -> e.getRight()
                                                       .as(ListExpression.class)
                                                       .stream()
                                                       .allMatch(se -> se.isInstance(EQUAL_PROPERTY,
                                                                                     BEGIN_OBJECT,
                                                                                     APPEND_PROPERTY)));
         prefix(BEGIN_OBJECT,
                ListExpression.handler(BEGIN_OBJECT,
                                       END_OBJECT,
                                       VALUE_SEPARATOR,
                                       ",",
                                       "{",
                                       "}"),
                (ListExpression e) -> e.stream().allMatch(se -> se.isInstance(KEY_VALUE_SEPARATOR)));
         postfix(EQUAL_PROPERTY,
                 BinaryInfixOperatorExpression.HANDLER,
                 5,
                 (BinaryInfixOperatorExpression e) -> e.getLeft().isInstance(ValueExpression.class) &&
                    e.getLeft().getType().isInstance(STRING, KEY));
         postfix(APPEND_PROPERTY,
                 BinaryInfixOperatorExpression.HANDLER,
                 5,
                 (BinaryInfixOperatorExpression e) -> e.getLeft().isInstance(ValueExpression.class) &&
                    e.getLeft().getType().isInstance(STRING, KEY));
         postfix(KEY_VALUE_SEPARATOR,
                 BinaryInfixOperatorExpression.HANDLER,
                 5,
                 (BinaryInfixOperatorExpression e) -> e.getLeft().isInstance(ValueExpression.class) &&
                    e.getLeft().getType().isInstance(STRING, KEY));
         prefix(IMPORT,
                UnaryOperatorExpression.PREFIX_OPERATOR_HANDLER,
                (UnaryOperatorExpression e) -> e.getValue().isInstance(ValueExpression.class) &&
                   e.getValue().getType().isInstance(STRING, KEY));
         prefix(BEGIN_ARRAY, ListExpression.handler(BEGIN_ARRAY,
                                                    END_ARRAY,
                                                    VALUE_SEPARATOR,
                                                    ",",
                                                    "[",
                                                    "]"
                                                   ));
      }
   };

   /**
    * Parse resource.
    *
    * @param resource the resource
    * @throws IOException    the io exception
    * @throws ParseException the parse exception
    */
   public static void parseResource(Resource resource) throws IOException, ParseException {
      new Parser(MSON_GRAMMAR, MSON_LEXER.lex(resource)).evaluateAll(new MsonEvaluator(resource.descriptor()));
   }

}//END OF MsonConfigParser2
