package com.gengoai.conversion;

import com.gengoai.io.resource.Resource;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.net.URL;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * URL Type Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class UrlTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      return Converter.convert(source, Resource.class).asURL()
                      .orElseThrow(() -> new TypeConversionException(source, URL.class));
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(URL.class);
   }
}//END OF FileTypeConverter
