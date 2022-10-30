/*
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

package com.gengoai.swing.component.listener;

import com.gengoai.collection.tree.Span;
import com.gengoai.string.Strings;
import com.gengoai.swing.component.StyledSpan;

import java.util.EventListener;

/**
 * Auto expand a selection based on predefined criteria.
 */
@FunctionalInterface
public interface AutoExpandAction extends EventListener {

   /**
    * Expands selection to include contiguous non-whitespace
    */
   AutoExpandAction contiguousNonWhitespace = (s, e, t, m) -> {
      if(m == null || m.start() > s) {
         return Strings.expand(t, s, e);
      }
      return Span.of(m.start(), m.end());
   };

   /**
    * Performs no expansion returning the current selection
    */
   AutoExpandAction noExpansion = (s, e, t, m) -> {
      return Span.of(s, e);
   };

   /**
    * Attempt to expand the selection defined using the given start and end position within the given text.
    *
    * @param start        the start
    * @param end          the end
    * @param text         the text
    * @param matchingSpan the matching style span in the selection or null if none
    * @return the expansion
    */
   Span expand(int start, int end, String text, StyledSpan matchingSpan);

}//END OF AutoExpandAction
