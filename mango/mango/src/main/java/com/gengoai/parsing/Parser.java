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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>An implementation of a <a href="http://en.wikipedia.org/wiki/Pratt_parser">Pratt Parser</a> inspired by <a
 * href="http://journal.stuffwithstuff.com/2011/03/19/pratt-parsers-expression-parsing-made-easy/">Pratt Parsers:
 * Expression Parsing Made Easy</a>. An instance of a Parser is tied to a specific <code>Grammar</code> and
 * <code>TokenStream</code>. This implementation allows for operator precedence to be defined for each
 * <code>ParserHandler</code> and allows the precedence to be specified when retrieving the next expression.</p>
 *
 * @author David B. Bracewell
 */
public class Parser implements TokenStream {
   private static final long serialVersionUID = 1L;
   private final Grammar grammar;
   private final TokenStream tokenStream;

   /**
    * Instantiates a new Parser.
    *
    * @param grammar     the grammar to use for parsing
    * @param tokenStream the {@link TokenStream} to parse from
    */
   public Parser(Grammar grammar, TokenStream tokenStream) {
      this.grammar = grammar;
      this.tokenStream = tokenStream;
   }

   @Override
   public ParserToken consume() {
      return tokenStream.consume();
   }

   /**
    * <p>Parses the given resource and evaluates it with the given evaluator. Requires that the parse result in a
    * single expression.</p>
    *
    * @param <O>       the return type of the evaluator
    * @param evaluator the evaluator to use for transforming expressions
    * @return the single return values from the evaluator
    * @throws ParseException the parse exception
    */
   public <O> O evaluate(Evaluator<? extends O> evaluator) throws ParseException {
      try {
         return evaluator.eval(parseExpression());
      } catch (Exception e2) {
         throw new ParseException(e2);
      }
   }

   /**
    * <p>Parses the given string and evaluates it with the given evaluator.</p>
    *
    * @param <O>       the return type of the evaluator
    * @param evaluator the evaluator to use for transforming expressions
    * @return A list of objects relating to the transformation of expressions by the given evaluator.
    * @throws ParseException Something went wrong parsing
    */
   public <O> List<O> evaluateAll(Evaluator<? extends O> evaluator) throws ParseException {
      List<O> evaluationResults = new ArrayList<>();
      while (tokenStream.hasNext()) {
         try {
            evaluationResults.add(evaluator.eval(parseExpression()));
         } catch (ParseException e) {
            throw e;
         } catch (Exception e2) {
            throw new ParseException(e2);
         }
      }
      return evaluationResults;
   }

   /**
    * Parse all expressions list.
    *
    * @return the list
    * @throws ParseException the parse exception
    */
   public List<Expression> parseAllExpressions() throws ParseException {
      List<Expression> expressions = new ArrayList<>();
      while (tokenStream.hasNext()) {
         expressions.add(parseExpression());
      }
      return expressions;
   }

   /**
    * Parses the token stream to get the next expression
    *
    * @return the next expression in the parse
    * @throws ParseException Something went wrong parsing
    */
   public Expression parseExpression() throws ParseException {
      return parseExpression(0);
   }

   public <T extends Expression> T parseExpression(Class<T> tClass) throws ParseException {
      return parseExpression().as(tClass);
   }

   public <T extends Expression> T parseExpression(ParserToken precedence, Class<T> tClass) throws ParseException {
      return parseExpression(precedence).as(tClass);
   }

   public <T extends Expression> T parseExpression(int precedence, Class<T> tClass) throws ParseException {
      return parseExpression(precedence).as(tClass);
   }

   /**
    * Parses the token stream to get the next expression
    *
    * @param precedence Uses the associated precedence of the handler associated with the given token or 0 if no
    *                   precedence is defined.
    * @return the next expression in the parse
    * @throws ParseException Something went wrong parsing
    */
   public Expression parseExpression(ParserToken precedence) throws ParseException {
      return parseExpression(precedence, false);
   }

