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
import com.gengoai.conversion.Val;
import com.gengoai.function.SerializableFunction;

import java.util.Objects;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;

/**
 * Generic value expression in which the value is stored using a {@link Val} to support arbitrary types.
 *
 * @author David B. Bracewell
 */
public class ValueExpression extends BaseExpression {
   /**
    * Creates a boolean value handler which will parse the {@link ParserToken} text to generate the boolean value
    */
   public static final PrefixHandler BOOLEAN_HANDLER = (p, t) -> new ValueExpression(t.getType(),
                                                                                     parseBoolean(t.getText()));
   /**
    * Creates a null value handler which will always contain a null value
    */
   public static final PrefixHandler NULL_HANDLER = (p, t) -> new ValueExpression(t.getType(), null);
   /**
    * Creates a numeric value handler which will parse the {@link ParserToken} text using
    * <code>Double.parseDouble</code> to generate the numeric value
    */
   public static final PrefixHandler NUMERIC_HANDLER = (p, t) -> new ValueExpression(t.getType(),
                                                                                     parseDouble(t.getText()));
   /**
    * Creates a string value handler which will use the text of the {@link ParserToken} as the value
    */
   public static final PrefixHandler STRING_HANDLER = (p, t) -> new ValueExpression(t.getType(), t.getText());

   private static final long serialVersionUID = 1L;
   private final Val value;

   /**
    * Instantiates a new Value expression.
    *
    * @param type  the type
    * @param value the value
    */
   public ValueExpression(Tag type, Object value) {
      super(type);
      this.value = Val.of(value);
   }

   /**
    * Creates a generic handler which uses the given {@link SerializableFunction} to convert the {@link ParserToken} to
    * a value.
    *
    * @param converter the converter to convert the token into a value.
    * @return the prefix handler
    */
   public static PrefixHandler handler(final SerializableFunction<ParserToken, Object> converter) {
      return (p, t) -> new ValueExpression(t.getType(), converter.apply(t));
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {return true;}
      if (obj == null || getClass() != obj.getClass()) {return false;}
      final ValueExpression other = (ValueExpression) obj;
      return Objects.equals(this.value, other.value) && Objects.equals(this.getType(), other.getType());
   }

   /**
    * Gets the stored value
    *
    * @return the value
    */
   public Val getValue() {
      return value;
   }

   /**
    * Gets the value as the given type
    *
    * @param <T>    the type parameter
    * @param tClass the class information of the type to return
    * @return the value
    */
   public <T> T getValue(Class<T> tClass) {
      return value.as(tClass);
   }

   @Override
   public int hashCode() {
      return Objects.hash(value, getType());
   }

   @Override
   public String toString() {
      return value.get().toString();
   }

}//END OF ValueExpression
