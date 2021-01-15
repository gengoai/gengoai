package com.gengoai.conversion;

import com.gengoai.io.resource.Resource;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.net.URI;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * URI Type Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class UriTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      return Converter.convert(source, Resource.class).asURI()
                      .orElseThrow(() -> new TypeConversionException(source, URI.class));
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(URI.class);
   }
}//END OF FileTypeConverter
