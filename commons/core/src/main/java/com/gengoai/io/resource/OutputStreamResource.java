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
import java.io.OutputStream;

/**
 * Resource that wraps an <Code>OutputStream</Code>
 *
 * @author David B. Bracewell
 */
public class OutputStreamResource extends BaseResource implements WriteOnlyResource, NonTraversableResource {

   private static final long serialVersionUID = 1233692902217463488L;
   private final OutputStream outputStream;

   /**
    * Instantiates a new Output stream resource.
    *
    * @param stream the output stream to wrap
    */
   public OutputStreamResource(OutputStream stream) {
      this.outputStream = stream;
   }

   @Override
   public boolean exists() {
      return true;
   }

   @Override
   public OutputStream createOutputStream() throws IOException {
      return outputStream;
   }

   @Override
   public Resource append(byte[] byteArray) throws IOException {
      outputStream.write(byteArray);
      return this;
   }

}//END OF OutputStreamResource
