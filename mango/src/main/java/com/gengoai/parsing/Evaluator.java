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
import com.gengoai.conversion.Cast;
import com.gengoai.function.CheckedFunction;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <p>An evaluator provides a switch-like interface for evaluating expressions. Custom evaluators can be created as
 * follows:
 * <pre>
 * {@code
 *
 * Evaluator<Double> eval = new Evaluator<Double>() {{
 *      $(BinaryOperatorExpression.class, CommonTypes.PLUS, e -> eval(e.left) + eval(e.right));
 *      $(ValueExpression.class, e -> Double.valueOf(e.toString()));
 * }}
 *
 * }
 * </pre>
 * The various <code>$</code> methods allow easily adding if-like predicates and then-like functions. The {@link
 * #eval(Expression)} method can be used to make recursive evaluation calls.
 * </p>
 *
 * @param <O> the type parameter
 * @author David B. Bracewell
 */
public abstract class Evaluator<O> implements Serializable {
   private static final long serialVersionUID = 1L;
   private final ArrayList<EvalCase<?, O>> statements = new ArrayList<>();

   @Value
   private static class EvalCase<E extends Expression, V> implements Serializable {
      private static final long serialVersionUID = 1L;
      private final Class<E> expressionClass;
      private final Tag type;
      private final CheckedFunction<E, ? extends V> function;

      public EvalCase(Class<E> expressionClass, Tag type, CheckedFunction<E, ? extends V> function) {
         this.expressionClass = expressionClass;
         this.type = type;
         this.function = function;
      }
   }

   /**
    * Instantiates a new Evaluator.
    */
   protected Evaluator() {
//      $default(exp -> {throw new ParseException("Unknown Expression [" + exp + " : " + exp.getType() + "]");});
   }

   /**
    * Adds a switch statement where the condition is that the expression is of type <code>expressionClass</code> and the
    * expressions's token type is an instance of <code>type</code>. When the condition is met the expression is cast as
    * the given expression class and the given function is applied.
    *
    * @param <E>             the type of expression
    * @param expressionClass the expression class
    * @param type            the token type
    * @param function        the function to apply when the condition is met.
    */
   protected final <E extends Expression> void $(@NonNull Class<E> expressionClass,
                                                 @NonNull Tag type,
                                                 @NonNull CheckedFunction<E, ? extends O> function) {
      statements.add(new EvalCase<>(expressionClass, type, function));
   }

   /**
    * Adds a switch statement where the condition is that the expression is of type <code>expressionClass</code>. When
    * the condition is met the expression is cast as the given expression class and the given function is applied.
    *
    * @param <E>             the type of expression
    * @param expressionClass the expression class
    * @param function        the function to apply when the condition is met.
    */
   protected final <E extends Expression> void $(Class<E> expressionClass, CheckedFunction<E, ? extends O> function) {
      statements.add(new EvalCase<>(expressionClass, null, function));
   }

   /**
    * Evaluates the given expression
    *
    * @param expression the expression to evaluate
    * @return the result of evaluation
    * @throws ParseException Something went wrong during evaluation
    */
   public final O eval(Expression expression) throws ParseException {
      for (EvalCase<?, O> statement : statements) {
         if ((statement.type == null && expression.isInstance(statement.expressionClass))
            || expression.isInstance(statement.expressionClass, statement.type)) {
            try {
               return statement.function.apply(Cast.as(expression.as(statement.expressionClass)));
            } catch (Throwable throwable) {
               throw new ParseException(throwable);
            }
         }
      }
      throw new ParseException("Unknown Expression [" + expression + " : " + expression.getType() + "]");
   }

}// END OF Evaluator
