package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Converts object into Charsets. Conversion is possible for the following types:
 * <ul>
 * <li>Charset</li>
 * <li>Other: Objects are converted to strings and Charset#forName is used to parse the string.</li>
 * </ul>
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class CharsetTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof Charset) {
         return source;
      }
      try {
         return Charset.forName(Converter.convert(source, String.class));
      } catch (Exception e) {
         throw new TypeConversionException(source, Charset.class);
      }
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(Charset.class);
   }
}//END OF CharsetTypeConverter
