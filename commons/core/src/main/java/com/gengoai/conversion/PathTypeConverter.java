package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Path Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class PathTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof Path) {
         return source;
      }
      try {
         return Paths.get(Converter.convert(source, URI.class));
      } catch (FileSystemNotFoundException e) {
         throw new TypeConversionException(source, Path.class, e);
      }
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Path.class);
   }
}//END OF PathTypeConverter
