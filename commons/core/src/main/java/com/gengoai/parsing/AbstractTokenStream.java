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

import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base token stream providing extracted tokens from a {@link Lexer}.
 *
 * @author David B. Bracewell
 */
public abstract class AbstractTokenStream implements TokenStream {
   private static final long serialVersionUID = 1L;
   private ParserToken current;
   private LinkedList<ParserToken> buffer = new LinkedList<>();

   @Override
   public final ParserToken consume() {
      if (current != null && current.isInstance(EOF)) {
         throw new IllegalStateException("Parser Error: Attempting to read beyond EOF");
      }
      if (buffer.isEmpty()) {
         buffer.addAll(next());
      }
      current = buffer.removeFirst();
      return current;
   }

   @Override
   public final ParserToken peek() {
      if (current != null && current.isInstance(EOF)) {
         throw new IllegalStateException("Parser Error: Attempting to read beyond EOF");
      }
      if (buffer.isEmpty()) {
         buffer.addAll(next());
      }
      return buffer.peek();
   }

   @Override
   public final ParserToken token() {
      return current;
   }

   /**
    * Gets the next available token.
    *
    * @return the next {@link ParserToken}
    */
   protected abstract List<ParserToken> next();
}//END OF AbstractTokenStream
