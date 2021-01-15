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

import java.io.Serializable;

/**
 * Handler for postfix and infix expressions.
 *
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface PostfixHandler extends Serializable {

   /**
    * Handles the given postfix (or infix) token using the given parser
    *
    * @param parser the parser to use
    * @param token  the token representing the prefix operator
    * @param left   the expression on the left-hand side of the operator
    * @return the expression  resulting the handler
    * @throws ParseException Something went wrong parsing the expression.
    */
   Expression handle(Parser parser, ParserToken token, Expression left) throws ParseException;

}//END OF PostfixHandler
