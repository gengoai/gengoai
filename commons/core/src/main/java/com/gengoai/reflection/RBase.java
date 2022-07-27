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

import com.gengoai.Primitives;
import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.conversion.Val;
import com.gengoai.function.CheckedBiConsumer;
import com.gengoai.function.CheckedBiFunction;
import com.gengoai.function.CheckedConsumer;
import com.gengoai.function.CheckedFunction;
import lombok.NonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Base-class for fluent Reflection classes
 *
 * @param <T> the element type parameter
 * @param <V> this type parameter
 * @author David B. Bracewell
 */
abstract class RBase<T extends AnnotatedElement, V extends RBase> implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Convert value type object.
    *
    * @param value   the value
    * @param toClass the to class
    * @return the object
    * @throws TypeConversionException the type conversion exception
    */
   static Object convertValueType(Object value, Type toClass) throws TypeConversionException {
      if (value == null) {
         return Primitives.defaultValue(TypeUtils.asClass(toClass));
      }
      if (TypeUtils.isAssignable(Val.class, toClass)) {
         return Validation.notNull(Cast.as(value, Val.class)).as(toClass);
      }
      if (!TypeUtils.isCollection(toClass) && TypeUtils.isAssignable(toClass, value.getClass())) {
         return value;
      }
      Object out = Converter.convert(value, toClass);
      return out == null ? value : out;
   }

   /**
    * Gets the types for an array of objects
    *
    * @param args The arguments to get types for
    * @return A 0 sized array if the array is null, otherwise an array of class equalling the classes of the given args
    * or <code>Object.cass</code> if the arg is null.
    */
   static Class[] getTypes(Object... args) {
      if (args.length == 0) {
         return new Class[0];
      }
      Class[] types = new Class[args.length];
      for (int i = 0; i < args.length; i++) {
         types[i] = args[i] == null ? Object.class : args[i].getClass();
      }
      return types;
   }


   /**
    * Determines if any of the given annotations are present on this object
    *
    * @param element           the annotated element to check
    * @param annotationClasses the annotation classes
    * @return True - if any of the given annotations are present on this object
    */
   @SuppressWarnings("unchecked")
   @SafeVarargs
   static boolean isAnnotationPresent(@NonNull AnnotatedElement element,
                                      @NonNull Class<? extends Annotation>... annotationClasses) {
      for (Class aClass : annotationClasses) {
         if (element.isAnnotationPresent(aClass)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Gets the annotation of the given class present on this object.
    *
    * @param <A>             the annotation type parameter
    * @param annotationClass the annotation class
    * @return the annotation (null if does not exist)
    */
   public final <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
      return getAnnotation(annotationClass, false);
   }

   /**
    * Gets the annotation of the given class on this object. When <code>onlyDirect</code> is true it will only return
    * annotations directly present via <code>getDeclaredAnnotation</code> otherwise will return any instance present
    * via
    * <code>getAnnotation</code>.
    *
    * @param <A>             the annotation type parameter
    * @param annotationClass the annotation class
    * @param onlyDirect      True - only declared annotations, False present annotations
    * @return the annotation (null if does not exist)
    */
   public final <A extends Annotation> A getAnnotation(@NonNull Class<A> annotationClass, boolean onlyDirect) {
      if (onlyDirect) {
         return getElement().getDeclaredAnnotation(annotationClass);
      }
      return getElement().getAnnotation(annotationClass);
   }

   /**
    * Gets all annotations on this object. When <code>onlyDirect</code> is true it will only return annotations directly
    * present via <code>getDeclaredAnnotations</code> otherwise will return any instance present via
    * <code>getAnnotations</code>.
    *
    * @param onlyDirect True - only declared annotations, False present annotations
    * @return the array of Annotation
    */
   public final Annotation[] getAnnotations(boolean onlyDirect) {
      if (onlyDirect) {
         return getElement().getDeclaredAnnotations();
      }
      return getElement().getAnnotations();
   }

   /**
    * Gets all annotations present on this object.
    *
    * @return the array of Annotation
    */
   public final Annotation[] getAnnotations() {
      return getAnnotations(false);
   }

   /**
    * Gets all annotations on this object. When <code>onlyDirect</code> is true it will only return annotations directly
    * present via <code>getDeclaredAnnotationsByType</code> otherwise will return any instance present via
    * <code>getAnnotationsByType</code>.
    *
    * @param <A>             the annotation type parameter
    * @param annotationClass the annotation class
    * @param onlyDirect      True - only declared annotations, False present annotations
    * @return the array of Annotation
    */
   public final <A extends Annotation> A[] getAnnotations(@NonNull Class<A> annotationClass, boolean onlyDirect) {
      if (onlyDirect) {
         return getElement().getDeclaredAnnotationsByType(annotationClass);
      }
      return getElement().getAnnotationsByType(annotationClass);
   }

   /**
    * Gets all associated annotations of given type on this object.
    *
    * @param <A>             the annotation type parameter
    * @param annotationClass the annotation class
    * @return the array of Annotation
    */
   public final <A extends Annotation> A[] getAnnotations(@NonNull Class<A> annotationClass) {
      return getAnnotations(annotationClass, false);
   }

   /**
    * Gets the AnnotatedElement
    *
    * @return the AnnotatedElement object
    */
   public abstract T getElement();

   /**
    * Gets the modifiers on this field.
    *
    * @return the modifiers
    */
   public abstract int getModifiers();

   /**
    * Gets the name of the element
    *
    * @return the name of the element
    */
   public abstract String getName();

   /**
    * Gets the type of the element
    *
    * @return the type of the element
    */
   public abstract Type getType();

   /**
    * Determines if any of the given annotations are declared on this object
    *
    * @param annotationClasses the annotation classes
    * @return True - if any of the given annotations are declared on this object
    */
   @SuppressWarnings("unchecked")
   @SafeVarargs
   public final boolean isAnnotationDeclared(@NonNull Class<? extends Annotation>... annotationClasses) {
      for (Class aClass : annotationClasses) {
         if (getElement().getDeclaredAnnotation(aClass) != null) {
            return true;
         }
      }
      return false;
   }

   /**
    * Determines if any of the given annotations are present on this object
    *
    * @param annotationClasses the annotation classes
    * @return True - if any of the given annotations are present on this object
    */
   @SafeVarargs
   public final boolean isAnnotationPresent(@NonNull Class<? extends Annotation>... annotationClasses) {
      return Stream.of(annotationClasses).anyMatch(a -> getElement().isAnnotationPresent(a));
   }

   private <A extends Annotation, O> O processAnnotation(A annotation,
                                                         CheckedFunction<? super A, ? extends O> function)
      throws ReflectionException {
      try {
         if (annotation != null) {
            return function.apply(Cast.as(annotation));
         }
         return null;
      } catch (Throwable e) {
         throw new ReflectionException(e);
      }
   }

   /**
    * Applies the given {@link CheckedBiFunction} to this object with the given annotation if it is present on the
    * object.
    *
    * @param <A>             the annotation type parameter
    * @param <O>             the return type parameter
    * @param annotationClass the annotation class
    * @param function        the function to apply
    * @return the return value of the function or null if no annotation was present
    * @throws ReflectionException Something went wrong during reflection
    */
   public final <A extends Annotation, O> O processAnnotation(@NonNull Class<A> annotationClass,
                                                              @NonNull CheckedFunction<? super A, ? extends O> function)
      throws ReflectionException {
      return processAnnotation(getAnnotation(annotationClass), function);
   }

   /**
    * Applies the given {@link CheckedBiFunction} to this object and all instances of the given annotation present on
    * the object.
    *
    * @param <A>             the annotation type parameter
    * @param <O>             the return type parameter
    * @param annotationClass the annotation class
    * @param function        the function to apply
    * @return A list of the return values of the function or null if no annotation was present
    * @throws ReflectionException Something went wrong during reflection
    */
   public final <A extends Annotation, O> List<O> processAnnotations(@NonNull Class<A> annotationClass,
                                                                     @NonNull CheckedFunction<? super A, ? extends O> function)
      throws ReflectionException {
      List<O> list = new ArrayList<>();
      for (A annotation : getAnnotations(annotationClass)) {
         list.add(processAnnotation(annotation, function));
      }
      return list;
   }

   @Override
   public String toString() {
      return getElement().toString();
   }

   private <A extends Annotation> void withAnnotation(A annotation,
                                                      CheckedConsumer<? super A> consumer) throws ReflectionException {
      try {
         if (annotation != null) {
            consumer.accept(Cast.as(annotation));
         }
      } catch (Throwable e) {
         throw new ReflectionException(e);
      }
   }

   /**
    * Applies the given {@link CheckedBiConsumer} to this object with the given annotation if it is present on the
    * object.
    *
    * @param <A>             the annotation type parameter
    * @param annotationClass the annotation class
    * @param consumer        the consumer to apply
    * @return This object
    * @throws ReflectionException Something went wrong during reflection
    */
   public final <A extends Annotation> V withAnnotation(@NonNull Class<A> annotationClass,
                                                        @NonNull CheckedConsumer<? super A> consumer)
      throws ReflectionException {
      withAnnotation(getAnnotation(annotationClass), consumer);
      return Cast.as(this);
   }

   /**
    * Applies the given {@link CheckedBiConsumer} to this object and all instances of the given annotation present on
    * this object.
    *
    * @param <A>             the annotation type parameter
    * @param annotationClass the annotation class
    * @param consumer        the consumer to apply
    * @return This object
    * @throws ReflectionException Something went wrong during reflection
    */
   public final <A extends Annotation> V withAnnotations(@NonNull Class<A> annotationClass,
                                                         @NonNull CheckedConsumer<? super A> consumer)
      throws ReflectionException {
      for (A annotation : getAnnotations(annotationClass)) {
         withAnnotation(annotation, consumer);
      }
      return Cast.as(this);
   }
}//END OF RBase
