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

import com.gengoai.sql.SQLDialect;
import com.gengoai.sql.SQLElement;
import com.gengoai.sql.SQLFormattable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>Defines an SQL operator that operators over one or more {@link SQLElement}</p>
 */
@EqualsAndHashCode
@ToString
public abstract class SQLOperator implements SQLFormattable, SQLOperable, Serializable {
   private static final long serialVersionUID = 1L;
   public final static String ADD = "+";
   public final static String ASC = "ASC";
   public final static String BETWEEN = "BETWEEN";
   public final static String CONCAT = "||";
   public final static String DESC = "DESC";
   public final static String DIVIDE = "/";
   public final static String EQUALS = "=";
   public final static String EXCEPT = "EXCEPT";
   public final static String EXISTS = "EXISTS";
   public final static String FULL_TEXT_MATCH = "MATCH";
   public final static String GREATER_THAN = ">";
   public final static String GREATER_THAN_EQUALS = ">=";
   public final static String IN = "IN";
   public final static String INTERSECT = "INTERSECT";
   public final static String IS = "IS";
   public final static String IS_NOT = "IS NOT";
   public final static String LESS_THAN = "<";
   public final static String LESS_THAN_EQUALS = "<=";
   public final static String LIKE = "LIKE";
   public final static String MULTIPLY = "*";
   public final static String NOT = "!";
   public final static String NOT_EQUALS = "!=";
   public final static String SUBTRACT = "-";
   public final static String UNION = "UNION";
   public final static String UNION_ALL = "UNION ALL";
   protected final String operator;

   protected SQLOperator() {
      this(null);
   }

   protected SQLOperator(String operator) {
      this.operator = operator;
   }

   /**
    * Gets the string form of the operator (not the full expression)
    *
    * @return the operator
    */
   public final String getOperator() {
      return operator;
   }

   /**
    * Does this operator require parenthesis for its arguments.
    *
    * @return true if arguments must be placed in parenthesis
    */
   public boolean isRequiresParenthesis() {
      return false;
   }

   protected String toSQL(SQLDialect dialect, SQLElement arg) {
      StringBuilder builder = new StringBuilder(" ");
      if(isRequiresParenthesis()) {
         builder.append("( ");
      }
      builder.append(dialect.toSQL(arg));
      if(isRequiresParenthesis()) {
         builder.append(" )");
      }
      return builder.toString();
   }

}//END OF SQLOperator
