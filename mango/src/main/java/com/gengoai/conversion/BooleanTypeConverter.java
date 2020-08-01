package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Converts object into Boolean values. Conversion is possible for the following types:
 * <ul>
 * <li>{@link JsonEntry} if the entry is a primitive</li>
 * <li>Booleans</li>
 * <li>Number: True if the integer value of the number is 0, False otherwise</li>
 * <li>Charsequence: using Boolean.parseBoolean</li>
 * </ul>
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class BooleanTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object object, Type... parameters) throws TypeConversionException {
      if(object instanceof JsonEntry) {
         JsonEntry entry = Cast.as(object);
         if(entry.isPrimitive()) {
            return convert(entry.get(), parameters);
         }
         throw new TypeConversionException(object, Boolean.class);
      }
      if(object instanceof Boolean) {
         return Cast.as(object);
      } else if(object instanceof Number) {
         return Cast.as(object, Number.class).intValue() == 1;
      } else if(object instanceof CharSequence) {
         return Boolean.parseBoolean(object.toString());
      }
      throw new TypeConversionException(object, Boolean.class);
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(Boolean.class, boolean.class);
   }
}//END OF BooleanTypeConverter
