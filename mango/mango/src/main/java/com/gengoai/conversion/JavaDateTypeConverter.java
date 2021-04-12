package com.gengoai.conversion;

import com.gengoai.string.Re;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Data Converter. Will try various common date formats based on the current Locale.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class JavaDateTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object object, Type... parameters) throws TypeConversionException {
      if (object instanceof Date) {
         return Cast.as(object);
      } else if (object instanceof Number) {
         return new Date(Cast.as(object, Number.class).longValue());
      } else if (object instanceof Calendar) {
         return Cast.as(object, Calendar.class).getTime();
      }

      String string = Converter.convert(object, String.class);
      if (string != null) {
         string = string.replaceAll(Re.MULTIPLE_WHITESPACE, " ").strip();

         for (DateFormat format : new DateFormat[]{
            SimpleDateFormat.getDateTimeInstance(),
            DateFormat.getDateInstance(DateFormat.SHORT),
            DateFormat.getDateInstance(DateFormat.MEDIUM),
            DateFormat.getDateInstance(DateFormat.LONG),
            DateFormat.getDateInstance(DateFormat.FULL),
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("MM/dd/yyyy")}
         ) {
            try {
               return format.parse(string);
            } catch (ParseException e) {
               //no op
            }
         }

      }
      throw new TypeConversionException(object, Date.class);
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(Date.class);
   }
}//END OF JavaDateTypeConverter
