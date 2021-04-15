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

import com.gengoai.Lazy;
import com.gengoai.collection.Lists;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.gengoai.reflection.TypeUtils.isAssignable;

/**
 * Base class for Executable objects (Methods and Constructors)
 *
 * @param <T> the Executable type parameter
 * @param <V> this type parameter
 * @author David B. Bracewell
 */
public abstract class RExecutable<T extends Executable, V extends RExecutable> extends RAccessibleBase<T, V> {
   private static final long serialVersionUID = 1L;
   private final Lazy<List<RParameter>> parameters = new Lazy<>(() -> new ArrayList<>(
      Lists.transform(Arrays.asList(getElement().getParameters()), parameter -> new RParameter(this, parameter))));

   /**
    * Instantiates a new R executable.
    *
    * @param owner the owning Reflect
    */
   protected RExecutable(Reflect owner) {
      super(owner);
   }


   /**
    * Converts the given set of arguments to values acceptable by this executable
    *
    * @param args the arguments to convert
    * @return the converted arguments
    * @throws TypeConversionException Something went wrong converting the arguments.
    */
   protected final Object[] convertParameters(Object... args) throws TypeConversionException {
      Class[] types = getTypes(args);
      Type[] pTypes = getElement().getGenericParameterTypes();
      Object[] eArgs = new Object[args.length];
      for (int i = 0; i < args.length; i++) {
         if (args[i] == null || isAssignable(pTypes[i], types[i])) {
            eArgs[i] = args[i];
         } else {
            eArgs[i] = Converter.convert(args[i], pTypes[i]);
         }
      }
      return eArgs;
   }

   /**
    * Gets the class that declares this executable as a {@link Reflect} object
    *
    * @return the declaring class as a {@link Reflect} object
    */
   public Reflect getDeclaringClass() {
      return Reflect.onClass(getElement().getDeclaringClass());
   }

   @Override
   public int getModifiers() {
      return getElement().getModifiers();
   }

   @Override
   public final String getName() {
      return getElement().getName();
   }


   /**
    * Gets the ith parameter of the executable
    *
    * @param i the index of the parameter in the executable's parameter list
    * @return the parameter
    */
   public RParameter getParameter(int i) {
      return parameters.get().get(i);
   }

   /**
    * Gets all parameters of this executable.
    *
    * @return the parameters
    */
   public List<RParameter> getParameters() {
      return Collections.unmodifiableList(parameters.get());
   }

   /**
    * Checks if this executable has a varargs parameter
    *
    * @return True if the executable takes a vararg parameter, False othrwise
    */
   public boolean isVarArgs() {
      return getElement().isVarArgs();
   }

   /**
    * Gets the number of parameters on the executable
    *
    * @return the number of parameters on the executable
    */
   public int getParameterCount() {
      return getElement().getParameterCount();
   }

   /**
    * Determines if the given types are compatible with the this executable's parameters
    *
    * @param types the types to check
    * @return True if the given types are compatible
    */
   public boolean parameterTypesCompatible(@NonNull Type... types) {
      return types.length == getParameterCount()
         && Streams.zip(parameters.get().stream(), Stream.of(types))
                   .allMatch(e -> e.getKey().isTypeCompatible(e.getValue()));
   }


}//END OF ReflectedExecutable
