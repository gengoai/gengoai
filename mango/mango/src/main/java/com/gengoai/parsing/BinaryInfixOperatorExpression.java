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

/**
 * An <code>Expression</code> representing a binary operation, eg. plus, minus, etc.
 *
 * @author David B. Bracewell
 */
public class BinaryInfixOperatorExpression extends BaseExpression {
   private static final long serialVersionUID = 1L;
   /**
    * Generic Handler for generating {@link BinaryInfixOperatorExpression}s for infix operators using {@link
    * Parser#parseExpression()} to generate the right-hand value of the operator.
    */
   public static PostfixHandler HANDLER = (p, t, l) -> new BinaryInfixOperatorExpression(t, l, p.parseExpression(t));

   public static PostfixHandler RIGHT_ASSOCIATIVE_HANDLER = (p, t, l) -> new BinaryInfixOperatorExpression(t,
                                                                                                           l,
                                                                                                           p.parseExpression(
                                                                                                              t, true));

   private final Expression left;
   private final String operator;
   private final Expression right;

   /**
    * Instantiates a new BinaryInfixOperatorExpression.
    *
    * @param token the token representing the operator
    * @param left  the left-hand expression
    * @param right the right-hand expression
    */
   public BinaryInfixOperatorExpression(ParserToken token, Expression left, Expression right) {
      super(token.getType());
      this.operator = token.getText();
      this.left = left;
      this.right = right;
   }

   /**
    * Gets the left-hand expression
    *
    * @return the left-hand expression
    */
   public Expression getLeft() {
      return left;
   }

   /**
    * Gets the operator.
    *
    * @return the operator
    */
   public String getOperator() {
      return operator;
   }

   /**
    * Gets the right-hand expression.
    *
    * @return the right-hand expression.
    */
   public Expression getRight() {
      return right;
   }

   @Override
   public String toString() {
      return String.format("%s %s %s", left, operator, right);
   }

}//END OF BinaryOperatorExpression
