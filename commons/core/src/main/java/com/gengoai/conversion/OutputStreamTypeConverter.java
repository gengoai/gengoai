package com.gengoai.conversion;

import com.gengoai.io.resource.Resource;
import org.kohsuke.MetaInfServices;

import java.io.OutputStream;
import java.lang.reflect.Type;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * OutputStream Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class OutputStreamTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof OutputStream) {
         return source;
      }

      Resource resource = Converter.convert(source, Resource.class);

      if (source instanceof CharSequence && !source.toString().toLowerCase().startsWith("string:")) {
         throw new TypeConversionException(source, OutputStream.class);
      }

      try {
         return resource.outputStream();
      } catch (Exception e) {
         throw new TypeConversionException(source, OutputStream.class, e);
      }
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(OutputStream.class);
   }
}//END OF OutputStreamTypeConverter
