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

import com.gengoai.Tag;
import com.gengoai.string.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type List expression.
 *
 * @author David B. Bracewell
 */
public class ListExpression extends BaseExpression implements Iterable<Expression> {
   private static final long serialVersionUID = 1L;
   private final List<Expression> expressions;
   private final String startOfList;
   private final String endOfList;
   private final String separator;

   /**
    * Instantiates a new List expression.
    *
    * @param type        the type
    * @param expressions the expressions
    */
   public ListExpression(Tag type, Collection<Expression> expressions, String separator, String startOfList, String endOfList) {
      super(type);
      this.expressions = new ArrayList<>(expressions);
      this.startOfList = startOfList;
      this.endOfList = endOfList;
      this.separator = separator;
   }

   /**
    * Handler prefix handler.
    *
    * @param startOfListTag the operator
    * @param endOfListTag   the end of list
    * @param separatorTag   the separator
    * @return the prefix handler
    */
   public static PrefixHandler handler(Tag startOfListTag,
                                       Tag endOfListTag,
                                       Tag separatorTag,
                                       String startOfListStr,
                                       String endOfListStr,
                                       String separatorStr
                                      ) {
      return (p, t) -> new ListExpression(startOfListTag,
                                          p.parseExpressionList(endOfListTag, separatorTag),
                                          separatorStr,
                                          startOfListStr,
                                          endOfListStr);
   }

   /**
    * Get expression.
    *
    * @param index the index
    * @return the expression
    */
   public Expression get(int index) {
      return expressions.get(index);
   }

   @Override
   public Iterator<Expression> iterator() {
      return expressions.iterator();
   }

   /**
    * Number of expressions int.
    *
    * @return the int
    */
   public int numberOfExpressions() {
      return expressions.size();
   }


   /**
    * Checks if there no expressions in the list
    *
    * @return True if the list of expressions is empty
    */
   public boolean isEmpty() {
      return expressions.isEmpty();
   }


   /**
    * Generates the list of elements casting them to the desired expression type
    *
    * @param <E>    the expression type parameter
    * @param eClass the expression class
    * @return the list of expression as the given type
    */
   public <E extends Expression> List<E> elementsAs(Class<E> eClass) {
      return expressions.stream().map(e -> e.as(eClass)).collect(Collectors.toList());
   }

   /**
    * Streams the elements.
    *
    * @return the stream of elements
    */
   public Stream<Expression> stream() {
      return expressions.stream();
   }

   /**
    * Streams the elements as the given type.
    *
    * @param <E>    the expression type parameter
    * @param eClass the expression class
    * @return the stream of elements as the given type
    */
   public <E extends Expression> Stream<Expression> stream(Class<E> eClass) {
      return expressions.stream().map(e -> e.as(eClass));
   }


   @Override
   public String toString() {
      return Strings.join(expressions, separator, startOfList, endOfList);
   }
}//END OF ListExpression
