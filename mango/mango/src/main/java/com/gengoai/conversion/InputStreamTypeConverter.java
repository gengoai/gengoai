package com.gengoai.conversion;

import com.gengoai.io.resource.Resource;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * InputStream Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class InputStreamTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof InputStream) {
         return source;
      }

      try {
         return Converter.convert(source, Resource.class).inputStream();
      } catch (IOException | RuntimeException e) {
         throw new TypeConversionException(source, InputStream.class, e);
      }
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(InputStream.class);
   }
}//END OF FileTypeConverter
