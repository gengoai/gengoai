package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Base class for converter objects to numbers. The following types are supported.
 * <ul>
 * <li>{@link JsonEntry} can be converted if it is a primitive</li>
 * <li>Numbers</li>
 * <li>Booleans are converted to 1 or 0</li>
 * <li>Characters are converted to their integer values</li>
 * <li>Other objects are converted to Strings and the class attempts to parse the strings in various formats.</li>
 * </ul>
 *
 * @author David B. Bracewell
 */
public abstract class BaseNumberTypeConverter implements TypeConverter {

   @Override
   public final Object convert(Object object, Type... parameters) throws TypeConversionException {
      if(object instanceof JsonEntry) {
         JsonEntry entry = Cast.as(object);
         if(entry.isPrimitive()) {
            return convert(entry.get(), parameters);
         }
         throw new TypeConversionException(object, Number.class);
      } else if(object instanceof Number) {
         return convertNumber(Cast.as(object));
      } else if(object instanceof Boolean) {
         return convertNumber(Cast.as(object, Boolean.class)
                              ? 1L
                              : 0L);
      } else if(object instanceof Character) {
         return convertNumber(((long) Cast.as(object, Character.class)));
      }

      try {
         return convertNumber(Long.parseLong(object.toString()));
      } catch(Exception e) {
         //ignore this and try a double parse
      }

      try {
         return convertNumber(Double.parseDouble(object.toString()));
      } catch(Exception e) {
         //ignore this and try biginteger
      }

      try {
         return convertNumber(Long.parseLong(object.toString(),16));
      } catch(Exception e) {
         //ignore this and try a double parse
      }


      try {
         return convertNumber(new BigInteger(object.toString()));
      } catch(Exception e) {
         //try BigDecimal
      }

      try {
         return convertNumber(new BigDecimal(object.toString()));
      } catch(Exception e) {
         throw new TypeConversionException(object, Number.class, e);
      }



   }

   protected abstract Object convertNumber(Number number);
}//END OF BaseNumberTypeConverter
