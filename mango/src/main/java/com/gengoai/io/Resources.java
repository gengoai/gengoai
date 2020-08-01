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

package com.gengoai.io;

import com.gengoai.SystemInfo;
import com.gengoai.Validation;
import com.gengoai.conversion.Converter;
import com.gengoai.io.resource.*;
import com.gengoai.io.resource.spi.ResourceProvider;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convenience methods for constructing <code>Resource</code>s
 *
 * @author David B. Bracewell
 */
public final class Resources {

   private static final Map<String, ResourceProvider> resourceProviders = new HashMap<>();
   private static final Pattern protocolPattern = Pattern.compile(
         "^(?<PROTOCOL>\\w+)(?<OPTIONS>\\[(?:[^\\]]+)\\])?:(?<PATH>.*)?");

   static {
      for(ResourceProvider provider : ServiceLoader.load(ResourceProvider.class)) {
         for(String protocol : provider.getProtocols()) {
            resourceProviders.put(protocol.toLowerCase(), provider);
         }
      }
   }

   /**
    * Finds all resources with the given pattern across loaded ClassLoaders
    *
    * @param pattern the file pattern
    * @return Iterator of resources
    */
   public static Iterator<Resource> findAllClasspathResources(String pattern) {
      Validation.notNullOrBlank(pattern);
      final Enumeration<URL> resources;
      try {
         resources = Thread.currentThread()
                           .getContextClassLoader()
                           .getResources(pattern);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      return new Iterator<Resource>() {

         @Override
         public boolean hasNext() {
            return resources.hasMoreElements();
         }

         @Override
         public Resource next() {
            URL next = resources.nextElement();
            if(next.getProtocol().equalsIgnoreCase("jar")) {
               int idx = next.getPath().indexOf("!");
               return new ZipResource(next.getPath().substring(0, idx),
                                      next.getPath().substring(idx + 1));
            }
            return fromUrl(next);
         }
      };
   }

   /**
    * Constructs a resource from a string representation. Defaults to a file based resource if no schema is present.
    *
    * @param resource The string representation of the resource
    * @return A resource representing the string representation
    */
   public static Resource from(String resource) {
      if(Strings.isNullOrBlank(resource)) {
         return new StringResource();
      }
      Matcher matcher = protocolPattern.matcher(resource);
      if(matcher.find()) {
         String schema = matcher.group("PROTOCOL");
         String options = matcher.group("OPTIONS");
         String path = matcher.group("PATH");

         if(Strings.isNullOrBlank(options)) {
            options = "";
         }
         ResourceProvider provider = resourceProviders.get(schema.toLowerCase());

         if(provider == null) {
            try {
               return new URIResource(new URI(resource));
            } catch(URISyntaxException e) {
               throw new IllegalStateException(schema + " is an unknown protocol.");
            }
         }

         if(provider.requiresProtocol()) {
            path = schema + ":" + path;
         }
         return provider.createResource(path,
                                        Converter.convertSilently(options, Map.class, String.class, String.class));
      }

      return new FileResource(resource);
   }

   /**
    * <p> Creases a new {@link ClasspathResource}. </p>
    *
    * @param resource The classpath making up the resource
    * @return A new Resource associated with the classpath
    */
   public static Resource fromClasspath(String resource) {
      return new ClasspathResource(resource);
   }

   /**
    * <p> Creases a new {@link FileResource}. </p>
    *
    * @param resource The file making up the resource
    * @return A new Resource associated with the file
    */
   public static Resource fromFile(File resource) {
      return new FileResource(resource);
   }

   /**
    * <p> Creases a new {@link FileResource}. </p>
    *
    * @param resource The file making up the resource
    * @return A new Resource associated with the file
    */
   public static Resource fromFile(String resource) {
      return new FileResource(resource);
   }

   /**
    * From input stream resource.
    *
    * @param inputStream The input stream to wrap
    * @return Resource that can read from given input stream
    */
   public static Resource fromInputStream(InputStream inputStream) {
      return new InputStreamResource(inputStream);
   }

   /**
    * From output stream resource.
    *
    * @param outputStream The output stream to wrap
    * @return Resource that can write to given output stream
    */
   public static Resource fromOutputStream(OutputStream outputStream) {
      return new OutputStreamResource(outputStream);
   }

   /**
    * Creates a new Resource that wraps the given reader
    *
    * @param reader The reader to wrap
    * @return Resource that can read from given reader
    */
   public static Resource fromReader(Reader reader) {
      return new ReaderResource(reader);
   }

   /**
    * From stdin resource.
    *
    * @return Resource that can read from standard in
    */
   public static Resource fromStdin() {
      return new StdinResource();
   }

   /**
    * From stdout resource.
    *
    * @return Resource that can output to standard out
    */
   public static Resource fromStdout() {
      return new StdoutResource();
   }

   /**
    * Creates a <code>StringResource</code> from the given string.
    *
    * @param stringResource the string resource
    * @return the resource
    */
   public static Resource fromString(String stringResource) {
      return new StringResource(stringResource);
   }

   /**
    * Creates a <code>StringResource</code> that is empty.
    *
    * @return the resource
    */
   public static Resource fromString() {
      return new StringResource();
   }

   /**
    * <p> Creates a new {@link URIResource}. </p>
    *
    * @param resource The uri to wrap.
    * @return A new Resource wrapping a uri.
    */
   public static URIResource fromURI(URI resource) {
      return new URIResource(resource);
   }

   /**
    * <p> Creates a new {@link URIResource}. </p>
    *
    * @param resource The uri to wrap.
    * @return A new Resource wrapping a uri.
    */
   public static URIResource fromURI(String resource) {
      return new URIResource(URI.create(resource));
   }

   /**
    * <p> Creates a new {@link URLResource}. </p>
    *
    * @param resource The url to wrap.
    * @return A new Resource wrapping a url.
    */
   public static URLResource fromUrl(URL resource) {
      return new URLResource(resource);
   }

   /**
    * Creates a new Resource that wraps the given writer
    *
    * @param writer the writer wrap
    * @return the resource
    */
   public static Resource fromWriter(Writer writer) {
      return new WriterResource(writer);
   }

   /**
    * Gets the jar file that a class is stored in.
    *
    * @param clazz The class whose associated jar file is descried.
    * @return The Resource (jar file) for the class
    */
   public static Resource getJar(@NonNull Class<?> clazz) {
      URL fileURL = clazz.getProtectionDomain().getCodeSource().getLocation();
      return new FileResource(fileURL.getFile());
   }

   /**
    * Creates a new Resource that points to a temporary directory.
    *
    * @return A resource which is a temporary directory on disk
    */
   public static Resource temporaryDirectory() {
      File tempDir = new File(SystemInfo.JAVA_IO_TMPDIR);
      String baseName = System.currentTimeMillis() + "-";
      for(int i = 0; i < 1_000_000; i++) {
         File tmp = new File(tempDir, baseName + i);
         if(tmp.mkdir()) {
            return new FileResource(tmp);
         }
      }
      throw new RuntimeException("Unable to create temp directory");
   }

   /**
    * Creates a new Resource that points to a temporary file.
    *
    * @return A resource which is a temporary file on disk
    */
   public static Resource temporaryFile() {
      try {
         return temporaryFile(UUID.randomUUID().toString(), "tmp");
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Creates a resource wrapping a temporary file
    *
    * @param name      The file name
    * @param extension The file extension
    * @return The resource representing the temporary file
    * @throws IOException the io exception
    */
   public static Resource temporaryFile(String name, String extension) throws IOException {
      return new FileResource(File.createTempFile(name, extension));
   }

   public static Resource writeObject(Object o) throws IOException {
      ByteArrayResource r = new ByteArrayResource();
      try {
         r.writeObject(o);
      } catch(Exception e) {
         throw new IOException(e);
      }
      return r;
   }

}// END OF Resources
