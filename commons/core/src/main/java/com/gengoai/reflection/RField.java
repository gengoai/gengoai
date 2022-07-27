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
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Wraps a Field allowing easy access to retrieving and setting its value.
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = true)
public class RField extends RAccessibleBase<Field, RField> {
   private static final long serialVersionUID = 1L;
   private final Field field;

   /**
    * Instantiates a new Reflected field.
    *
    * @param owner the owner
    * @param field the field
    */
   RField(Reflect owner, Field field) {
      super(owner);
      this.field = field;
      setIsPrivileged(owner.isPrivileged());
   }

   /**
    * Gets the value of this field.
    *
    * @return the value of this field.
    * @throws ReflectionException Something went wrong getting the value
    */
   public <T> T get() throws ReflectionException {
      return process(f -> Cast.as(f.get(getOwner().get())));
   }

   /**
    * Gets the class that declares this field as a {@link Reflect} object
    *
    * @return the declaring class as a {@link Reflect} object
    */
   public Reflect getDeclaringClass() {
      return Reflect.onClass(field.getDeclaringClass());
   }

   @Override
   public Field getElement() {
      return field;
   }

   @Override
   public int getModifiers() {
      return field.getModifiers();
   }

   @Override
   public String getName() {
      return field.getName();
   }

   /**
    * Gets the value of this field as a {@link Reflect} object.
    *
    * @return the value of this field as a {@link Reflect} object.
    * @throws ReflectionException Something went wrong getting the value
    */
   public Reflect getReflectValue() throws ReflectionException {
      return Reflect.onObject(get());
   }

   @Override
   public Type getType() {
      return field.getGenericType();
   }

   /**
    * Sets the value of this field. Will attempt to convert the value.
    *
    * @param value the value to set
    * @return This object
    * @throws ReflectionException Something went wrong setting or converting the value
    */
   public RField set(Object value) throws ReflectionException {
      with(f -> f.set(getOwner().get(), convertValueType(value, getType())));
      return this;
   }

   @Override
   public String toString() {
      if (getOwner().getType() != null) {
         return getOwner().getType().getSimpleName() + "::" + field.getName();
      }
      return field.getName();
   }

}//END OF RField
