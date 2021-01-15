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

/**
 * An empty resource
 *
 * @author David B. Bracewell
 */
public class EmptyResource extends BaseResource implements NonTraversableResource {
   private static final long serialVersionUID = 1L;

   /**
    * The singleton instance
    */
   public static final EmptyResource INSTANCE = new EmptyResource();

   @Override
   public Resource append(byte[] byteArray) throws IOException {
      throw new IllegalStateException("This is resource cannot be written to.");
   }

   @Override
   public boolean exists() {
      return false;
   }

   @Override
   public boolean canWrite() {
      return false;
   }

   @Override
   public boolean canRead() {
      return false;
   }

}//END OF EmptyResource
