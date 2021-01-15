package com.gengoai.conversion;

import java.lang.reflect.Type;

/**
 * Defines an interface for converting one type to another. Implementations are loaded at runtime using the Java {@link
 * java.util.ServiceLoader}. A runtime exception will be thrown if multiple classes are trying to convert the same
 * type.
 *
 * @author David B. Bracewell
 */
public interface TypeConverter {

   /**
    * Converts an object from one type to another.
    *
    * @param source     the source object
    * @param parameters the optional type parameters when using Generics.
    * @return the converted object
    * @throws TypeConversionException the source object was not able to be converted to the target type
    */
   Object convert(Object source, Type... parameters) throws TypeConversionException;

   /**
    * Array of classes that this type converter can convert objects to
    *
    * @return the classes that this converter can convert to
    */
   Class[] getConversionType();


}//END OF TypeConverter
