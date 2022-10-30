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

import com.gengoai.Validation;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.io.resource.Resource;
import com.gengoai.reflection.RField;
import com.gengoai.reflection.TypeUtils;
import com.gengoai.string.CharMatcher;
import com.gengoai.string.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>
 * Represents an named command line option. The command line specification is determined by the name and type of the
 * option.
 * <ul>
 * <li>Name is a single character and is boolean: Short form <code>-n</code></li>
 * <li>Name is a multiple characters: long form <code>--n</code></li>
 * </ul>
 * <code>NamedOption</code>s can be built either by passing a <code>Field</code> containing an {@link Option}
 * annotation or by using the builder.
 * </p>
 * <p>
 * Three options are predefined and are automatically added to every command line parser:
 * <ul>
 * <li>{@link NamedOption#HELP}: Shows help if <code>-h</code> or <code>--help</code> is given.</li>
 * <li>{@link NamedOption#CONFIG}: Specifies a configuration resource to load when <code>--config</code> is given.
 * </li>
 * <li>{@link NamedOption#DUMP_CONFIG}: Shows how the current configuration was created with the lineage of each
 * property when <code>--config-explain</code> is given.</li>
 * </ul>
 * </p>
 *
 * @author David B. Bracewell
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public final class NamedOption {
   private final String name;
   private final Type type;
   private final String description;
   private final String[] aliases;
   private final boolean required;
   private final RField field;
   private Object value = null;

   /**
    * Instantiates a new Named option.
    *
    * @param field the field which contains an {@link Option} annotation
    */
   public NamedOption(RField field) {
      Option option = Validation.notNull(field.getAnnotation(Option.class));
      this.field = field;
      this.name = Strings.isNullOrBlank(option.name()) ? field.getName() : option.name();
      Validation.checkArgument(!Strings.isNullOrBlank(this.name) && !CharMatcher.WhiteSpace.matchesAnyOf(this.name),
                               "Option name must have at least one character and must not have a space");
      this.type = field.getType();
      Validation.notNullOrBlank(option.description(), "Description must not be blank");
      this.description = option.description();
      if (!Strings.isNullOrBlank(option.defaultValue())) {
         Config.setProperty(this.name, option.defaultValue());
         this.value = Converter.convertSilently(option.defaultValue(), type);
      } else {
         this.value = null;
      }
      this.aliases = Arrays.stream(option.aliases()).distinct().toArray(String[]::new);
      this.required = option.required();
   }

   private NamedOption(String name,
                       Class<?> type,
                       String description,
                       Object defaultValue,
                       Set<String> aliases,
                       boolean required) {
      Validation.checkArgument(!Strings.isNullOrBlank(name) && !CharMatcher.WhiteSpace.matchesAnyOf(name),
                               "Option name must have at least one character and must not have a space");
      Validation.notNullOrBlank(description, "Description must not be blank");
      this.name = name;
      this.type = type;
      this.description = description;
      this.aliases = aliases == null ? new String[0] : aliases.toArray(new String[0]);
      if (defaultValue != null) {
         Config.setProperty(this.name, Converter.convertSilently(defaultValue, String.class));
         this.value = Converter.convertSilently(defaultValue, type);
      } else {
         this.value = null;
      }
      this.required = required;
      this.field = null;
   }

   /**
    * Gets a builder for NamedOptions
    *
    * @return the named option builder
    */
   public static NamedOptionBuilder builder() {
      return new NamedOptionBuilder();
   }

   /**
    * Converts the aliases into a specification forms, e.g. with "-" or "--" added.
    *
    * @return the alias specifications
    */
   public String[] getAliasSpecifications() {
      if (aliases == null) {
         return new String[0];
      }
      String[] a = new String[aliases.length];
      for (int i = 0; i < aliases.length; i++) {
         a[i] = toSpecificationForm(aliases[i]);
      }
      return a;
   }

   private String toSpecificationForm(String optionName) {
      if (isBoolean() && optionName.length() == 1) {
         return "-" + optionName;
      }
      return "--" + optionName;
   }

   /**
    * Gets specification form for the command line.
    *
    * @return the specification form for the command line
    */
   public String getSpecification() {
      return toSpecificationForm(name);
   }

   /**
    * Gets the value of the option.
    *
    * @param <T> the type parameter
    * @return the value op the option
    */
   public <T> T getValue() {
      return Cast.as(value);
   }

   /**
    * Sets the value of the option.
    *
    * @param optionValue the option value
    */
   void setValue(String optionValue) {
      if (Strings.isNullOrBlank(optionValue) && isBoolean()) {
         this.value = true;
      } else if (!Strings.isNullOrBlank(optionValue)) {
         this.value = Converter.convertSilently(optionValue, type);
      }
   }

   /**
    * Determines if the option is a boolean type or not
    *
    * @return True if the option is a boolean type, False otherwise
    */
   public boolean isBoolean() {
      return TypeUtils.isAssignable(boolean.class, type) || TypeUtils.isAssignable(Boolean.class, type);
   }


   /**
    * Shows help if <code>-h</code> or <code>--help</code> is given.</li>
    */
   public static final NamedOption HELP = NamedOption.builder()
                                                     .name("h")
                                                     .type(Boolean.class)
                                                     .description("Shows this help")
                                                     .alias("help")
                                                     .defaultValue(false)
                                                     .build();

   /**
    * Specifies a configuration resource to load when <code>--config</code> is given.
    */
   public static final NamedOption CONFIG = NamedOption.builder()
                                                       .name("config")
                                                       .type(Resource.class)
                                                       .description(
                                                          "Configuration file that can be specified on the command line.")
                                                       .build();

   /**
    * Shows how the current configuration was created with the lineage of each property when
    * <code>--config-explain</code> is given.
    */
   public static final NamedOption DUMP_CONFIG = NamedOption.builder()
                                                            .name("dump-config")
                                                            .type(Boolean.class)
                                                            .description("Explains how the config values were set.")
                                                            .defaultValue(false)
                                                            .build();

   /**
    * Builder class to create a named options
    */
   public static class NamedOptionBuilder {
      private String name;
      private Class<?> type;
      private String description;
      private Object defaultValue;
      private Set<String> aliases = new HashSet<>();
      private boolean required;

      /**
       * Instantiates a new Named option builder.
       */
      NamedOptionBuilder() {
      }

      /**
       * Adds an alias for this option
       *
       * @param alias the alias to add
       * @return the named option builder
       */
      public NamedOptionBuilder alias(String alias) {
         this.aliases.add(alias);
         return this;
      }


      /**
       * Builds the named option
       *
       * @return the named option
       */
      public NamedOption build() {
         return new NamedOption(name, type, description, defaultValue, aliases, required);
      }

      /**
       * Sets the default value for the option
       *
       * @param defaultValue the default value
       * @return the named option builder
       */
      public NamedOptionBuilder defaultValue(Object defaultValue) {
         this.defaultValue = defaultValue;
         return this;
      }

      /**
       * Sets the description of the option
       *
       * @param description the description
       * @return the named option builder
       */
      public NamedOptionBuilder description(String description) {
         this.description = description;
         return this;
      }

      /**
       * Sets the name of the option
       *
       * @param name the name
       * @return the named option builder
       */
      public NamedOptionBuilder name(String name) {
         this.name = name;
         return this;
      }

      /**
       * Sets whether or not the option is required
       *
       * @param required True - the option is required to be set
       * @return the named option builder
       */
      public NamedOptionBuilder required(boolean required) {
         this.required = required;
         return this;
      }

      public String toString() {
         return "NamedOptionBuilder(name=" + this.name + ", type=" + this.type + ", description=" + this.description + ", defaultValue=" + this.defaultValue + ", aliases=" + this.aliases + ", required=" + this.required + ")";
      }

      /**
       * Sets the type of the option
       *
       * @param type the type
       * @return the named option builder
       */
      public NamedOptionBuilder type(Class<?> type) {
         this.type = type;
         return this;
      }
   }
}//END OF NamedOption
