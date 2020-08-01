package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Writer and BufferedWriter Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class WriterTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof BufferedWriter) {
         return source;
      }
      if (source instanceof Writer) {
         return new BufferedWriter(Cast.as(source));
      }
      return new BufferedWriter(new OutputStreamWriter(Converter.convert(source, OutputStream.class),
                                                       StandardCharsets.UTF_8));
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(Writer.class, BufferedWriter.class);
   }
}//END OF FileTypeConverter
