/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.io.resource.spi;

import com.gengoai.conversion.Cast;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.URLResource;
import com.gengoai.reflection.BeanMap;
import org.kohsuke.MetaInfServices;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * A provider for URL based resources.
 *
 * @author David B. Bracewell
 */
@MetaInfServices
public class URLResourceProvider implements ResourceProvider {

   @Override
   public String[] getProtocols() {
      return new String[]{"http", "https"};
   }

   @Override
   public Resource createResource(String specification, Map<String, String> properties) {
      BeanMap beanMap = null;
      try {
         beanMap = new BeanMap(new URLResource(specification));
      } catch(MalformedURLException e) {
         throw new RuntimeException(e);
      }
      beanMap.putAll(properties);
      return Cast.as(beanMap.getBean());
   }

   @Override
   public boolean requiresProtocol() {
      return true;
   }

}//END OF FileResourceProvider
