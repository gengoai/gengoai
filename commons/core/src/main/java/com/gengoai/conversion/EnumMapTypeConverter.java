package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.getOrObject;
import static com.gengoai.reflection.TypeUtils.isAssignable;

/**
 * Converts objects into EnumMap
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class EnumMapTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object object, Type... parameters) throws TypeConversionException {
      Type enumClass = getOrObject(0, parameters);
      Type valueType = getOrObject(1, parameters);
      if (enumClass == null || !isAssignable(Enum.class, enumClass)) {
         throw new TypeConversionException("Invalid type parameter (" + enumClass + ")");
      }
      return new EnumMap<>(Converter.<Map<Enum, Object>>convert(object, Map.class, enumClass, valueType));
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(EnumMap.class);
   }
}//END OF EnumSetTypeConverter
