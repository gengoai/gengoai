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

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An expression encapsulates the result of parsing
 *
 * @author David B. Bracewell
 */
public interface Expression extends Serializable {

   /**
    * Applies the given function to this Expression treating it as an Expression of type <code>T</code>.
    *
    * @param <T>      the expression type parameter
    * @param <O>      the output type parameter
    * @param tClass   the class information of desired Expression type
    * @param function the function to process the expression
    * @return the output of the function processing this expression
    * @throws IllegalStateException if this Expression is not of type <code>tClass</code>
    */
   default <T extends Expression, O> O apply(Class<T> tClass, Function<T, O> function) {
      return function.apply(as(tClass));
   }

   /**
    * Casts this Expression as the given Expression type
    *
    * @param <T>    the expression type parameter
    * @param tClass the class information of desired expression type
    * @return the Expression as the given type
    * @throws IllegalStateException if this Expression is not of type <code>tClass</code>
    */
   default <T extends Expression> T as(Class<T> tClass) {
      if (tClass.isInstance(this)) {
         return Cast.as(this);
      }
      throw new IllegalStateException("Parse Exception: Attempting to convert an expression of type '" +
                                         getClass().getSimpleName() +
                                         "' to an expression of type '" +
                                         tClass.getSimpleName() + " (" + toString() + ")");
   }

   /**
    * Gets the tag associated with the Expression
    *
    * @return the type
    */
   Tag getType();

   /**
    * Checks if this Expression is an instance of the given Expression type
    *
    * @param tClass the class information of desired expression type
    * @return True - if the expression is an instance of the given expression type, False otherwise
    */
   default boolean isInstance(Class<? extends Expression> tClass) {
      return tClass.isInstance(this);
   }

   /**
    * Checks if this Expression is an instance of the given Expression type and the given Tag type
    *
    * @param tClass the class information of desired expression type
    * @param type   the tag type
    * @return True - if the expression is an instance of the given expression type and tag type, False otherwise
    */
   default boolean isInstance(Class<? extends Expression> tClass, Tag type) {
      return tClass.isInstance(this) && this.getType().isInstance(type);
   }

   /**
    * Checks if this Expression's tag is an instance of any of the given tags
    *
    * @param tags the tags to check
    * @return True - if the expression of any of the given tag , False otherwise
    */
   default boolean isInstance(Tag... tags) {
      return getType().isInstance(tags);
   }

   /**
    * Applies the given function to the expression when it is an instance of the given expression type
    *
    * @param <T>      the expression type parameter
    * @param <O>      the output type parameter
    * @param tClass   the class information of desired Expression type
    * @param function the function to process the expression
    * @return the Optional output of the function.
    */
   default <T extends Expression, O> Optional<O> when(Class<T> tClass, Function<T, O> function) {
      if (isInstance(tClass)) {
         return Optional.ofNullable(function.apply(Cast.as(this)));
      }
      return Optional.empty();
   }

   /**
    * Applies the given consumer to the expression when it is an instance of the given expression type
    *
    * @param <T>      the expression type parameter
    * @param tClass   the class information of desired Expression type
    * @param consumer the consumer to process the expression
    * @return True if consumer is applied, False otherwise
    */
   default <T extends Expression> boolean when(Class<T> tClass, Consumer<T> consumer) {
      if (tClass.isInstance(this)) {
         consumer.accept(Cast.as(this));
         return true;
      }
      return false;
   }

}//END OF Expression
