package com.gengoai.conversion;

import com.gengoai.io.CharsetDetectingReader;
import org.kohsuke.MetaInfServices;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Reader and BufferedReader Converter.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class ReaderTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof BufferedReader) {
         return source;
      }
      if (source instanceof Reader) {
         return new BufferedReader(Cast.as(source));
      }
      return new BufferedReader(new CharsetDetectingReader(Converter.convert(source, InputStream.class)));
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(Reader.class, BufferedReader.class);
   }
}//END OF FileTypeConverter
