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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Resource that wraps standard out
 *
 * @author David B. Bracewell
 */
public class StdoutResource extends BaseResource implements NonTraversableResource, WriteOnlyResource {
   private static final long serialVersionUID = 1L;

   @Override
   public OutputStream createOutputStream() throws IOException {
      return new BufferedOutputStream(System.out);
   }

   @Override
   public boolean exists() {
      return true;
   }

   @Override
   public Resource append(byte[] byteArray) throws IOException {
      try(OutputStream os = createOutputStream()) {
         os.write(byteArray);
      }
      return this;
   }

   @Override
   public boolean mkdir() {
      return true;
   }

   @Override
   public boolean mkdirs() {
      return true;
   }

   @Override
   public Resource getParent() {
      return this;
   }

   @Override
   public Resource getChild(String relativePath) {
      return this;
   }

}//END OF StdoutResource
