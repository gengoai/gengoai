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

import java.util.Objects;

/**
 * An <code>Expression</code> unary operator which contains a single expression value
 *
 * @author David B. Bracewell
 */
public class UnaryOperatorExpression extends BaseExpression {
   private static final long serialVersionUID = 1L;
   /**
    * Generic Handler for generating {@link UnaryOperatorExpression}s for postfix operators.
    */
   public static PostfixHandler POSTFIX_OPERATOR_HANDLER = (parser, token, left) -> new UnaryOperatorExpression(
      token, left, false);
   /**
    * Generic Handler for generating {@link UnaryOperatorExpression}s for prefix operators using {@link
    * Parser#parseExpression()}* to generate the value of the operator.
    */
   public static PrefixHandler PREFIX_OPERATOR_HANDLER = (p, t) -> new UnaryOperatorExpression(t,
                                                                                               p.parseExpression(t),
                                                                                               true);
   private final boolean isPrefix;
   private final String operator;
   private final Expression value;

   /**
    * Instantiates a new Prefix operator expression.
    *
    * @param token    the token
    * @param value    the value
    * @param isPrefix the is prefix
    */
   public UnaryOperatorExpression(ParserToken token, Expression value, boolean isPrefix) {
      super(token.getType());
      this.operator = token.getText();
      this.value = value;
      this.isPrefix = isPrefix;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof UnaryOperatorExpression)) return false;
      UnaryOperatorExpression that = (UnaryOperatorExpression) o;
      return isPrefix == that.isPrefix &&
                Objects.equals(getType(), that.getType()) &&
                Objects.equals(operator, that.getOperator()) &&
                Objects.equals(value, that.value);
   }

   /**
    * Gets operator.
    *
    * @return the operator
    */
   public String getOperator() {
      return operator;
   }

   /**
    * Gets the {@link Expression} representing the value of the operator.
    *
    * @return the value
    */
   public Expression getValue() {
      return value;
   }

   @Override
   public int hashCode() {
      return Objects.hash(value, isPrefix, getType(), operator);
   }

   /**
    * Is prefix boolean.
    *
    * @return the boolean
    */
   public boolean isPrefix() {
      return isPrefix;
   }

   @Override
   public String toString() {
      if (isPrefix) {
         return String.format("%s%s", operator, value);
      }
      return String.format("%s%s", value, operator);
   }

}//END OF PrefixOperatorExpression