   /**
    * Parses the token stream to get the next expression
    *
    * @param token              Uses the associated precedence of the handler associated with the given token or 0 if no
    *                           precedence is defined.
    * @param isRightAssociative True - if this is a right associative rule
    * @return the next expression in the parse
    * @throws ParseException Something went wrong parsing
    */
   public Expression parseExpression(ParserToken token, boolean isRightAssociative) throws ParseException {
      return parseExpression(grammar.precedenceOf(token), isRightAssociative);
   }

   /**
    * Parses the token stream to get the next expression
    *
    * @param precedence The precedence of the next prefix expression
    * @return the next expression in the parse
    * @throws ParseException Something went wrong parsing
    */
   public Expression parseExpression(int precedence) throws ParseException {
      return parseExpression(precedence, false);
   }

   /**
    * Parses the token stream to get the next expression
    *
    * @param precedence         The precedence of the next prefix expression
    * @param isRightAssociative True - if this is a right associative rule
    * @return the next expression in the parse
    * @throws ParseException Something went wrong parsing
    */
   public Expression parseExpression(int precedence, boolean isRightAssociative) throws ParseException {
      if (isRightAssociative) {
         precedence--;
      }
      skip();
      ParserToken token = consume();
      Expression left = grammar.getPrefixHandler(token)
                               .orElseThrow(() -> new IllegalStateException("Parsing Error: Unable to parse '" +
                                                                                  token().getType() +
                                                                                  "', no prefix handler registered"))
                               .handle(this, token);

      grammar.validatePrefix(left);
      skip();
      while (precedence < grammar.precedenceOf(peek())) {
         token = consume();
         left = grammar.getPostfixHandler(token)
                       .orElseThrow(() -> new IllegalStateException("Parsing Error: Unable to parse '" +
                                                                          token().getType() +
                                                                          "', no postfix handler registered"))
                       .handle(this, token, left);
         grammar.validatePostfix(left);
         skip();
      }
      return left;
   }

   /**
    * Parses a list of tokens ending with the <code>endOfList</code> tag and values separated using the
    * <code>separator</code> tag.
    *
    * @param endOfList the {@link Tag} indicating the end of the list has been reached.
    * @param separator the {@link Tag} separating values of the list (null value means no separator).
    * @return the list of parsed expressions
    * @throws ParseException Something went wrong parsing the token stream
    */
   public <T extends Expression> List<T> parseExpressionList(Tag endOfList, Tag separator) throws ParseException {
      List<Expression> objExpressions = new ArrayList<>();
      boolean isFirst = true;
      while (!peek().isInstance(EOF, endOfList)) {
         if (!isFirst && separator != null) {
            consume(separator);
         }
         isFirst = false;
         objExpressions.add(parseExpression());
      }
      if (peek().isInstance(EOF)) {
         throw new ParseException("Parsing Error: Premature EOF");
      }
      consume(endOfList);
      return Cast.cast(objExpressions);
   }

   /**
    * Parses a list of tokens ending with the <code>endOfList</code> tag and values separated using the
    * <code>separator</code> tag.
    *
    * @param <T>         the type parameter
    * @param startOfList the start of list
    * @param endOfList   the {@link Tag} indicating the end of the list has been reached.
    * @param separator   the {@link Tag} separating values of the list (null value means no separator).
    * @return the list of parsed expressions
    * @throws ParseException Something went wrong parsing the token stream
    */
   public <T extends Expression> List<T> parseExpressionList(Tag startOfList,
                                                             Tag endOfList,
                                                             Tag separator) throws ParseException {
      List<Expression> objExpressions = new ArrayList<>();
      boolean isFirst = true;
      consume(startOfList);
      while (!peek().isInstance(EOF, endOfList)) {
         if (!isFirst && separator != null) {
            consume(separator);
         }
         isFirst = false;
         objExpressions.add(parseExpression());
      }
      if (peek().isInstance(EOF)) {
         throw new ParseException("Parsing Error: Premature EOF");
      }
      consume(endOfList);
      return Cast.cast(objExpressions);
   }

   @Override
   public ParserToken peek() {
      skip();
      return tokenStream.peek();
   }

   @Override
   public ParserToken token() {
      return tokenStream.token();
   }

   private void skip() {
      while (grammar.isIgnored(tokenStream.peek())) {
         tokenStream.consume();
      }
   }


}//END OF Parser
