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

package com.gengoai.application;

import com.gengoai.collection.Iterables;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.function.Unchecked;
import com.gengoai.reflection.RField;
import com.gengoai.reflection.Reflect;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * <p>A command line parser that can handle non-specified arguments. Arguments can be specified by manually adding
 * {@link NamedOption}s via the {@link #addOption(NamedOption)} method or by setting the parser's owner object via the
 * constructor which will look for fields annotated with {@link Option}. All parsers will have  help (-h or --help),
 * config (--config), and explain config (--config-explain) options added.</p>
 *
 * <p>The parser accepts long (e.g. --longOption) and short (e.g. -s) arguments. Multiple short (e.g. single character)
 * arguments can be specified at one time (e.g. -xzf would set the x, z, and f options to true). Short arguments may
 * have values (e.g. -f FILENAME).</p>
 *
 *
 * <p>Values for options will be specified on the corresponding {@link NamedOption} instance. The value can be
 * retrieved either directly from the NamedOption or by using the {@link #get(String)} method. Argument names need not
 * specify the "--" or "-" prefix.</p>
 *
 * @author David B. Bracewell
 */
public final class CommandLineParser {

   private static final String KEY_VALUE_SEPARATOR = "=";
   private static final String LONG = "--";
   private static final String SHORT = "-";
   private final Map<String, NamedOption> options = new HashMap<>();
   private final Set<NamedOption> optionSet = new HashSet<>();
   private final Map<String, String> unnamedOptions = new HashMap<>();
   private final Object owner;
   private final String applicationDescription;


   /**
    * Instantiates a new Command line parser.
    */
   public CommandLineParser() {
      this(null, Strings.EMPTY);
   }

   /**
    * Instantiates a new Command line parser.
    *
    * @param owner                  the owner
    * @param applicationDescription the application description
    */
   public CommandLineParser(Object owner, String applicationDescription) {
      this.owner = owner;
      if (owner != null) {

         for (RField field : Reflect.onObject(owner)
                                    .allowPrivilegedAccess()
                                    .getFieldsWithAnnotation(Option.class)) {
            addOption(new NamedOption(field));
         }
      }
      this.applicationDescription = Strings.isNullOrBlank(applicationDescription)
                                    ? "Help"
                                    : applicationDescription.trim();
      addOption(NamedOption.HELP);
      addOption(NamedOption.CONFIG);
      addOption(NamedOption.DUMP_CONFIG);
   }

   private static boolean isOptionName(String string) {
      return string.startsWith(LONG) || string.startsWith(SHORT);
   }

   /**
    * Add option.
    *
    * @param namedOption the named option
    */
   public CommandLineParser addOption(NamedOption namedOption) {
      options.put(namedOption.getName(), namedOption);
      optionSet.add(namedOption);
      if (namedOption.getAliases() != null) {
         for (String alias : namedOption.getAliases()) {
            options.put(alias, namedOption);
         }
      }
      return this;
   }

   private int processKeyValue(List<String> cleanedArgs, int i, String current, List<String> filtered) {
      String value = "true";
      String next = Iterables.get(cleanedArgs, i + 1, Strings.EMPTY);
      if (next.equalsIgnoreCase(KEY_VALUE_SEPARATOR)) {
         //If are peeked at value is our key value separator then lets set the value to what comes after.
         value = Iterables.get(cleanedArgs, i + 2, Strings.EMPTY);
         if (isOptionName(value)) {
            //If the value is an option name, we have an error
            throw new CommandLineParserException(current, null);
         }
         i += 2;
      } else if (!isOptionName(next)) {
         //If are peeked at value isn't our key value separator and isn't another option name, lets set the value to what comes after.
         i++;
         value = next;
      }
      setValue(current, value, filtered);
      return i;
   }

   /**
    * Parses an array of arguments
    *
    * @param args The command line arguments.
    * @return the non-config/option parameters
    */
   public String[] parse(String[] args) {
      List<String> filtered = new ArrayList<>();
      List<String> cleanedArgs = new ArrayList<>();

      //Pass through the args once to do a clean up. Will make the next round easier to parser.
      for (String c : args) {
         if (c.endsWith(KEY_VALUE_SEPARATOR) && isOptionName(c)) {
            //If it is ends with a key/value separator and is an option
            //lets separate them into two tokens.
            cleanedArgs.add(c.substring(0, c.length() - 1));
            cleanedArgs.add(KEY_VALUE_SEPARATOR);
         } else if (c.contains(KEY_VALUE_SEPARATOR) && isOptionName(c)) {
            //If it contains a key/value separator, but doesn't end in one and is an option
            //then we have a key value pair that will split into three tokens [key, =, value]
            int index = c.indexOf(KEY_VALUE_SEPARATOR);
            cleanedArgs.add(c.substring(0, index));
            cleanedArgs.add(KEY_VALUE_SEPARATOR);
            cleanedArgs.add(c.substring(index + KEY_VALUE_SEPARATOR.length()));
         } else if (Strings.isNotNullOrBlank(c)) {
            //Anything else that isn't null or blank we keep as is
            cleanedArgs.add(c);
         }
      }


      for (int i = 0; i < cleanedArgs.size(); i++) {
         String current = cleanedArgs.get(i);

         //Check that the token isn't just a long or short argument marker.
         if (LONG.equals(current) || SHORT.equals(current)) {
            throw new CommandLineParserException(current, null);
         }

         if (current.startsWith(LONG)) {
            //Process Long form Argument
            i = processKeyValue(cleanedArgs, i, current, filtered);
         } else if (current.startsWith(SHORT)) {
            //We have a short form argument.
            //Are we setting multiple "boolean" values?
            if (current.length() > 2) {
               //All of these will be set to true
               char[] opts = current.substring(1).toCharArray();
               for (char c : opts) {
                  setValue("-" + c, "true", filtered);
               }
            } else {
               i = processKeyValue(cleanedArgs, i, current, filtered);
            }
         } else {
            filtered.add(current);
         }

      }

      //Check if the help was set, if a required argument was not set, and try to set the associated field on the owner object if one is set.
      optionSet.forEach(Unchecked.consumer(option -> {
         if (option.getName().equals("h") && Cast.<Boolean>as(option.getValue())) {
            showHelp();
            System.exit(0);
         } else if (option.isRequired() && !isSet(option.getName())) {
            System.err.println("ERROR: " + option.getName() + " is required, but was not set.");
            showHelp();
            System.exit(-1);
         }

         if (owner != null && option.getField() != null) {
            Reflect.onObject(owner)
                   .allowPrivilegedAccess()
                   .getField(option.getField().getName())
                   .set(option.getValue());
         }
      }));

      return filtered.toArray(new String[0]);
   }


   /**
    * Prints help to standard error showing the application description, if set, and the list of valid command line
    * arguments with those required arguments marked with an asterisk.
    */
   public void showHelp() {
      System.err.println(applicationDescription);
      System.err.println("===============================================");

      Map<NamedOption, String> optionNames = new HashMap<>();
      optionSet.forEach(option -> {
         String out = Stream.concat(
            Stream.of(option.getAliasSpecifications()),
            Stream.of(option.getSpecification()))
                            .sorted(Comparator.comparingInt(String::length))
                            .collect(Collectors.joining(", "));
         if (option.isRequired()) {
            out += " *";
         }
         optionNames.put(option, out);
      });

      int maxArgName = optionNames.values().stream().mapToInt(String::length).max().orElse(10);

      optionNames.entrySet().stream()
                 .sorted(Map.Entry.comparingByValue())
                 .forEach(entry -> {
                    String arg = entry.getValue();
                    boolean insertSpace = !arg.startsWith("--");
                    if (insertSpace) {
                       System.err.print(" ");
                    }
                    System.err.printf("%1$-" + maxArgName + "s\t", arg);
                    System.err.println(entry.getKey().getDescription());
                 });

      System.err.println("===============================================");
      System.err.println("* = Required");

   }


   private String setValue(String key, String value, List<String> filtered) {
      //look up the option
      NamedOption option = options.get(key.replaceAll("^-+", ""));
      if (option == null) {
         //We have a non-specified argument, so add it to the filter list and check the value.
         filtered.add(key);
         if (Strings.isNullOrBlank(value)) {
            value = "true";
         } else {
            //if one was specified, add it
            filtered.add(value);
         }
         unnamedOptions.put(key.replaceAll("^-+", ""), value);
         return value;

      } else if (option.isBoolean()) {

         if (Strings.isNullOrBlank(value) || value.equalsIgnoreCase("true")) {
            option.setValue(Boolean.toString(true));
            return null;
         } else {
            option.setValue(Boolean.toString(false));
            return null;
         }

      } else {

         if (Strings.isNullOrBlank(value)) {
            throw new CommandLineParserException(key, value);
         }

         option.setValue(value);
         return value;

      }

   }

   /**
    * Determines if an option was set or not.
    *
    * @param optionName the option name
    * @return True if it was set (boolean options must be true), False otherwise
    */
   public boolean isSet(String optionName) {
      optionName = optionName.replaceAll("^-+", "");
      NamedOption option = options.get(optionName);

      if (option == null) {
         return unnamedOptions.containsKey(optionName) &&
            !unnamedOptions.get(optionName).equalsIgnoreCase("false");
      }

      return isSet(option);
   }

   /**
    * Determines if an option was set or not.
    *
    * @param option the option to check
    * @return True if it was set (boolean options must be true), False otherwise
    */
   public boolean isSet(NamedOption option) {
      if (option.isBoolean()) {
         return option.getValue() != null && option.<Boolean>getValue();
      }

      return option.getValue() != null;
   }

   /**
    * Gets the value for the specified option
    *
    * @param <T>        the type of the value for the option
    * @param optionName the name of the option whose value we want to retrieve
    * @return the value of the option or null if not set
    */
   public <T> T get(String optionName) {
      optionName = optionName.replaceAll("^-+", "");
      NamedOption option = options.get(optionName);

      if (option == null) {
         return Cast.as(unnamedOptions.get(optionName));
      }

      if (option.isBoolean()) {
         return option.getValue() == null ? Cast.as(Boolean.FALSE) : option.getValue();
      }

      return option.getValue();
   }

   /**
    * Gets the specified options.
    *
    * @return the specified options
    */
   public Set<NamedOption> getOptions() {
      return Collections.unmodifiableSet(optionSet);
   }

   /**
    * Gets options and values for everything passed in to the command line including unamed options.
    *
    * @return An <code>Map.Entry</code> of options and values for everything passed in to the command line including
    * unamed options.
    */
   public Set<Map.Entry<String, String>> getSetEntries() {
      Set<Map.Entry<String, String>> entries = optionSet.stream()
                                                        .filter(this::isSet)
                                                        .map(no -> Tuple2.of(no.getName(),
                                                                             Converter.convertSilently(no.getValue(),
                                                                                                       String.class)))
                                                        .collect(Collectors.toSet());
      entries.addAll(unnamedOptions.entrySet());
      return entries;
   }

}//END OF CLI
