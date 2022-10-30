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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 * Iterates over the children of a given resource.
 *
 * @author David B. Bracewell
 */
public class ResourceChildIterator implements Iterator<Resource> {

   private final Pattern filePattern;
   private final Queue<Resource> queue = new LinkedList<>();
   private final boolean recursive;

   /**
    * Instantiates a new Child iterator.
    *
    * @param startingPoint the starting point
    * @param filePattern   the file pattern
    * @param recursive     the recursive
    */
   public ResourceChildIterator(Resource startingPoint, Pattern filePattern, boolean recursive) {
      this.filePattern = filePattern;
      queue.addAll(startingPoint.getChildren(filePattern, false));
      this.recursive = recursive;
      advance();
   }

   private void advance() {
      if(queue.isEmpty()) {
         return;
      }
      if(queue.peek().isDirectory()) {
         if(recursive) {
            queue.addAll(queue.peek().getChildren(filePattern, false));
         }
      }
   }

   @Override
   public boolean hasNext() {
      return !queue.isEmpty();
   }

   @Override
   public Resource next() {
      if(queue.isEmpty()) {
         throw new NoSuchElementException();
      }
      Resource next = queue.remove();
      advance();
      return next;
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException();
   }

}//END OF ResourceChildIterator
