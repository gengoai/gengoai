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

package com.gengoai.reflection;

import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import static com.gengoai.reflection.TypeUtils.isAssignable;

/**
 * Wraps a Parameter giving easy access to its type information.
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = false)
public class RParameter extends RBase<Parameter, RParameter> {
   private static final long serialVersionUID = 1L;
   private final RExecutable<?, ?> owner;
   private final Parameter parameter;


   RParameter(RExecutable<?, ?> owner, Parameter parameter) {
      this.owner = owner;
      this.parameter = parameter;
   }

   @Override
   public Parameter getElement() {
      return parameter;
   }

   @Override
   public int getModifiers() {
      return parameter.getModifiers();
   }

   @Override
   public String getName() {
      return parameter.isNamePresent() ? parameter.getName() : parameter.toString();
   }

   @Override
   public Type getType() {
      return parameter.getParameterizedType();
   }


   /**
    * Determines if the given type is compatible with the type of this parameter
    *
    * @param type the type to check
    * @return True if the given type is assignable to this parameter
    */
   public boolean isTypeCompatible(@NonNull Type type) {
      return isAssignable(getType(), type)
         || isAssignable(Val.class, getType())
         || isAssignable(Val.class, type);
   }

   /**
    * Gets the executable that this parameter belongs to
    *
    * @param <T> the executable type parameter
    * @return the Executable that this parameter belongs to
    */
   public <T extends RExecutable<?, T>> T owner() {
      return Cast.as(owner);
   }


   @Override
   public String toString() {
      return parameter.toString();
   }

}//END OF RParameter
