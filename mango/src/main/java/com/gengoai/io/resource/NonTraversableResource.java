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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Defines a resources whose hierarchy is not traversable.
 *
 * @author David B. Bracewell
 */
public interface NonTraversableResource extends Resource {

   @Override
   default Resource getChild(String relativePath) {
      return EmptyResource.INSTANCE;
   }

   @Override
   default Resource getParent() {
      return EmptyResource.INSTANCE;
   }

   @Override
   default Iterator<Resource> childIterator(String pattern, boolean recursive) {
      return Collections.emptyIterator();
   }

   @Override
   default List<Resource> getChildren() {
      return Collections.emptyList();
   }

   @Override
   default List<Resource> getChildren(Pattern pattern, boolean recursive) {
      return Collections.emptyList();
   }

   @Override
   default List<Resource> getChildren(String pattern) {
      return Collections.emptyList();
   }

   @Override
   default List<Resource> getChildren(String pattern, boolean recursive) {
      return Collections.emptyList();
   }

   @Override
   default List<Resource> getChildren(boolean recursive) {
      return Collections.emptyList();
   }

   @Override
   default Iterator<Resource> childIterator(boolean recursive) {
      return Collections.emptyIterator();
   }

}//END OF NonTraversableResource
