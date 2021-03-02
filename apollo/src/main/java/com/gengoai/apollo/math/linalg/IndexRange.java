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

package com.gengoai.apollo.math.linalg;

import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.util.stream.Stream;

/**
 * <p>An contiguous range of {@link Index}.</p>
 */
public interface IndexRange extends Iterable<Index> {

   /**
    * Is the given coordinate contained within this range
    *
    * @param o the coordinate
    * @return True - if the coordinate is contained within this range.
    */
   boolean contains(@NonNull Coordinate o);

   /**
    * The lower index of the range.
    *
    * @return the lower index of the range.
    */
   Index lower();

   /**
    * The upper index of the range.
    *
    * @return the upper index of the range.
    */
   Index upper();

   /**
    * The Shape of the IndexRange
    *
    * @return the Shape of the IndexRange
    */
   Shape shape();

   /**
    * Creates a Stream of the Index in this range
    *
    * @return the stream of the Index in this range
    */
   default Stream<Index> stream(){
      return Streams.asStream(this);
   }

}//END OF IndexRange
