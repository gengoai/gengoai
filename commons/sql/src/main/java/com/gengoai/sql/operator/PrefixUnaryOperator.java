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

import com.gengoai.sql.SQLElement;
import lombok.*;

/**
 * Unary Prefix {@link SQLOperator}
 */
@Value
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(callSuper = true)
public class PrefixUnaryOperator extends SQLOperator {
   private static final long serialVersionUID = 1L;
   @NonNull SQLElement arg1;
   boolean requiresParenthesis;

   /**
    * Instantiates a new SQLPrefixUnaryOperator.
    *
    * @param operator            the operator
    * @param arg1                the argument
    * @param requiresParenthesis are parenthesis required
    */
   public PrefixUnaryOperator(String operator, @NonNull SQLElement arg1, boolean requiresParenthesis) {
      super(operator);
      this.arg1 = arg1;
      this.requiresParenthesis = requiresParenthesis;
   }

   @Override
   public String toString() {
      return "SQLPrefixUnaryOperator{" +
            "operator='" + operator + '\'' +
            ", arg1=" + arg1 +
            '}';
   }
}//END OF PrefixUnaryOperator
