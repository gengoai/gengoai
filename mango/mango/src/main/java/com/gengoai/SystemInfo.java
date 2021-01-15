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

package com.gengoai;

import java.util.Locale;

/**
 * <p>Common system properties accessed via the runtime or system classes.</p>
 *
 * @author David B. Bracewell
 */
public interface SystemInfo {

   /**
    * Checks if the OS is a unix variant
    *
    * @return True if unix, False otherwise
    */
   static boolean isUnix() {
      return OS_NAME.toLowerCase().contains("nux");
   }

   /**
    * Checks if the OS is a windows variant
    *
    * @return True if windows, False otherwise
    */
   static boolean isWindows() {
      return OS_NAME.toLowerCase().contains("win") && !OS_NAME.toLowerCase().contains("darwin");
   }

   static boolean isMacOs(){
      return OS_NAME.toLowerCase().contains("mac") || OS_NAME.toLowerCase().contains("darwin");
   }

   /**
    * The number of available processors according to the runtime
    */
   int NUMBER_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();
   /**
    * The character encoding for the default locale *
    */
   String FILE_ENCODING = System.getProperty("file.encoding");
   /**
    * The package that contains the converters that handle converting between local encodings and Unicode
    */
   String FILE_ENCODING_PKG = System.getProperty("file.encoding.pkg");
   /**
    * The platform-dependent file separator (e.g., "/" on UNIX, "\" for Windows)
    */
   String FILE_SEPARATOR = System.getProperty("file.separator");
   /**
    * The value of the CLASSPATH environment variable *
    */
   String JAVA_CLASS_PATH = System.getProperty("java.class.path");
   /**
    * The version of the Java API *
    */
   String JAVA_CLASS_VERSION = System.getProperty("java.class.version");
   /**
    * The just-in-time compiler to use, if any. The java interpreter provided with the JDK initializes this property
    * from the environment variable JAVA_COMPILER.
    */
   String JAVA_COMPILER = System.getProperty("java.compiler");
   /**
    * The directory in which Java is installed *
    */
   String JAVA_HOME = System.getProperty("java.home");
   /**
    * The directory in which java should create temporary files *
    */
   String JAVA_IO_TMPDIR = System.getProperty("java.io.tmpdir");
   /**
    * The version of the Java interpreter *
    */
   String JAVA_VERSION = System.getProperty("java.version");
   /**
    * A vendor-specific string *
    */
   String JAVA_VENDOR = System.getProperty("java.vendor");
   /**
    * A vendor URL *
    */
   String JAVA_VENDOR_URL = System.getProperty("java.vendor.url");
   /**
    * The platform-dependent line separator (e.g., "\n" on UNIX, "\r\n" for Windows)
    */
   String LINE_SEPARATOR = System.getProperty("line.separator");
   /**
    * The name of the operating system *
    */
   String OS_NAME = System.getProperty("os.name");
   /**
    * The system architecture *
    */
   String OS_ARCH = System.getProperty("os.arch");
   /**
    * The operating system version *
    */
   String OS_VERSION = System.getProperty("os.version");
   /**
    * The platform-dependent path separator (e.g., ":" on UNIX, "," for Windows)
    */
   String PATH_SEPARATOR = System.getProperty("path.separator");
   /**
    * The current working directory when the properties were initialized *
    */
   String USER_DIR = System.getProperty("user.dir");
   /**
    * The home directory of the current user *
    */
   String USER_HOME = System.getProperty("user.home");
   /**
    * The two-letter language code of the default locale *
    */
   String USER_LANGUAGE = System.getProperty("user.language");
   /**
    * The username of the current user *
    */
   String USER_NAME = System.getProperty("user.name");
   /**
    * The two-letter country code of the default locale *
    */
   String USER_REGION = System.getProperty("user.region");
   /**
    * The default time zone *
    */
   String USER_TIMEZONE = System.getProperty("user.timezone");


}// END OF CLASS SystemInfo
