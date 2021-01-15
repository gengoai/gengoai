package com.gengoai.conversion;

import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.sql.Date;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * SQL Date Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class SqlDateTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object object, Type... parameters) throws TypeConversionException {
      if (object instanceof Date) {
         return Cast.as(object);
      }

      return new Date(Converter.convert(object, java.util.Date.class).getTime());
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(Date.class);
   }
}//END OF SqlDateTypeConverter
