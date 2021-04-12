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

package com.gengoai.swing.component;

import com.gengoai.collection.tree.SimpleSpan;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The type Styled span.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StyledSpan extends SimpleSpan {
   private static final long serialVersionUID = 1L;
   /**
    * The Style.
    */
   public final String style;
   /**
    * The Label.
    */
   public final String label;

   /**
    * Instantiates a new Simple span.
    *
    * @param start the start
    * @param end   the end
    * @param style the style
    * @param label the label
    */
   public StyledSpan(int start, int end, String style, String label) {
      super(start, end);
      this.style = style;
      this.label = label;
   }

}//END OF StyledSpan
