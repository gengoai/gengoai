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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;

import java.util.Arrays;

/**
 * <p>A Tag represents a label on an object. Tags allow for instanceOf checks using {@link #isInstance(Tag)} and {@link
 * #isInstance(Tag...)}. Individual implementations may define the <code>isInstance</code> method to take into account a
 * hierarchy or other attributes that define a tag.</p>
 *
 * @author David B. Bracewell
 */
@JsonDeserialize(as = StringTag.class)
public interface Tag {

   /**
    * Determines if this tag is an instance of a given tag.
    *
    * @param tag The given tag to check if this one is an instance of
    * @return True if this tag is an instance of the given tag
    */
   default boolean isInstance(@NonNull Tag tag) {
      if(getClass().isInstance(tag)) {
         if(name().equalsIgnoreCase(tag.name())) {
            return true;
         }
         Tag p = parent();
         while(p != null) {
            if(p.name().equalsIgnoreCase(tag.name())) {
               return true;
            }
            p = p.parent();
         }
         return false;
      }
      return false;
   }

   /**
    * Determines if this tag is an instance of any of the given tags.
    *
    * @param tags the tags to check against
    * @return True if this tag is an instance of any one of the given tags
    */
   default boolean isInstance(@NonNull Tag... tags) {
      return Arrays.stream(Validation.notNull(tags)).anyMatch(this::isInstance);
   }

   /**
    * Gets the label associated with the tag. In most cases the label is the same as the name, but when tags are
    * defined using a {@link HierarchicalEnumValue} the label is the leaf node name without the full path.
    *
    * @return the label of the tag
    */
   default String label() {
      return name();
   }

   /**
    * The name of the tag.
    *
    * @return The name of the tag
    */
   String name();

   /**
    * Gets the parent of this tag
    *
    * @return the parent or null if it has no parent
    */
   default Tag parent() {
      return null;
   }

}//END OF Tag
