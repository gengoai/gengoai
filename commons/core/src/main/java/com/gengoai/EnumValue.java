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
 *
 */

package com.gengoai;

import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.application.CommandLineParser;
import com.gengoai.application.NamedOption;
import com.gengoai.config.Preloader;
import com.gengoai.conversion.Cast;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * <p>A enum like object that can have elements created at runtime as needed. Elements are singleton objects and can
 * have their equality safely checked using the <code>==</code> operator. A python script in the mango tools directory
 * (<code>tools/enumGen.py</code>) bootstraps the creation of basic EnumValues. Names associated with EnumValues are
 * normalized to be uppercase and have all whitespace replaced by underscores with consecutive whitespace becoming a
 * single underscore. Names must not contain a period (.) or be blank.</p>
 *
 * <p>Examples of common usage patterns for EnumValue types generated using <code>tools/enumGen.py</code> are as
 * follows:</p>
 *
 * <pre>
 * {@code
 *    //Enum values can be retrieved or created using the create method.
 *    MyEnum red = MyEnum.create("red");
 *    MyEnum blue = MyEnum.create("blue);
 *
 *    //Can emulate Java enum using the valueOf method
 *    MyEnum green = MyEnum.valueOf("gReEn");
 *
 *    //Can retrieve all instances in an unmodifiable set using the values method
 *    Set<MyEnum> allColors = MyEnum.values();
 * }*
 * </pre>
 *
 *
 * <p>
 * If your EnumValue stores other information and want to ensure that declared instances are loaded in memory you can
 * use Mango's {@link Preloader} to load during application startup.
 * </p>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public abstract class EnumValue<T extends EnumValue<T>> implements Tag, Serializable, Cloneable, Comparable<T> {
   private static final long serialVersionUID = 1L;
   private final String canonicalName;
   @JsonValue
   private final String name;

   /**
    * The entry point of application.
    *
    * @param args the input arguments
    * @throws Exception the exception
    */
   public static void main(String[] args) throws Exception {
      CommandLineParser parser = new CommandLineParser();
      parser.addOption(NamedOption.builder()
                                  .type(String.class)
                                  .name("className")
                                  .description("The class name.")
                                  .alias("c")
                                  .required(true)
                                  .build());
      parser.addOption(NamedOption.builder()
                                  .type(String.class)
                                  .name("packageName")
                                  .description("The package name to write the class to.")
                                  .alias("p")
                                  .required(true)
                                  .build());
      parser.addOption(NamedOption.builder()
                                  .type(Resource.class)
                                  .description("The src directory to write class to.")
                                  .name("src")
                                  .alias("o")
                                  .required(true)
                                  .build());
      parser.addOption(NamedOption.builder()
                                  .type(boolean.class)
                                  .defaultValue(false)
                                  .name("t")
                                  .description("True - create a hierarchical enum")
                                  .build());
      parser.parse(args);
      Resource template = parser.get("t")
                          ? Resources.fromClasspath("com/gengoai/TemplateHierarchicalEnumValue.java")
                          : Resources.fromClasspath("com/gengoai/TemplateEnumValue.java");

      String className = parser.get("className");
      String packageName = parser.get("packageName");
      if(packageName.endsWith(";")) {
         packageName = packageName.substring(0, packageName.length() - 1);
      }
      String str = template.readToString();
      str = str.replaceAll("\\$\\{TEMPLATE}", className);
      str = "package " + packageName + ";\n\n" + str;
      Resource out = parser.get("src");
      out = out.getChild(packageName.replace('.', '/'));
      out.mkdirs();
      out = out.getChild(className + ".java");
      if(out.exists()) {
         throw new IllegalStateException(out.path() + " already exists, please delete first!");
      }
      out.write(str);
   }

   /**
    * Instantiates a new enum value.
    *
    * @param name the name of the enum value
    */
   protected EnumValue(String name) {
      this.name = name;
      this.canonicalName = getClass().getCanonicalName() + "." + this.name;
   }

   /**
    * <p>Retrieves the canonical name of the enum value, which is the canonical name of the enum class and the
    * specified name of the enum value.</p>
    *
    * @return the canonical name of the enum value
    */
   public final String canonicalName() {
      return canonicalName;
   }

   @Override
   protected final Object clone() throws CloneNotSupportedException {
      super.clone();
      return this;
   }

   @Override
   public int compareTo(T o) {
      return canonicalName().compareTo(o.canonicalName());
   }

   @Override
   public final boolean equals(Object obj) {
      return obj instanceof EnumValue && canonicalName().equals(Cast.<EnumValue>as(obj).canonicalName());
   }

   @Override
   public final int hashCode() {
      return canonicalName().hashCode();
   }

   @Override
   public boolean isInstance(Tag value) {
      return this.equals(value);
   }

   @Override
   public String name() {
      return name;
   }

   protected Object readResolve() throws ObjectStreamException {
      return registry().make(name());
   }

   /**
    * Gets the registry used by this value
    *
    * @return the registry
    */
   protected abstract Registry<T> registry();

   @Override
   public final String toString() {
      return name;
   }

}//END OF EnumValue
