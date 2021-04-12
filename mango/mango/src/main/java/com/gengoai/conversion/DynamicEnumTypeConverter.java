package com.gengoai.conversion;

import com.gengoai.EnumValue;
import com.gengoai.HierarchicalEnumValue;
import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.*;

/**
 * Converts objects in {@link EnumValue} and {@link HierarchicalEnumValue}s. JsonEntry, CharSequence, and EnumValues are
 * supported. All are converted to String values and the string parsed to determine the correct class and enum value.
 * Enum values will be created if needed.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class DynamicEnumTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if(source instanceof JsonEntry
            || source instanceof CharSequence
            || source instanceof EnumValue) {
         String asString = Converter.convert(source, String.class);
         String enumName = asString;
         Class<?> enumClass = null;

         //We can either get the EnumValue class as part of the name or as a type parameter

         //Check in the name
         int lastDot = asString.lastIndexOf('.');
         if(lastDot > 0) {
            enumClass = Reflect.getClassForNameQuietly(asString.substring(0, lastDot));
            enumName = asString.substring(lastDot + 1);
         }


         if(enumClass == null) {
            //Didn't find it in the name, so check in the parameters
            enumClass = asClass(getOrObject(0, parameters));
         }
         else if(parameters != null && parameters.length > 0 && !isAssignable(parameters[0], enumClass)) {
            //make sure we didn't specify a parameter that is invalid.
            throw new TypeConversionException(
                  "Invalid type parameter (" + enumClass + ") is not of type (" + parameters[0] + ")");
         }

         if(enumClass == null || !EnumValue.class.isAssignableFrom(enumClass)) {
            throw new TypeConversionException("Invalid type parameter (" + enumClass + ")");
         }

         try {

            return Reflect.onClass(enumClass)
                          .allowPrivilegedAccess()
                          .getMethod("make", String.class)
                          .invoke(enumName);
         } catch(ReflectionException e) {
            throw new TypeConversionException(source, parameterizedType(EnumValue.class, enumClass), e);
         }
      }
      throw new TypeConversionException(source, parameterizedType(EnumValue.class, parameters));
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(EnumValue.class, HierarchicalEnumValue.class);
   }
}//END OF EnumTypeConverter
