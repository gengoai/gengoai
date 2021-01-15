package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.List;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.parameterizedType;

/**
 * Converts objects into EnumSets.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class EnumSetTypeConverter implements TypeConverter {

   @SuppressWarnings("unchecked")
   @Override
   public Object convert(Object object, Type... parameters) throws TypeConversionException {
      return EnumSet.copyOf(Converter.<List>convert(object, List.class, parameterizedType(Enum.class, parameters)));
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(EnumSet.class);
   }
}//END OF EnumSetTypeConverter
