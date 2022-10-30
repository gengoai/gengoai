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

import com.gengoai.StringTag;
import com.gengoai.Tag;
import com.gengoai.string.Strings;

import java.io.Serializable;

/**
 * A stream of tokens extracted via a {@link Lexer} allowing for single token look-ahead. TokenStream implementations
 * should signal the end of stream using the <code>EOF_TOKEN</code> which has a special <code>EOF</code> tag.
 *
 * @author David B. Bracewell
 */
public interface TokenStream extends Serializable {
   /**
    * Special tag signaling the End-of-File (i.e. stream)
    */
   Tag EOF = new StringTag("~~~EOF~~~");
   /**
    * Special token signaling the End-of-File (i.e. stream)
    */
   ParserToken EOF_TOKEN = new ParserToken(EOF, Strings.EMPTY, -1, -1);

   /**
    * Consumes a token from the stream.
    *
    * @return the consumed token.
    */
   ParserToken consume();

   /**
    * Consume a token from the stream expecting the tag to be an instance of the given target type.
    *
    * @param target the target tag that the consumed should be an instance of
    * @return the consumed token
    */
   default ParserToken consume(Tag target) {
      ParserToken token = consume();
      if (!token.isInstance(target)) {
         throw new IllegalArgumentException(
            "Parsing Error: consumed token of type " + token.getType() + ", but was expecting " + target);
      }
      return token;
   }

   /**
    * Checks if there are more non-EOF tokens on the stream
    *
    * @return True if the next consumable token exists and is not EOF, False otherwise
    */
   default boolean hasNext() {
      return !peek().isInstance(EOF);
   }

   /**
    * Peeks at the next token on the stream.
    *
    * @return the next token on the stream (special EOF token if no more tokens exists)
    */
   ParserToken peek();

   /**
    * Returns the last token extracted via the call to consume.
    *
    * @return the last extracted token via consume
    */
   ParserToken token();

}//END OF TokenStream
