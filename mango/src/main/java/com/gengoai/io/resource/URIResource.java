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

package com.gengoai.io.resource;

import com.gengoai.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * Resource that wraps a URI
 *
 * @author David B. Bracewell
 */
public class URIResource extends BaseResource {
   private static final long serialVersionUID = 1L;
   private final URI uri;

   /**
    * Instantiates a new Uri resource.
    *
    * @param uri the uri
    */
   public URIResource(URI uri) {
      this.uri = uri;
   }

   @Override
   public Resource append(byte[] byteArray) throws IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public Optional<File> asFile() {
      if(uri.getScheme().equalsIgnoreCase("file")) {
         return Optional.of(new File(uri.getPath()));
      }
      return super.asFile();
   }

   @Override
   public Optional<URI> asURI() {
      return Optional.of(uri);
   }

   @Override
   protected InputStream createInputStream() throws IOException {
      try {
         return super.createInputStream();
      } catch(UnsupportedOperationException ue) {
         return uri.toURL().openConnection().getInputStream();
      }
   }

   @Override
   protected OutputStream createOutputStream() throws IOException {
      try {
         return super.createOutputStream();
      } catch(UnsupportedOperationException ue) {
         return uri.toURL().openConnection().getOutputStream();
      }
   }

   @Override
   public String descriptor() {
      return uri.toString();
   }

   @Override
   public boolean equals(Object o) {
      if(this == o) return true;
      if(!(o instanceof URIResource)) return false;
      URIResource that = (URIResource) o;
      return Objects.equals(uri, that.uri);
   }

   @Override
   public boolean exists() {
      try(InputStream is = createInputStream()) {
         return true;
      } catch(IOException e) {
         return false;
      }
   }

   @Override
   public Resource getChild(String relativePath) {
      return new URIResource(uri.resolve("/" + relativePath));
   }

   @Override
   public Resource getParent() {
      try {
         return new URIResource(new URI(FileUtils.parent(uri.toString())));
      } catch(URISyntaxException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(uri);
   }

   @Override
   public String path() {
      return uri.getPath();
   }

}//END OF URIResource
