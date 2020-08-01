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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * A token and its associated metadata extracted via a {@link Lexer}
 *
 * @author David B. Bracewell
 */
public class ParserToken implements Serializable {
   private static final long serialVersionUID = 1L;
   private final int end;
   private final int start;
   private final String text;
   private final Tag type;
   private final String[] variables;

   /**
    * Instantiates a new Parser token.
    *
    * @param type  the type
    * @param text  the text
    * @param start the start
    * @param end   the end
    */
   public ParserToken(Tag type, String text, int start, int end) {
      this(type, text, start, end, new String[0]);
   }

   /**
    * Instantiates a new Parser token.
    *
    * @param type  the type
    * @param text  the text
    * @param start the start
    */
   public ParserToken(Tag type, String text, int start) {
      this(type, text, start, start + text.length(), new String[0]);
   }

   /**
    * Instantiates a new Parser token.
    *
    * @param type      the type
    * @param text      the text
    * @param start     the start
    * @param end       the end
    * @param variables the variables
    */
   public ParserToken(Tag type, String text, int start, int end, String[] variables) {
      this.type = type;
      this.text = text;
      this.start = start;
      this.end = end;
      this.variables = variables;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ParserToken)) return false;
      ParserToken that = (ParserToken) o;
      return Objects.equals(type, that.type) &&
                Objects.equals(text, that.text) &&
                Arrays.equals(variables, that.variables) &&
                Objects.equals(start, that.start);
   }

   /**
    * Gets the ending character offset of the token in the stream.
    *
    * @return the ending character offset
    */
   public int getEndOffset() {
      return end;
   }

   /**
    * Gets the starting character offset of the token in the stream.
    *
    * @return the starting character offset
    */
   public int getStartOffset() {
      return start;
   }

   /**
    * Gets the extracted surface text of the token
    *
    * @return the surface text of the token
    */
   public String getText() {
      return text;
   }

   /**
    * Gets the token's associated type
    *
    * @return the token's type
    */
   public Tag getType() {
      return type;
   }

   /**
    * Gets a variable captured with the token.
    *
    * @param index the index of the variable
    * @return the variable value or null if the index is invalid or there are no variables
    */
   public String getVariable(int index) {
      if (variables == null || index < 0 || index >= variables.length) {
         return null;
      }
      return variables[index];
   }

   /**
    * Gets the number of variables associated with the token
    *
    * @return the number of variables
    */
   public int getVariableCount() {
      return variables.length;
   }

   /**
    * Gets the token variables as an array of String
    *
    * @return the string array of token variables
    */
   public String[] getVariables() {
      return variables;
   }

   @Override
   public int hashCode() {
      int result = Objects.hash(type, text, start, end);
      result = 31 * result + Arrays.hashCode(variables);
      return result;
   }

   /**
    * Checks if the token's tag is an instance of one of the given tags
    *
    * @param tags the tags to check
    * @return True if this token's tag is an instance of any of the given tags
    */
   public boolean isInstance(Tag... tags) {
      return type.isInstance(tags);
   }

   @Override
   public String toString() {
      return "ParserToken{" +
                "type=" + type +
                ", text='" + text + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", variables=" + Arrays.toString(variables) +
                '}';
   }
}//END OF ParserToken
