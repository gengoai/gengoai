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

package com.gengoai.sql.operator;

import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLElement;
import lombok.NonNull;

/**
 * Interface defining an {@link SQLElement} that is can be an argument to an {@link SQLOperator}.
 */
public interface SQLOperable extends SQLElement {

   /**
    * Adds this element with the given element.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable add(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.ADD, this, rhs);
   }

   /**
    * Adds this element with the given number.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable add(@NonNull Number rhs) {
      return add(SQL.N(rhs));
   }

   /**
    * Ands this element with given element.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable and(@NonNull SQLElement rhs) {
      return SQL.and(this, rhs);
   }

   default SQLOperable as(@NonNull String name) {
     return new SQLBinaryOperator("AS", SQL.group(this), SQL.C(name));
   }

   /**
    * Informs the query that this element is to be sorted in ascending order.
    *
    * @return the SQLOperable
    */
   default SQLOperable asc() {
      return new SQLPostfixUnaryOperator(SQLOperator.ASC, this, false);
   }

   /**
    * Creates a BETWEEN operator with this element representing the column name
    *
    * @param lower the lower range of the between
    * @param upper the upper range of the between
    * @return the SQLOperable
    */
   default SQLOperable between(@NonNull SQLElement lower, @NonNull SQLElement upper) {
      return new SQLBetween(this, lower, upper);
   }

   /**
    * Creates a BETWEEN operator with this element representing the column name
    *
    * @param lower the lower range of the between
    * @param upper the upper range of the between
    * @return the SQLOperable
    */
   default SQLOperable between(@NonNull Number lower, @NonNull Number upper) {
      return new SQLBetween(this, SQL.N(lower), SQL.N(upper));
   }

   /**
    * Concatenates this element with the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable concat(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.CONCAT, this, rhs);
   }

   /**
    * Informs the query that this element is to be sorted in descending order.
    *
    * @return the SQLOperable
    */
   default SQLOperable desc() {
      return new SQLPostfixUnaryOperator(SQLOperator.DESC, this, false);
   }

   /**
    * Divides this element by the given element.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable div(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.DIVIDE, this, rhs);
   }

   /**
    * Divides this element by the given element.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable div(@NonNull Number rhs) {
      return div(SQL.N(rhs));
   }

   /**
    * Creates an equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable eq(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.EQUALS, this, rhs);
   }

   /**
    * Creates an equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable eq(@NonNull Number rhs) {
      return eq(SQL.N(rhs));
   }

   /**
    * Creates a full text search operator using this element as the column
    *
    * @param string the string to search for
    * @return the SQLOperable
    */
   default SQLOperable fullTextMatch(@NonNull String string) {
      return new SQLBinaryOperator(SQLOperator.FULL_TEXT_MATCH, this, SQL.L(string));
   }

   /**
    * Creates a greater than equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable gt(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.GREATER_THAN, this, rhs);
   }

   /**
    * Creates a greater than equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable gt(@NonNull Number rhs) {
      return gt(SQL.N(rhs));
   }

   /**
    * Creates a greater than equal to equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable gte(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.GREATER_THAN_EQUALS, this, rhs);
   }

   /**
    * Creates a greater than equal to equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable gte(@NonNull Number rhs) {
      return gte(SQL.N(rhs));
   }

   /**
    * Creates an IN operator treating this element as the item being checked and the given element as the list of items
    * being checked in.
    *
    * @param element the element representing the items we are checking for existence in
    * @return the SQLOperable
    */
   default SQLOperable in(@NonNull SQLElement element) {
      return new SQLBinaryOperator(SQLOperator.IN, this, element);
   }

   /**
    * Creates an is not null check for this element
    *
    * @return the SQLOperable
    */
   default SQLOperable isNotNull() {
      return new SQLBinaryOperator(SQLOperator.IS_NOT, this, SQL.nullValue());
   }

   /**
    * Creates a null check for this element
    *
    * @return the SQLOperable
    */
   default SQLOperable isNull() {
      return new SQLBinaryOperator(SQLOperator.IS, this, SQL.nullValue());
   }

   /**
    * Creates a LIKE operator with this element as the column or expression being checked .
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable like(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.LIKE, this, rhs);
   }

   /**
    * Creates a less than equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable lt(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.LESS_THAN, this, rhs);
   }

   /**
    * Creates a less than equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable lt(@NonNull Number rhs) {
      return lt(SQL.N(rhs));
   }

   /**
    * Creates a less than or equal to equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable lte(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.LESS_THAN_EQUALS, this, rhs);
   }

   /**
    * Creates a less than or equal to equality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable lte(@NonNull Number rhs) {
      return lte(SQL.N(rhs));
   }

   /**
    * Multiplies this element with the given element.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable mul(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.MULTIPLY, this, rhs);
   }

   /**
    * Multiplies this element with the given element.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable mul(@NonNull Number rhs) {
      return mul(SQL.N(rhs));
   }

   /**
    * Creates an inequality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable neq(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.NOT_EQUALS, this, rhs);
   }

   /**
    * Creates an inequality check between this element and the given element
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable neq(@NonNull Number rhs) {
      return neq(SQL.N(rhs));
   }

   /**
    * Negates this expression
    *
    * @return the SQLOperable
    */
   default SQLOperable not() {
      return new SQLPrefixUnaryOperator(SQLOperator.NOT, this, false);
   }

   /**
    * ORs this element with given element.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable or(@NonNull SQLElement rhs) {
      return SQL.or(this, rhs);
   }

   /**
    * Subtracts the given element from this element with.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable sub(@NonNull SQLElement rhs) {
      return new SQLBinaryOperator(SQLOperator.SUBTRACT, this, rhs);
   }

   /**
    * Subtracts the given element from this element with.
    *
    * @param rhs the right hand side of the binary operator
    * @return the SQLOperable
    */
   default SQLOperable sub(@NonNull Number rhs) {
      return sub(SQL.N(rhs));
   }

}//END OF SQLOperable
