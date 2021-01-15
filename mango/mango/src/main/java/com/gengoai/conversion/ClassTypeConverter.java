package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.TypeUtils;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.asClass;

/**
 * Converts object into Class values. Conversion is possible for the following types:
 * <ul>
 * <li>Type: All types are attempted to convert to classes using {@link TypeUtils#asClass(Type)}</li>
 * <li>{@link JsonEntry} and CharSequence: Converted to strings and treated as class names</li>
 * <li>Other: <code>Object.getClass()</code></li>
 * </ul>
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class ClassTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object object, Type... parameters) throws TypeConversionException {
      if (object instanceof Type) {
         try {
            return asClass(Cast.as(object));
         } catch (IllegalArgumentException e) {
            throw new TypeConversionException(object, Class.class, e);
         }
      } else if (object instanceof CharSequence || object instanceof JsonEntry) {
         Class<?> clazz = Reflect.getClassForNameQuietly(Converter.convert(object, String.class));
         if (clazz != null) {
            return clazz;
         }

         try {
            return TypeUtils.asClass(TypeUtils.parse(object.toString()));
         } catch (Exception e) {
            //Ignore to return String.class
         }
      }
      return object.getClass();
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(Class.class);
   }
}//END OF ClassTypeConverter
