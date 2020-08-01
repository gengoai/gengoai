package com.gengoai.conversion;

import com.gengoai.EnumValue;
import com.gengoai.Primitives;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * String Converter that handles encoding different object types to make them more easily converted back.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class StringTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object object, Type... parameters) throws TypeConversionException {
      if(object instanceof JsonEntry) {
         JsonEntry entry = Cast.as(object);
         if(entry.isString()) {
            return entry.asString();
         }
         return entry.isPrimitive()
                ? entry.get().toString()
                : entry.toString();
      } else if(object instanceof CharSequence) {
         return object.toString();
      } else if(object instanceof char[]) {
         return new String(Cast.<char[]>as(object));
      } else if(object instanceof byte[]) {
         return new String(Cast.<byte[]>as(object));
      } else if(object instanceof Character[]) {
         return new String(Primitives.toCharArray(Arrays.asList(Cast.as(object))));
      } else if(object instanceof Byte[]) {
         return new String(Primitives.toByteArray(Arrays.asList(Cast.as(object))));
      } else if(object.getClass().isArray()) {
         return Json.dumps(object);
      } else if(object instanceof Date) {
         return SimpleDateFormat.getDateTimeInstance().format(object);
      } else if(object instanceof Map) {
         return Json.dumps(object);
      } else if(object instanceof Enum) {
         Enum e = Cast.as(object);
         return e.getDeclaringClass().getName() + "." + e.name();
      } else if(object instanceof EnumValue) {
         return Cast.<EnumValue>as(object).canonicalName();
      } else if(object instanceof Resource) {
         return Cast.<Resource>as(object).descriptor();
      }
      return object.toString();
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(String.class, CharSequence.class);
   }
}//END OF StringTypeConverter
