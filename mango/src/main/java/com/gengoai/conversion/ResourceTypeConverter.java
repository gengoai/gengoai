package com.gengoai.conversion;

import com.gengoai.io.Resources;
import com.gengoai.io.resource.*;
import com.gengoai.json.JsonEntry;
import org.kohsuke.MetaInfServices;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import static com.gengoai.collection.Arrays2.arrayOf;

/**
 * Resource Converter
 *
 * @author David B. Bracewell
 */
@MetaInfServices(value = TypeConverter.class)
public class ResourceTypeConverter implements TypeConverter {
   @Override
   public Object convert(Object source, Type... parameters) throws TypeConversionException {
      if (source instanceof Resource) {
         return source;
      } else if (source instanceof File) {
         return Resources.fromFile((File) source);
      } else if (source instanceof Path) {
         return Resources.fromFile(Cast.<Path>as(source).toFile());
      } else if (source instanceof URL) {
         return Resources.fromUrl((URL) source);
      } else if (source instanceof URI) {
         return new URIResource(Cast.as(source));
      } else if (source instanceof Reader) {
         return new ReaderResource(Cast.as(source));
      } else if (source instanceof InputStream) {
         return new InputStreamResource(Cast.as(source));
      } else if (source instanceof OutputStream) {
         return new OutputStreamResource(Cast.as(source));
      } else if (source instanceof byte[]) {
         return new ByteArrayResource(Cast.as(source));
      } else if (source instanceof Byte[]) {
         return new ByteArrayResource(Converter.convert(source, byte[].class));
      } else if (source instanceof CharSequence) {
         return Resources.from(source.toString());
      } else if (source instanceof Writer) {
         return new WriterResource(Cast.as(source));
      } else if (source instanceof JsonEntry) {
         JsonEntry e = Cast.as(source);
         if (e.isString()) {
            return Resources.from(e.asString());
         }
      }

      throw new TypeConversionException(source, Resource.class);
   }

   @Override
   public Class[] getConversionType() {
      return arrayOf(Resource.class);
   }
}//END OF ResourceTypeConverter
