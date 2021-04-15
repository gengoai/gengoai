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

package com.gengoai.stream.local;

import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableSupplier;
import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.util.stream.Stream;

/**
 * The type Local reusable m stream.
 *
 * @param <T> the type parameter
 */
public class LocalReusableMStream<T> extends AbstractLocalMStream<T> {
   private static final long serialVersionUID = 1L;
   private final Stream<T> stream;

   /**
    * Instantiates a new Local reusable m stream.
    *
    * @param stream the stream
    */
   public LocalReusableMStream(@NonNull Stream<T> stream) {
      this.stream = Cast.as(stream);
   }

   /**
    * Instantiates a new Local reusable m stream.
    *
    * @param stream the stream
    */
   public LocalReusableMStream(@NonNull SerializableSupplier<Stream<T>> stream) {
      this.stream = Streams.reusableStream(stream);
   }

   @Override
   public Stream<T> javaStream() {
      return stream;
   }
}//END OF LocalReusableMStream
