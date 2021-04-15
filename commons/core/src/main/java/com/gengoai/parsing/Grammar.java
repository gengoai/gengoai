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
import com.gengoai.function.SerializablePredicate;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;

/**
 * <p>A grammar representing the rules for parsing. Rules are defined using <code>ParserHandler</code>s, which are
 * associated with individual <code>Tag</code>s. There are two main types of handlers, prefix and postfix. The
 * <code>PrefixHandler</code> takes care of prefix operators and the <code>PostfixHandler</code> handles infix and
 * postfix operators.</p>
 *
 * <p>By default a grammar will throw a <code>ParseException</code> when it encounters a token type that it does not
 * know how to handle. Grammars can be set to instead ignore these tokens.</p>
 *
 * @author David B. Bracewell
 */
public class Grammar implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<Tag, PostfixHandler> postfixHandlerMap = new HashMap<>();
   private final Map<Tag, SerializablePredicate<? extends Expression>> postfixValidators = new HashMap<>();
   private final Map<Tag, Integer> precedenceMap = new HashMap<>();
   private final Map<Tag, PrefixHandler> prefixHandlerMap = new HashMap<>();
   private final Map<Tag, SerializablePredicate<? extends Expression>> prefixValidators = new HashMap<>();
   private final Set<Tag> skipTags = new HashSet<>();

   public Grammar() {

   }

   public Grammar(@NonNull GrammarRegistrable... registrables) {
      for (GrammarRegistrable registrable : registrables) {
         registrable.register(this);
      }
   }

   /**
    * Gets the postfix handler associated with the {@link Tag} of the given {@link ParserToken}
    *
    * @param token the token to use to determine the correct {@link PostfixHandler} to retrieve
    * @return An {@link Optional} containing the postfix handler if available or empty if none are available
    */
   public Optional<PostfixHandler> getPostfixHandler(ParserToken token) {
      return getPostfixHandler(token.getType());
   }

   /**
    * Gets the postfix handler associated with the given {@link Tag}.
    *
    * @param tag the tag representing the operator / value for which we want to retrieve a {@link PostfixHandler}
    * @return An {@link Optional} containing the postfix handler if available or empty if none are available
    */
   public Optional<PostfixHandler> getPostfixHandler(Tag tag) {
      return Optional.ofNullable(postfixHandlerMap.get(tag));
   }

   /**
    * Gets the prefix handler associated with the {@link Tag} of the given {@link ParserToken}
    *
    * @param token the token to use to determine the correct {@link PrefixHandler} to retrieve
    * @return An {@link Optional} containing the prefix handler if available or empty if none are available
    */
   public Optional<PrefixHandler> getPrefixHandler(ParserToken token) {
      return getPrefixHandler(token.getType());
   }

   /**
    * Gets the prefix handler associated with the given {@link Tag}.
    *
    * @param tag the tag representing the operator / value for which we want to retrieve a {@link PrefixHandler}
    * @return An {@link Optional} containing the postfix handler if available or empty if none are available
    */
   public Optional<PrefixHandler> getPrefixHandler(Tag tag) {
      return Optional.ofNullable(prefixHandlerMap.get(tag));
   }

   /**
    * Checks if the given token should be ignored during parsing
    *
    * @param token the token
    * @return True - if should be ignored, False otherwise
    */
   public boolean isIgnored(ParserToken token) {
      return isIgnored(token.getType());
   }

   /**
    * Checks if the given tag should be ignored during parsing
    *
    * @param tag the tag
    * @return True - if should be ignored, False otherwise
    */
   public boolean isIgnored(Tag tag) {
      return skipTags.contains(tag);
   }

   /**
    * Registers a {@link PostfixHandler} with a precedence of <code>1</code> for the given {@link Tag}
    *
    * @param tag     the tag for which the handler will be registered
    * @param handler the handler to associate with the given tag
    * @return the grammar
    */
   public Grammar postfix(Tag tag, PostfixHandler handler) {
      return postfix(tag, handler, 1);
   }

   /**
    * Registers a {@link PostfixHandler} with the given precedence for the given {@link Tag}
    *
    * @param tag        the tag for which the handler will be registered
    * @param handler    the handler to associate with the given tag
    * @param precedence the precedence of the tag (operator). Note that precedence will be the given value or 1 if the
    *                   given value is less than 1.
    * @return the grammar
    */
   public Grammar postfix(Tag tag, PostfixHandler handler, int precedence) {
      postfixHandlerMap.put(tag, Cast.as(handler));
      precedenceMap.put(tag, Math.max(precedence, 1));
      return this;
   }

   /**
    * Registers a {@link PostfixHandler} with the given precedence for the given {@link Tag}
    *
    * @param <E>        the type parameter
    * @param tag        the tag for which the handler will be registered
    * @param handler    the handler to associate with the given tag
    * @param precedence the precedence of the tag (operator). Note that precedence will be the given value or 1 if the
    *                   given value is less than 1.
    * @param validator  the validator to use validate the generated expression
    * @return the grammar
    */
   public <E extends Expression> Grammar postfix(Tag tag,
                                                 PostfixHandler handler,
                                                 int precedence,
                                                 SerializablePredicate<E> validator) {
      postfixHandlerMap.put(tag, Cast.as(handler));
      precedenceMap.put(tag, Math.max(precedence, 1));
      if (validator != null) {
         postfixValidators.put(tag, validator);
      }
      return this;
   }

   /**
    * Determines the precedence of the given tag
    *
    * @param tag the tag
    * @return the precedence
    */
   public int precedenceOf(Tag tag) {
      if (tag.isInstance(TokenStream.EOF)) {
         return Integer.MIN_VALUE;
      }
      return precedenceMap.getOrDefault(tag, 0);
   }

   /**
    * Determines the precedence of the {@link Tag} of the given {@link ParserToken}
    *
    * @param token the token for which the precedence will bec calculated.
    * @return the precedence
    */
   public int precedenceOf(ParserToken token) {
      if (token.equals(TokenStream.EOF_TOKEN)) {
         return Integer.MIN_VALUE;
      }
      return precedenceOf(token.getType());
   }

   /**
    * Registers a {@link PrefixHandler}  for the given {@link Tag}
    *
    * @param tag     the tag for which the handler will be registered
    * @param handler the handler to associate with the given tag
    * @return the grammar
    */
   public Grammar prefix(Tag tag, PrefixHandler handler) {
      prefixHandlerMap.put(tag, Cast.as(handler));
      return this;
   }

   /**
    * Registers a {@link PrefixHandler}  for the given {@link Tag}
    *
    * @param tag       the tag for which the handler will be registered
    * @param handler   the handler to associate with the given tag
    * @param validator the validator to use validate the generated expression
    * @return the grammar
    */
   public <E extends Expression> Grammar prefix(Tag tag, PrefixHandler handler, SerializablePredicate<E> validator) {
      prefixHandlerMap.put(tag, Cast.as(handler));
      if (validator != null) {
         prefixValidators.put(tag, validator);
      }
      return this;
   }

   /**
    * Registers the given {@link Tag} as one which should be skipped during parsing.
    *
    * @param tag the tag which will be skipped.
    * @return the grammar
    */
   public Grammar skip(Tag tag) {
      skipTags.add(tag);
      return this;
   }

   /**
    * Validates the given expression as if it were generated from a postfix handler.
    *
    * @param expression the expression to validate
    * @throws ParseException the exception if the expression is invalid
    */
   public void validatePostfix(Expression expression) throws ParseException {
      if (!postfixValidators.getOrDefault(expression.getType(), e -> true).test(Cast.as(expression))) {
         throw new ParseException("Parse Exception: Invalid Expression -> " + expression);
      }
   }

   /**
    * Validates the given expression as if it were generated from a prefix handler.
    *
    * @param expression the expression to validate
    * @throws ParseException the exception if the expression is invalid
    */
   public void validatePrefix(Expression expression) throws ParseException {
      if (!prefixValidators.getOrDefault(expression.getType(), e -> true).test(Cast.as(expression))) {
         throw new ParseException("Parse Exception: Invalid Expression -> " + expression);
      }
   }


}//END OF Grammar
