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

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource that wraps an <code>InputStream</code>
 *
 * @author David B. Bracewell
 */
public class InputStreamResource extends BaseResource implements ReadOnlyResource, NonTraversableResource {

   private static final long serialVersionUID = -4341744444291053097L;
   private final InputStream inputStream;

   /**
    * Instantiates a new Input stream resource.
    *
    * @param stream the input stream to wrap
    */
   public InputStreamResource(InputStream stream) {
      this.inputStream = stream;
   }

   @Override
   public InputStream createInputStream() throws IOException {
      return inputStream;
   }

   @Override
   public boolean exists() {
      return true;
   }

}//END OF InputStreamResource
