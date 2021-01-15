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

/**
 * @author David B. Bracewell
 */
public class ParserTest {

//   @Test
//   public void test() throws Exception {
//      RegexLexer lexer = new RegexLexer(CommonTypes.EQUALS,
//                                        CommonTypes.POUND,
//                                        CommonTypes.WHITESPACE,
//                                        CommonTypes.PERIOD,
//                                        CommonTypes.NUMBER,
//                                        CommonTypes.PLUS,
//                                        CommonTypes.COMMA,
//                                        CommonTypes.NEWLINE,
//                                        define(CommonTypes.WORD, "[a-zA-z]\\w*"));
//
//      Parser parser2 = new Parser(new TestGrammar(), lexer);
//
//      ExpressionIterator parser = parser2.parse(
//         "(1+2)\nmethod(arg1,arg2)\n#This is a comment.\n... var=(100+34)"
//                                               );
//
//
//      Expression exp1 = parser.next();
//      assertEquals("(1 + 2)", exp1.toString());
//      assertNotNull(exp1.as(BinaryOperatorExpression.class));
//      Expression left = exp1.as(BinaryOperatorExpression.class).left;
//      Expression right = exp1.as(BinaryOperatorExpression.class).right;
//      String operator = exp1.as(BinaryOperatorExpression.class).operator.text;
//      assertEquals("1", left.toString());
//      assertEquals("2", right.toString());
//      assertEquals("+", operator);
//
//      Expression exp2 = parser.next();
//      assertEquals("(method[arg1, arg2])", exp2.toString());
//      assertNotNull(exp2.as(MethodCallExpression.class));
//      assertEquals("method", exp2.as(MethodCallExpression.class).methodName);
//      assertEquals(2, exp2.as(MethodCallExpression.class).arguments.size());
//      assertEquals("arg1", exp2.as(MethodCallExpression.class).arguments.get(0).toString());
//      assertEquals("arg2", exp2.as(MethodCallExpression.class).arguments.get(1).toString());
//
//      Expression comment = parser.next();
//      assertEquals("#This is a comment.", comment.toString());
//
//      Expression last = parser.next();
//      assertTrue(last instanceof AssignmentExpression);
//      AssignmentExpression a = (AssignmentExpression) last;
//      assertEquals("var", a.variableName);
//      assertEquals("=", a.operator);
//      assertEquals("(100 + 34)", a.right.toString());
//   }
//
//   private static class TestGrammar extends Grammar {
//
//      public TestGrammar() {
//         super(true);
//         register(CommonTypes.POUND, new CommentHandler(CommonTypes.NEWLINE.getTag()));
//         register(CommonTypes.OPENPARENS, new GroupHandler(CommonTypes.CLOSEPARENS.getTag()));
//         register(CommonTypes.WORD, new ValueHandler());
//         register(CommonTypes.NUMBER, new ValueHandler());
//         register(CommonTypes.PLUS, new BinaryOperatorHandler(3, true));
//         register(CommonTypes.EQUALS, new AssignmentHandler(10));
//         register(CommonTypes.POUND, new CommentHandler(CommonTypes.NEWLINE.getTag()));
//      }
//
//   }

}

