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

import com.gengoai.config.Preloader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>A enum like object that can have elements created at runtime as needed and which have a parent associated with
 * them. As with EnumValues, elements are singleton objects and can have their equality safely checked using the
 * <code>==</code> operator. Their implementation of {@link Tag#isInstance(Tag)} returns true if the element is equal
 * to or a descendant of the tag being compared against. Elements can have their parents assigned at later time as long
 * up until a non-null parent has been set.</p>
 *
 * <p>The python script in the mango tools directory (<code>tools/enumGen.py</code>) bootstraps the creation of basic
 * HierarchicalEnumValue. As with enum values the names associated with EnumValues are normalized to be uppercase and
 * have all whitespace replaced by underscores with consecutive whitespace becoming a  single underscore.</p>
 *
 * <p>Examples of common usage patterns for HierarchicalEnumValue types generated using <code>tools/enumGen.py</code>
 * are as follows:</p>
 *
 * <pre>
 * {@code
 *    //Enum values can be retrieved or created using the create method.
 *    MyEnum animal = MyEnum.create("animal");
 *    MyEnum dog = MyEnum.create("dog", animal);
 *    MyEnum pug = MyEnum.create("pug", dog);
 *
 *    MyEnum thing = MyEnum.create("thing");
 *
 *    //Now we want to set the parent of animal to thing. This is ok, because we did not set the parent yet, and
 *    // do not have one defined via a configuration property.
 *    MyEnum.create("animal", thing);
 *
 *    //Will evaluate isInstance using the hierarchy
 *    boolean isAnimal = pug.isInstance(animal);
 *
 *    //A leaf element is one that doesn't have children
 *    boolean isLeaf = pug.isLeaf();
 *
 *    //A root element is one that doesn't have a parent.
 *    //Note: this can change since parents can be updated.
 *    boolean isRoot = thing.isRoot();
 *
 *    //Can get the children of an element using the getChildren method
 *    List<MyEnum> typesOfAnimals = animal.getChildren();
 *
 *    //Can emulate Java enum using the valueOf method
 *    MyEnum cat = MyEnum.valueOf("cat");
 *
 *    //Can retrieve all instances in an unmodifiable set using the values method
 *    Set<MyEnum> allThings = MyEnum.values();
 *
 *    //Will result in [dog, animal, thing]
 *    List<MyEnum> ancestors = pug.getAncestors();
 * }*
 * </pre>
 *
 *
 * <p> If your HierarchicalEnumValue stores other information and want to ensure that declared instances are loaded in
 * memory you can use Mango's {@link Preloader} to load during application startup. </p>
 *
 * @author David B. Bracewell
 */
public abstract class HierarchicalEnumValue<T extends HierarchicalEnumValue<T>> extends EnumValue<T> {
   public static final char SEPARATOR = '$';
   private final int depth;

   /**
    * Instantiates a new enum value.
    *
    * @param name the name of the enum value
    */
   protected HierarchicalEnumValue(String name) {
      super(name);
      this.depth = (int) name().chars().filter(i -> i == SEPARATOR).count();
   }


   /**
    * Gets the child enum values for this value
    *
    * @return the list of child enum values
    */
   public List<T> children() {
      final String target = name() + SEPARATOR;
      if (this == registry().ROOT) {
         return registry().values()
                          .parallelStream()
                          .filter(v -> v.parent() == registry().ROOT)
                          .collect(Collectors.toList());
      }
      return registry().values()
                       .parallelStream()
                       .filter(v -> v.depth() == (depth() + 1) && v.name().startsWith(target))
                       .collect(Collectors.toList());
   }

   /**
    * The depth of the enum value in the hierarchy
    *
    * @return the depth
    */
   public int depth() {
      return depth;
   }

   @Override
   public final boolean isInstance(Tag value) {
      return value != null
            && (value == registry().ROOT ||
            value.name().equals(name()) ||
            name().startsWith(value.name() + SEPARATOR));
   }

   /**
    * Checks if this enum value is a leaf
    *
    * @return True if a leaf, False otherwise
    */
   public boolean isLeaf() {
      return this != registry().ROOT && children().isEmpty();
   }

   /**
    * Checks if this enum value is a root
    *
    * @return True if a root, False otherwise
    */
   public boolean isRoot() {
      return parent() == null;
   }

   @Override
   public String label() {
      int index = name().lastIndexOf(SEPARATOR);
      if (index > 0) {
         return name().substring(index + 1);
      }
      return name();
   }

   @Override
   public T parent() {
      if (this == registry().ROOT) {
         return null;
      }
      int idx = name().lastIndexOf(SEPARATOR);
      if (idx < 0) {
         return registry().ROOT;
      }
      return registry().make(name().substring(0, idx));
   }

   /**
    * Generates an array of string representing the path of this enum value in the hierarchy
    *
    * @return the path
    */
   public String[] path() {
      return name().split(Character.toString(SEPARATOR));
   }


   @Override
   protected abstract HierarchicalRegistry<T> registry();

}//END OF HierarchicalEnum
