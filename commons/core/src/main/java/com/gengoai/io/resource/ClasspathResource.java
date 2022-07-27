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

import com.gengoai.Validation;
import com.gengoai.function.Unchecked;
import com.gengoai.io.FileUtils;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.spi.ClasspathResourceProvider;
import com.gengoai.stream.Streams;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p> A <code>Resource</code> implementation for resources that exist on the classpath. Resources are loaded either
 * using the supplied class loader or the system class loader if none is supplied. </p>
 *
 * @author David B. Bracewell
 */
public class ClasspathResource extends BaseResource {
   private static final long serialVersionUID = -1977592698953910323L;
   private final String resource;
   private final ClassLoader classLoader;

   /**
    * Constructs a ClasspathResource resource with a given charset, compression and encoding setting
    *
    * @param resource The resource
    */
   public ClasspathResource(String resource) {
      this(resource, Thread.currentThread().getContextClassLoader());
   }

   /**
    * <p> Creates a classpath resource that uses the given class loader to load the resource. </p>
    *
    * @param resource    The path to the resource.
    * @param classLoader The class loader to use.
    */
   public ClasspathResource(String resource, @NonNull ClassLoader classLoader) {
      this.resource = FileUtils.toUnix(Validation.notNullOrBlank(resource));
      this.classLoader = classLoader;
   }

   @Override
   public Resource append(byte[] byteArray) throws IOException {
      Validation.checkState(canWrite(), "Unable to write to this resource");
      new FileResource(asFile().orElseThrow(NullPointerException::new)).append(byteArray);
      return this;
   }

   @Override
   public Optional<File> asFile() {
      return asURL()
            .filter(u -> u.getProtocol() != null && u.getProtocol().equalsIgnoreCase("file"))
            .map(u -> {
               try {
                  return new File(u.toURI());
               } catch(Exception e) {
                  return null;
               }
            });
   }

   @Override
   public Optional<URI> asURI() {
      return asURL().map(url -> {
         try {
            return url.toURI();
         } catch(URISyntaxException e) {
            return null;
         }
      });
   }

   @Override
   public Optional<URL> asURL() {
      return Optional.ofNullable(classLoader.getResource(resource));
   }

   @Override
   public String baseName() {
      if(asFile().isPresent()) {
         return asFile().get().getName();
      }
      String path = path();
      int index = Math.max(0, Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
      return path.substring(index);
   }

   @Override
   public boolean canRead() {
      if(asFile().isPresent()) {
         return !asFile().get().isDirectory() && asFile().get().canRead();
      }
      try(InputStream is = inputStream()) {
         return is.read() > 0;
      } catch(Exception e) {
         return false;
      }
   }

   @Override
   public boolean canWrite() {
      return asFile().map(File::canWrite).orElse(false);
   }

   @Override
   public InputStream createInputStream() throws IOException {
      return asFile().map(Unchecked.function(file -> Resources.fromFile(file).inputStream()))
                     .orElse(classLoader.getResourceAsStream(resource));
   }

   @Override
   public OutputStream createOutputStream() throws IOException {
      Validation.checkState(canWrite(), "Unable to write to this resource");
      return new FileOutputStream(this.asFile().orElseThrow(NullPointerException::new));
   }

   @Override
   public String descriptor() {
      return ClasspathResourceProvider.SCHEME + ":" + resource;
   }

   @Override
   public boolean equals(Object obj) {
      if(this == obj)
         return true;
      if(obj == null)
         return false;
      if(getClass() != obj.getClass())
         return false;
      ClasspathResource other = (ClasspathResource) obj;
      return Objects.equals(classLoader, other.classLoader) &&
            Objects.equals(resource, other.resource);
   }

   @Override
   public boolean exists() {
      return classLoader.getResource(resource) != null;
   }

   @Override
   public Resource getChild(String relativePath) {
      if(relativePath == null) {
         relativePath = Strings.EMPTY;
      }
      relativePath = relativePath.replaceFirst("^[\\\\/]+", "");
      if(resource.endsWith("/")) {
         return new ClasspathResource(resource + relativePath, classLoader);
      } else {
         return new ClasspathResource(resource + "/" + relativePath, classLoader);
      }
   }

   @Override
   public List<Resource> getChildren(Pattern filePattern, boolean recursive) {
      List<Resource> rval = new ArrayList<>();

      if(!isDirectory()) {
         return rval;
      }

      if(asFile().isPresent()) {
         return Resources.fromFile(asFile().get()).getChildren(filePattern, recursive);
      }

      String path = path() + "/";
      return Streams.asStream(Resources.findAllClasspathResources(path))
                    .flatMap(resource -> resource.getChildren(filePattern, recursive).stream())
                    .collect(Collectors.toList());
   }

   @Override
   public Resource getParent() {
      String parent = FileUtils.parent(resource);
      if(parent == null) {
         return EmptyResource.INSTANCE;
      }
      return new ClasspathResource(parent);
   }

   @Override
   public int hashCode() {
      return Objects.hash(classLoader, resource);
   }

   @Override
   public boolean isDirectory() {
      if(asFile().isPresent()) {
         return asFile().get().isDirectory();
      }
      return !canRead();
   }

   @Override
   public boolean mkdir() {
      return asFile().map(File::mkdir).orElse(false);
   }

   @Override
   public boolean mkdirs() {
      return asFile().map(File::mkdirs).orElse(false);
   }

   @Override
   public String path() {
      return FileUtils.path(resource);
   }

}// END OF ClasspathResource
