package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import com.gengoai.string.Re;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Data Converter. Will try various common date formats based on the current Locale.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class JavaDateTypeConverter implements TypeConverter {
   private static final Pattern mm_dd_yyyy = Pattern.compile("\\d{1,2}-\\d{1,2}-\\d{4}");

   @Override
   public Object convert(Object object, Type... parameters) throws TypeConversionException {
      if (object instanceof Date) {
         return Cast.as(object);
      }

      if (object instanceof Number) {
         return new Date(Cast.as(object, Number.class).longValue());
      }

      if (object instanceof Calendar) {
         return Cast.as(object, Calendar.class).getTime();
      }

      if (object instanceof JsonEntry) {
         return convert(Cast.<JsonEntry>as(object).get());
      }

      String string = Converter.convert(object, String.class);
      if (string != null) {
         string = string.replaceAll(Re.MULTIPLE_WHITESPACE, " ").strip();

         if (mm_dd_yyyy.matcher(string).matches()) {
            try {
               return new SimpleDateFormat("MM-dd-yyyy").parse(string);
            } catch (ParseException e) {
               //no op
            }
         }

         for (DateFormat format : new DateFormat[]{
               new SimpleDateFormat("yyyy-MM-dd"),
               new SimpleDateFormat("MM/dd/yyyy"),
               SimpleDateFormat.getDateTimeInstance(),
               DateFormat.getDateInstance(DateFormat.SHORT),
               DateFormat.getDateInstance(DateFormat.MEDIUM),
               DateFormat.getDateInstance(DateFormat.LONG),
               DateFormat.getDateInstance(DateFormat.FULL)
         }
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
