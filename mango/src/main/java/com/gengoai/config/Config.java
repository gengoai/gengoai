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

package com.gengoai.config;

import com.gengoai.Language;
import com.gengoai.LogUtils;
import com.gengoai.SystemInfo;
import com.gengoai.application.Application;
import com.gengoai.application.CommandLineApplication;
import com.gengoai.application.CommandLineParser;
import com.gengoai.application.NamedOption;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Val;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.ClasspathResource;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.parsing.ParseException;
import com.gengoai.reflection.BeanUtils;
import com.gengoai.reflection.ReflectionException;
import com.gengoai.string.Strings;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.LogUtils.*;

/**
 * <p> A complete configuration class that allows for inheritance, multiline inputs, variable interpolation, and
 * scripting. </p>
 * <p>
 * <ul>
 * <li>${var} will substitute the value of "var"</li>
 * </ul>
 * </p>
 *
 * @author David B. Bracewell
 */
@Log
public final class Config implements Serializable {
   /**
    * File extensions for config files.
    */
   public static final String CONF_EXTENSION = ".conf";
   private static final String BEAN_PROPERTY = "@{";
   private static final Pattern BEAN_SUBSTITUTION = Pattern.compile(Pattern.quote(BEAN_PROPERTY) + "(.+?)\\}");
   private static final String DEFAULT_CONFIG_FILE_NAME = "default.conf";
   private static final Pattern STRING_SUBSTITUTION = Pattern.compile("\\$\\{(.+?)}");
   private static final String SYSTEM_PROPERTY = "system.";
   private static final long serialVersionUID = 6875819132224789761L;
   private volatile static Config INSTANCE;
   private static ClassLoader defaultClassLoader = Config.class.getClassLoader();
   private static Resource localConfigDirectory = Resources.fromFile(SystemInfo.USER_HOME + "/config/");

