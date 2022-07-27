package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * StringBuilder Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class StringBuilderTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object object, Type... parameters) throws TypeConversionException {
      if (object instanceof StringBuilder) {
         return object;
      }
      return new StringBuilder(Converter.convert(object, String.class));
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(StringBuilder.class);
   }
}//END OF StringBuilderTypeConverter
