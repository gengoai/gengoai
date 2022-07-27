package com.gengoai.io.resource.spi;

import com.gengoai.conversion.Cast;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.ZipResource;
import com.gengoai.reflection.BeanMap;
import org.kohsuke.MetaInfServices;

import java.util.Map;

/**
 * @author David B. Bracewell
 */
@MetaInfServices
public class ZipResourceProvider implements ResourceProvider {
   @Override
   public String[] getProtocols() {
      return new String[]{"zip", "jar"};
   }

   @Override
   public Resource createResource(String specification, Map<String, String> properties) {
      BeanMap beanMap = new BeanMap(new ZipResource(specification, null));
      beanMap.putAll(properties);
      return Cast.as(beanMap.getBean());
   }

   @Override
   public boolean requiresProtocol() {
      return false;
   }

}//END OF ZipResourceProvider
