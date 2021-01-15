package com.gengoai.reflection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.Primitives;
import com.gengoai.conversion.Cast;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * <p>Convenience methods for creating type information</p>
 *
 * @author David B. Bracewell
 */
public final class TypeUtils {

   /**
    * Converts type information to class information
    *
    * @param <T>  the type parameter
    * @param type the type
    * @return the class
    */
   public static <T> Class<T> asClass(Type type) {
      if(type instanceof Class) {
         return Cast.as(type);
      }
      if(type instanceof ParameterizedType) {
         return asClass(Cast.<ParameterizedType>as(type).getRawType());
      }
      if(type instanceof GenericArrayType) {
         Class<?> componenet = asClass(Cast.<GenericArrayType>as(type).getGenericComponentType());
         return Cast.as(Array.newInstance(componenet, 1).getClass());
      }
      throw new IllegalArgumentException("Unable to handle type (" + type.getClass() + "): " + type.getTypeName());
   }

   /**
    * Get actual type arguments type [ ].
    *
    * @param type the type
    * @return the type [ ]
    */
   public static Type[] getActualTypeArguments(Type type) {
      if(type instanceof ParameterizedType) {
         return Cast.<ParameterizedType>as(type).getActualTypeArguments();
      }
      return null;
   }

   /**
    * Gets or object.
    *
    * @param n     the n
    * @param types the types
    * @return the or object
    */
   public static Type getOrObject(int n, Type... types) {
      return types == null || types.length <= n
             ? Object.class
             : types[n];
   }

   /**
    * Is array boolean.
    *
    * @param type the type
    * @return the boolean
    */
   public static boolean isArray(Type type) {
      return asClass(type).isArray();
   }

   /**
    * Is assignable boolean.
    *
    * @param parent the t 1
    * @param child  the to check
    * @return the boolean
    */
   public static boolean isAssignable(Type parent, Type child) {
      Class<?> c1 = Primitives.wrap(asClass(parent));
      Class<?> c2 = Primitives.wrap(asClass(child));
      return c1.isAssignableFrom(c2);
   }

   /**
    * Is collection boolean.
    *
    * @param type the type
    * @return the boolean
    */
   public static boolean isCollection(Type type) {
      return Collection.class.isAssignableFrom(asClass(type));
   }

   /**
    * Is container boolean.
    *
    * @param type the type
    * @return the boolean
    */
   public static boolean isContainer(Type type) {
      if(type == null) {
         return false;
      }
      Class<?> clazz = asClass(type);
      return Iterable.class.isAssignableFrom(clazz) ||
            Iterator.class.isAssignableFrom(clazz) ||
            clazz.isArray();
   }

   /**
    * Is iterable boolean.
    *
    * @param type the type
    * @return the boolean
    */
   public static boolean isIterable(Type type) {
      return Iterable.class.isAssignableFrom(asClass(type));
   }

   /**
    * Is iterator boolean.
    *
    * @param type the type
    * @return the boolean
    */
   public static boolean isIterator(Type type) {
      return Iterator.class.isAssignableFrom(asClass(type));
   }

   /**
    * Is primitive boolean.
    *
    * @param type the type
    * @return the boolean
    */
   public static boolean isPrimitive(Type type) {
      Class<?> c = asClass(type);
      return c.isPrimitive() || (c.isArray() && c.getComponentType().isPrimitive());
   }

   /**
    * Creates parameterized type information for the given raw type and optional type arguments.
    *
    * @param rawType       the raw type
    * @param typeArguments the type arguments
    * @return the parameterized type
    */
   public static Type parameterizedType(Type rawType, Type... typeArguments) {
      return new ParameterizedTypeImpl(rawType, typeArguments, null);
   }

   /**
    * From string type.
    *
    * @param s the s
    * @return the type
    */
   public static Type parse(String s) {
      int tStart = s.indexOf('<');
      int tEnd = s.lastIndexOf('>');
      int rawEnd = tStart > 0
                   ? tStart
                   : s.length();

      if((tStart == -1 && tEnd != -1) || (tStart != -1 && tEnd != s.length() - 1)) {
         throw new RuntimeException("Invalid Parameterized Type Declaration: " + s);
      }
      Type rawType = null;
      try {
         rawType = Reflect.getClassForName(s.substring(0, rawEnd));
      } catch(Exception e) {
         throw new RuntimeException(e);
      }
      Type[] pTypes = null;
      if(tStart > 0) {
         pTypes = Arrays.stream(s.substring(tStart + 1, tEnd).split("[, ]+"))
                        .map(TypeUtils::parse)
                        .toArray(Type[]::new);
      }
      return pTypes == null
             ? rawType
             : parameterizedType(rawType, pTypes);
   }

   private TypeUtils() {
      throw new IllegalAccessError();
   }

   @JsonAutoDetect(
         fieldVisibility = JsonAutoDetect.Visibility.NONE,
         setterVisibility = JsonAutoDetect.Visibility.NONE,
         getterVisibility = JsonAutoDetect.Visibility.NONE,
         isGetterVisibility = JsonAutoDetect.Visibility.NONE,
         creatorVisibility = JsonAutoDetect.Visibility.NONE
   )
   public static class ParameterizedTypeImpl implements ParameterizedType, Serializable {
      private static final long serialVersionUID = 1L;
      private final Type[] actualTypeArguments;
      private final Type ownerType;
      private final Type rawType;

      @JsonCreator
      private ParameterizedTypeImpl(@JsonProperty("rawType") Type rawType,
                                    @JsonProperty("parameters") Type[] actualTypeArguments,
                                    @JsonProperty("ownerType") Type ownerType) {
         this.rawType = rawType;
         this.actualTypeArguments = actualTypeArguments;
         this.ownerType = ownerType;
      }

      @Override
      public boolean equals(Object obj) {
         if(this == obj) {
            return true;
         }
         if(obj instanceof ParameterizedType) {
            ParameterizedType ptO = Cast.as(obj);
            return Objects.equals(rawType, ptO.getRawType())
                  && Objects.equals(ownerType, ptO.getOwnerType())
                  && Objects.deepEquals(actualTypeArguments, ptO.getActualTypeArguments());
         }
         return false;
      }

      @Override
      @JsonProperty("parameters")
      public Type[] getActualTypeArguments() {
         return actualTypeArguments;
      }

      @Override
      @JsonProperty("ownerType")
      public Type getOwnerType() {
         return ownerType;
      }

      @Override
      @JsonProperty("rawType")
      public Type getRawType() {
         return rawType;
      }

      @Override
      public int hashCode() {
         return Objects.hash(actualTypeArguments, ownerType, rawType);
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder(rawType.getTypeName());
         if(actualTypeArguments != null) {
            sb.append("<");
            sb.append(actualTypeArguments[0].getTypeName());
            for(int i = 1; i < actualTypeArguments.length; i++) {
               sb.append(", ").append(actualTypeArguments[i].getTypeName());
            }
            sb.append(">");
         }
         return sb.toString();
      }
   }

}//END OF Types
