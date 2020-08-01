package com.gengoai.conversion;

import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.Reflect;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;

import static com.gengoai.collection.Arrays2.arrayOf;
import static com.gengoai.reflection.TypeUtils.*;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class EnumTypeConverter implements TypeConverter {

   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      Class<?> enumClass = null;
      if (source instanceof Class) {
         return convert(Cast.<Class>as(source).getName(), parameters);
      }

      if( source instanceof JsonEntry){
         return convert(Cast.<JsonEntry>as(source).get(), parameters);
      }
      if (source instanceof CharSequence) {
         String sourceStr = source.toString();
         int lastDot = sourceStr.lastIndexOf('.');
         int lastDollar = sourceStr.lastIndexOf('$');
         if (lastDollar > 0) {
            enumClass = Reflect.getClassForNameQuietly(sourceStr.substring(0, lastDollar));
            try {
               source = enumClass.getFields()[Integer.parseInt(sourceStr.substring(lastDollar + 1)) - 1].get(null);
            } catch (IllegalAccessException e) {
               throw new TypeConversionException(source, parameterizedType(Enum.class, enumClass));
            }
         } else if (lastDot > 0) {
            enumClass = Reflect.getClassForNameQuietly(sourceStr.substring(0, lastDot));
            source = sourceStr.substring(lastDot + 1);
         }
      }
      if (enumClass == null) {
         enumClass = asClass(getOrObject(0, parameters));
      }
      if (enumClass == null || !Enum.class.isAssignableFrom(enumClass)) {
         throw new TypeConversionException("Invalid type parameter (" + enumClass + ")");
      }

      if (source instanceof Enum) {
         return source;
      }
      if (source instanceof CharSequence) {
         try {
            return Enum.valueOf(Cast.as(enumClass), source.toString());
         } catch (Exception e) {
            throw new TypeConversionException(source, Enum.class, e);
         }
      }
      throw new TypeConversionException(source, parameterizedType(Enum.class, enumClass));
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(Enum.class);
   }
}//END OF EnumTypeConverter
