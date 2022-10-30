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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;

/**
 * <p>A tag which is represented as a string. Care must be taken in that different string variations will represent
 * different tags.</p>
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@Value
public class StringTag implements Tag, Serializable {
   private static final long serialVersionUID = 1L;
   @JsonValue
   String tag;

   /**
    * Default Constructor
    *
    * @param tag The tag name
    */
   public StringTag(String tag) {
      this.tag = Validation.notNullOrBlank(tag, "Tag must not be null or blank.");
   }

   @Override
   public boolean isInstance(Tag tag) {
      return equals(tag);
   }

   @Override
   public String name() {
      return toString();
   }

   @Override
   public String toString() {
      return tag;
   }

}//END OF StringTag
