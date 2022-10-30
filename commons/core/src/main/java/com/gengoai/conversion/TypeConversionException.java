package com.gengoai.conversion;

import java.lang.reflect.Type;

/**
 * Exception throw when unable to converter from a source to destination type.
 *
 * @author David B. Bracewell
 */
public class TypeConversionException extends Exception {

   /**
    * Instantiates a new Type conversion exception.
    *
    * @param source   the source object (thing being converted)
    * @param destType the destination type (the type being converted to)
    */
   public TypeConversionException(Object source, Type destType) {
      this(source, destType, null);
   }

   /**
    * Instantiates a new Type conversion exception.
    *
    * @param message the error message
    */
   public TypeConversionException(String message) {
      super(message);
   }

   /**
    * Instantiates a new Type conversion exception.
    *
    * @param source   the source object (thing being converted)
    * @param destType the destination type (the type being converted to)
    * @param cause    the exception causing the failed conversion
    */
   public TypeConversionException(Object source, Type destType, Throwable cause) {
      super(
         "Cannot convert object (" + source + ") of type (" +
            (source == null ? "null" : source.getClass()) +
            ") to object of type (" + destType + ")",
         cause);
   }


}//END OF TypeConversionException
