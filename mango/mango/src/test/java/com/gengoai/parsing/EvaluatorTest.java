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
 */

package com.gengoai.parsing;

import com.gengoai.collection.Lists;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class EvaluatorTest {
   Grammar grammar = new Grammar().skip(CommonTypes.WHITESPACE) //Skip Whitespace, but nothing else
                                  .postfix(CommonTypes.PLUS, BinaryInfixOperatorExpression.HANDLER, 10)
                                  .postfix(CommonTypes.MINUS, BinaryInfixOperatorExpression.HANDLER, 10)
                                  .postfix(CommonTypes.MULTIPLY, BinaryInfixOperatorExpression.HANDLER, 20)
                                  .postfix(CommonTypes.DIVIDE, BinaryInfixOperatorExpression.HANDLER, 20)
                                  .prefix(CommonTypes.NUMBER, ValueExpression.NUMERIC_HANDLER)
                                  .postfix(CommonTypes.EXCLAMATION, UnaryOperatorExpression.POSTFIX_OPERATOR_HANDLER,
                                           30);

   Lexer lexer = new RegexLexer(CommonTypes.WHITESPACE,
                                CommonTypes.NUMBER,
                                CommonTypes.PLUS,
                                CommonTypes.MINUS,
                                CommonTypes.MULTIPLY,
                                CommonTypes.DIVIDE,
                                CommonTypes.EXCLAMATION);

   Evaluator<Double> mathEvaluator = new Evaluator<Double>() {
      private static final long serialVersionUID = 1L;

      {
         $(BinaryInfixOperatorExpression.class, CommonTypes.PLUS, boe -> eval(boe.getLeft()) + eval(boe.getRight()));
         $(BinaryInfixOperatorExpression.class, CommonTypes.MINUS, boe -> eval(boe.getLeft()) - eval(boe.getRight()));
         $(BinaryInfixOperatorExpression.class, CommonTypes.MULTIPLY, boe -> eval(boe.getLeft()) * eval(boe.getRight()));
         $(BinaryInfixOperatorExpression.class, CommonTypes.DIVIDE, boe -> eval(boe.getLeft()) / eval(boe.getRight()));
         $(UnaryOperatorExpression.class, CommonTypes.EXCLAMATION, pe -> eval(pe.getValue()) * 10);
         $(ValueExpression.class, v -> v.getValue().as(Double.class));
      }
   };

   ParserGenerator parser = ParserGenerator.parserGenerator(grammar, lexer);


   @Test
   public void test() throws Exception {
      assertEquals(30, parser.create("23 + 4 * 2 - 1 ").evaluate(mathEvaluator), 0);
   }

   @Test
   public void postfix() throws Exception {
      assertEquals(60, parser.create("2 * 3!").evaluate(mathEvaluator), 0);
   }

   @Test
   public void order() throws Exception {
      assertEquals(8, parser.create("2+3 * 2").evaluate(mathEvaluator), 0);
      assertEquals(5, parser.create("2 + 3 * 2 / 2").evaluate(mathEvaluator), 0);
   }

   @Test(expected = ParseException.class)
   public void error() throws Exception {
      parser.create("-2 * 3 + 5").evaluate(mathEvaluator);
   }

   @Test
   public void all() throws Exception {
      assertEquals(Lists.arrayListOf(8d), parser.create("2+3 * 2").evaluateAll(mathEvaluator));
   }

}//END OF EvaluatorTest