   static {
      if (System.getProperty("java.util.logging.config.file") == null) {
         try {
            ROOT.setLevel(Level.INFO);
            for (Handler handler : ROOT.getHandlers()) {
               ROOT.removeHandler(handler);
            }
            final ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFilter(LOG_FILTER);
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(FORMATTER);
            ROOT.addHandler(consoleHandler);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

   private final Set<String> loaded = new ConcurrentSkipListSet<>();
   private final Map<String, String> properties = new ConcurrentHashMap<>();
   /**
    * The Setter function.
    */
   ConfigPropertySetter setterFunction = ConfigSettingError.INSTANCE;

   /**
    * Clears all set properties
    */
   public static void clear() {
      getInstance().loaded.clear();
      getInstance().properties.clear();
   }

   /**
    * Finds the best key for the given class and child path. If a language is specified, it should be the first argument
    * of the path.
    *
    * @param root the root
    * @param path the path
    * @return the best matching key or null
    */
   public static String findKey(Class<?> root, Object... path) {
      return findKey(root.getName(), path);
   }

   /**
    * Finds the best key for the given class and child path. If a language is specified, it should be the first argument
    * of the path.
    *
    * @param root the root
    * @param path the path
    * @return the best matching key or null
    */
   public static String findKey(String root, Object... path) {
      if (path == null || path.length == 0) {
         return hasProperty(root)
               ? root
               : null;
      }
      Language language = path[0] instanceof Language
            ? Cast.as(path[0])
            : null;

      if (language == null) {
         String key = root + "." + Strings.join(path, ".");
         return hasProperty(key)
               ? key
               : null;
      }

      if (path.length == 1) {
         for (String key : new String[]{
               root + "." + language,
               root + "." + language.toString().toLowerCase(),
               root + "." + language.getCode(),
               root + "." + language.getCode().toLowerCase(),
               root
         }
         ) {
            if (hasProperty(key)) {
               return key;
            }
         }
         return null;
      }

      String components = Strings.join(Arrays.copyOfRange(path, 1, path.length), ".");
      for (String key : new String[]{
            root + "." + language + "." + components,
            root + "." + language.toString().toLowerCase() + "." + components,
            root + "." + language.getCode() + "." + components,
            root + "." + language.getCode().toLowerCase() + "." + components,
            root + "." + components + "." + language,
            root + "." + components + "." + language.toString().toLowerCase(),
            root + "." + components + "." + language.getCode(),
            root + "." + components + "." + language.getCode().toLowerCase(),
            root + "." + components
      }) {
         if (hasProperty(key)) {
            return key;
         }
      }

      return null;
   }

   /**
    * Static method allowing the Config to be deserialized from Json
    *
    * @param entry  the Json entry
    * @param params The type parameters (not used)
    * @return the Config instance
    */
   public static Config fromJson(JsonEntry entry, Type... params) {
      clear();
      getInstance().loaded.addAll(entry.getProperty("loaded").asArray(String.class));
      getInstance().properties.putAll(entry.getProperty("properties").asMap(String.class));
      return getInstance();
   }

   /**
    * Gets the value of a property for a given class
    *
    * @param clazz              The class
    * @param propertyComponents The components
    * @return The value associated with clazz.propertyName
    */
   public static Val get(Class<?> clazz, Object... propertyComponents) {
      return get(clazz.getName(), propertyComponents);
   }

   /**
    * Gets the value of a property for a given class and language (the language is optional)
    *
    * @param propertyPrefix     The prefix
    * @param propertyComponents The components.
    * @return The value associated with clazz.propertyName.language exists or clazz.propertyName
    */
   public static Val get(String propertyPrefix, Object... propertyComponents) {
      String key = findKey(propertyPrefix, propertyComponents);
      if (key == null) {
         return Val.NULL;
      }

      String value;
      if (getInstance().properties.containsKey(key)) {
         value = getInstance().properties.get(key);
      } else if (System.getProperty(key) != null) {
         value = System.getProperty(key);
      } else {
         value = System.getenv(key);
      }

      if (value == null) {
         return Val.NULL;
      }

      //resolve variables
      if (STRING_SUBSTITUTION.matcher(value).find()) {
         value = resolveVariables(getInstance().properties.get(key));
      }

      if (value == null) {
         return Val.NULL;
      }

      if (value.contains(BEAN_PROPERTY)) {
         return getBean(value);
      }

      return Val.of(value);
   }

   private static Val getBean(String value) {
      Matcher m = BEAN_SUBSTITUTION.matcher(value);
      if (m.find()) {
         try {
            return Val.of(BeanUtils.getNamedBean(m.group(1), Object.class));
         } catch (ReflectionException e) {
            throw new RuntimeException(e);
         }
      }
      return Val.NULL;
   }

   private static String getCallingClass() {
      // Auto-discover the package of the calling class.
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      for (int i = 1; i < stackTrace.length; i++) {
         StackTraceElement ste = stackTrace[i];
         // ignore the config class
         if (!ste.getClassName().equals(Config.class.getName()) &&
               !ste.getClassName().equals(CommandLineApplication.class.getName())
               && !ste.getClassName().equals(Application.class.getName())
         ) {
            return ste.getClassName();
         }
      }
      return null;
   }

   /**
    * Gets default class loader.
    *
    * @return the default class loader
    */
   public static ClassLoader getDefaultClassLoader() {
      return defaultClassLoader;
   }

   /**
    * Sets the default <code>ClassLoader</code> to use with Classpath resources
    *
    * @param newDefaultClassLoader The new ClassLoader
    */
   public static void setDefaultClassLoader(ClassLoader newDefaultClassLoader) {
      defaultClassLoader = newDefaultClassLoader;
   }

   /**
    * Gets the singleton instance of the config. This should not normally be used by api consumers
    *
    * @return the singleton instance of the config.
    */
   public static Config getInstance() {
      if (INSTANCE == null) {
         synchronized (Config.class) {
            if (INSTANCE == null) {
               INSTANCE = new Config();
            }
         }
      }
      return INSTANCE;
   }

   /**
    * Sets the singleton instance of the config. This is mainly useful in distributed environments where we are passing
    * configurations around.
    *
    * @param config the config that will become the single instance
    * @return the singleton instance of the config
    */
   public static Config setInstance(Config config) {
      synchronized (Config.class) {
         INSTANCE = config;
      }
      return INSTANCE;
   }

   /**
    * <p> Gets a <code>list</code> of properties whose name is matched using the supplied <code>StringMatcher</code>.
    * </p>
    *
    * @param matcher The <code>StringMatcher</code> to use for matching property names.
    * @return A <code>List</code> of properties matched using the <code>StringMatcher</code>.
    */
   public static List<String> getPropertiesMatching(Predicate<? super String> matcher) {
      if (matcher != null) {
         return getInstance().properties
               .keySet()
               .parallelStream()
               .filter(matcher)
               .collect(Collectors.toList());
      }
      return Collections.emptyList();
   }

   /**
    * Checks if a property is in the config or or set on the system. The property name is constructed as
    * <code>propertyPrefix + . + propertyComponent[0] + . + propertyComponent[1] + ...</code>
    *
    * @param root The prefix
    * @param path The components.
    * @return True if the property is known, false if not.
    */
   public static boolean hasProperty(String root, Object... path) {
      if (path != null && path.length > 0) {
         return findKey(root, path) != null;
      }
      return getInstance().properties.containsKey(root) || System.getProperties().contains(root);
   }

   /**
    * Checks if a property is in the config or or set on the system. The property name is constructed as
    * <code>clazz.getName() + . + propertyComponent[0] + . + propertyComponent[1] + ...</code>
    *
    * @param root The class with which the property is associated.
    * @param path The components.
    * @return True if the property is known, false if not.
    */
   public static boolean hasProperty(Class<?> root, Object... path) {
      return hasProperty(root.getName(), path);
   }

   /**
    * <p> Initializes the configuration. </p> <p> Looks for a properties (programName.conf) in the classpath, the user
    * home directory, and in the run directory to load </p> <p> The command line arguments are parsed and added to the
    * configuration. </p>
    *
    * @param programName   the program name
    * @param args          the command line arguments
    * @param parser        the                    to use for parsing the arguments
    * @param otherPackages Other packages whose configs we should load
    * @return Non config/option parameters from command line
    */
   public static String[] initialize(String programName,
                                     String[] args,
                                     CommandLineParser parser,
                                     String... otherPackages) {
      Preloader.preload();

      String rval[];
      if (args != null) {
         rval = parser.parse(args);
      } else {
         rval = new String[0];
      }

      //Check if we should only explain the config
      if (NamedOption.DUMP_CONFIG.<Boolean>getValue()) {
         getInstance().setterFunction = ConfigExplainSettingFunction.INSTANCE;
      } else {
         getInstance().setterFunction = ConfigSettingFunction.INSTANCE;
      }

      if (otherPackages != null) {
         for (String otherPackage : otherPackages) {
            loadPackageConfig(otherPackage);
         }
      }

      // Auto-discover the package of the calling class.
      String className = getCallingClass();

      if (className != null) {
         loadDefaultConfig(className);
      }

      loadApplicationConfig(programName);

      // Store the command line arguments as a config settings.
      if (args != null) {
         parser.getSetEntries()
               .forEach(
                     entry -> ConfigSettingFunction.INSTANCE.setProperty(entry.getKey(),
                                                                         entry.getValue(),
                                                                         "CommandLine"));
      }

      if (parser.isSet(NamedOption.CONFIG)) {
         loadConfig(NamedOption.CONFIG.getValue());
      }

      setAllCommandLine(parser);

      // If config-explain was set then output the config recording and then quit
      if (parser.isSet(NamedOption.DUMP_CONFIG)) {
         ConfigExplainSettingFunction settings = (ConfigExplainSettingFunction) getInstance().setterFunction;
         for (String key : new TreeSet<>(settings.properties.keySet())) {
            System.err.println(key);
            int max = settings.properties.get(key).size();
            int i = 1;
            for (String prop : settings.properties.get(key)) {
               System.err.println("\t" + (i == max
                     ? "*"
                     : "") + prop.replaceAll("\r?\n", "  "));
               i++;
            }
            System.err.println("--------------------------------------------------");
         }
         System.exit(0);
      }

      return rval;
   }

   /**
    * <p> Initializes the configuration. </p> <p> Looks for a properties (programName.conf) in the classpath, the user
    * home directory, and in the run directory to load </p> <p> The command line arguments are parsed and added to the
    * configuration. </p>
    *
    * @param programName   the program name
    * @param args          the command line arguments
    * @param otherPackages Other packages whose configs we should load
    * @return Non config/option parameters from command line
    */
   public static String[] initialize(String programName, String[] args, String... otherPackages) {
      return initialize(programName, args, new CommandLineParser(), otherPackages);
   }

   /**
    * Initialize test.
    */
   public static void initializeTest() {
      clear();
      initialize("Test", new String[0], new CommandLineParser());
   }

   /**
    * Is bean boolean.
    *
    * @param property the property
    * @return the boolean
    */
   public static boolean isBean(String property) {
      if (Strings.isNullOrBlank(getInstance().properties.get(property))) {
         return false;
      }
      return BEAN_SUBSTITUTION.matcher(getInstance().properties.get(property)).find();
   }

   /**
    * Determines if a given configuration resource has been loaded
    *
    * @param configResource the config resource to check.
    * @return True if the config has been loaded, False if not
    */
   public static boolean isConfigLoaded(Resource configResource) {
      return getInstance().loaded.contains(configResource.path());
   }

   public static void loadApplicationConfig(String applicationName) {
      if (!Strings.isNullOrBlank(applicationName)) {
         getInstance().setterFunction = ConfigSettingFunction.INSTANCE;
         final String appConfName = applicationName.replaceAll("\\s+", "_").toLowerCase() + CONF_EXTENSION;
         //Look for application specific properties
         Stream.of(
               new ClasspathResource(appConfName, defaultClassLoader),
               Resources.fromFile(new File(SystemInfo.USER_HOME, appConfName)),
               localConfigDirectory.getChild(appConfName),
               Resources.fromFile(new File(appConfName))
         )
               .filter(Resource::exists)
               .forEach(resource -> {
                  logFine(log, "Loading Application Configuration from {0}", resource.path());
                  loadConfig(resource);
               });
      }
   }

   /**
    * Loads a config file
    *
    * @param resource The config file
    */
   public static void loadConfig(Resource resource) {
      if (resource == null || !resource.exists()) {
         //throw new RuntimeException("Unable to Load Config: " + resource);
         return;
      }
      if (resource.path() != null && getInstance().loaded.contains(resource.path())) {
         return; //Only load once!
      }
      try {
         MsonConfigParser.parseResource(resource);
      } catch (ParseException | IOException e) {
         throw new RuntimeException(e);
      }
      if (resource.path() != null) {
         getInstance().loaded.add(resource.path());
      }
   }

   private static void loadDefaultConfig(String packageName) {
      Resource defaultConf = new ClasspathResource(packageToDefaultConfig(packageName), Config.getDefaultClassLoader());
      // Go through each level of the package until we find one that
      // has a default properties file or we cannot go any further.
      while (!defaultConf.exists()) {
         int idx = packageName.lastIndexOf('.');
         if (idx == -1) {
            break;
         }
         packageName = packageName.substring(0, idx);
         defaultConf = new ClasspathResource(packageToDefaultConfig(packageName), Config.getDefaultClassLoader());
      }
      if (defaultConf.exists()) {
         loadConfig(defaultConf);
      }
   }

   /**
    * Load package config.
    *
    * @param packageName the package name
    */
   public static void loadPackageConfig(String packageName) {
      loadConfig(Resources.fromClasspath(packageToDefaultConfig(packageName)));
   }

   private static String packageToDefaultConfig(String packageName) {
      return (packageName.replaceAll("\\.", "/") + "/" + DEFAULT_CONFIG_FILE_NAME).trim();
   }

   /**
    * Resolve variables string.
    *
    * @param string the string
    * @return the string
    */
   public static String resolveVariables(String string) {
      if (string == null) {
         return null;
      }

      String rval = string;
      Matcher m = STRING_SUBSTITUTION.matcher(string);
      while (m.find()) {
         if (getInstance().properties.containsKey(m.group(1))) {
            rval = rval.replaceAll(Pattern.quote(m.group(0)), get(m.group(1)).asString());
         } else if (System.getProperties().contains(m.group(1))) {
            rval = rval.replaceAll(Pattern.quote(m.group(0)), System.getProperties().get(m.group(1)).toString());
         } else if (System.getenv().containsKey(m.group(1))) {
            rval = rval.replaceAll(Pattern.quote(m.group(0)), System.getenv().get(m.group(1)));
         }
      }
      return rval;
   }

   /**
    * Sets all properties from the given array of arguments
    *
    * @param args the args
    */
   public static void setAllCommandLine(String[] args) {
      if (args != null) {
         CommandLineParser parser = new CommandLineParser();
         parser.parse(args);
         setAllCommandLine(parser);
      }
   }

   /**
    * Sets all properties from the given command line parser.
    *
    * @param parser the parser
    */
   public static void setAllCommandLine(CommandLineParser parser) {
      if (parser != null) {
         parser.getSetEntries()
               .forEach(entry -> getInstance().setterFunction.setProperty(entry.getKey(),
                                                                          entry.getValue(),
                                                                          "CommandLine"));
      }
   }

   public static void setProperty(@NonNull String name, Object value) {
      if (value == null) {
         getInstance().properties.remove(name);
      }
      if (value instanceof String) {
         setProperty(name, value.toString());
         return;
      }
      setProperty(name, Json.asJsonEntry(value).toString());
   }

   /**
    * Sets the value of a property.
    *
    * @param name  the name of the property
    * @param value the value of the property
    */
   public static void setProperty(@NonNull String name, String value) {
      getInstance().properties.put(name, value);
      if (name.toLowerCase().endsWith(".level")) {
         String className = name.substring(0, name.length() - ".level".length());
         LogUtils.setLevel(className, Level.parse(value.trim().toUpperCase()));
      }
      if (name.equals("com.gengoai.logging.logfile")) {
         try {
            LogUtils.addFileHandler(value);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
      if (name.toLowerCase().startsWith(SYSTEM_PROPERTY)) {
         String systemSetting = name.substring(SYSTEM_PROPERTY.length());
         System.setProperty(systemSetting, value);
      }
      logFinest(log, "Setting property {0} to value of {1}", name, value);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      getInstance().properties.putAll(Cast.<Map<String, String>>as(in.readObject()));
      getInstance().loaded.addAll(Cast.<Set<String>>as(in.readObject()));
   }

   public JsonEntry toJson() {
      return JsonEntry.object()
                      .addProperty("loaded", loaded)
                      .addProperty("properties", properties);
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.defaultWriteObject();
      out.writeObject(properties);
      out.writeObject(loaded);
   }

   /**
    * A ConfigPropertySetter implementation that records which config file set a property. This class is used with the
    * <code>config-explain</code> command line option.
    */
   enum ConfigExplainSettingFunction implements ConfigPropertySetter {
      /**
       * The INSTANCE.
       */
      INSTANCE;
      /**
       * The Properties.
       */
      final Map<String, Set<String>> properties = new HashMap<>();

      @Override
      public void setProperty(String name, String value, String resourceName) {
         Config.setProperty(name, value);
         if (!properties.containsKey(name)) {
            properties.put(name, new LinkedHashSet<>());
         }
         properties.get(name).add(resourceName + "::" + value);
      }

   }

   /**
    * The enum Config setting error.
    */
   enum ConfigSettingError implements ConfigPropertySetter {
      /**
       * The INSTANCE.
       */
      INSTANCE;

      @Override
      public void setProperty(String name, String value, String resourceName) {
         throw new IllegalStateException("Config not initialized");
      }

   }

   /**
    * Standard implementation of ConfigPropertySetter
    */
   enum ConfigSettingFunction implements ConfigPropertySetter {
      /**
       * The INSTANCE.
       */
      INSTANCE;

      @Override
      public void setProperty(String name, String value, String resourceName) {
         Config.setProperty(name, value);
      }

   }

   /**
    * A <code>ConfigPropertySetter</code> takes care of setting properties and their values are they are parsed by the
    * {@link MsonConfigParser}.
    *
    * @author David B. Bracewell
    */
   interface ConfigPropertySetter {

      /**
       * Sets a property
       *
       * @param name         The name of the property
       * @param value        The value of the property
       * @param resourceName The resource that is responsible for this property
       */
      void setProperty(String name, String value, String resourceName);

   }// END OF ConfigPropertySetter

}// END OF Config
