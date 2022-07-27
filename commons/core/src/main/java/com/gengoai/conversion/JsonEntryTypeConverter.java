package com.gengoai.conversion;

import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.lang.reflect.Type;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * JsonEntry converter. Will first try to parse CharSequences and if fails create JsonEntry string for them.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class JsonEntryTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof JsonEntry) {
         return source;
      }
      if (source instanceof CharSequence) {
         String str = source.toString();
         try {
            return Json.parse(str);
         } catch (IOException e) {
            return JsonEntry.from(str);
         }
      }
      return JsonEntry.from(source);
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Class[] getConversionType() {
      return arrayOf(JsonEntry.class);
   }
}//END OF JsonEntryTypeConverter
