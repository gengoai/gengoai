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

package com.gengoai.documentdb;

import lombok.NoArgsConstructor;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor
public class DocumentDBException extends Exception {


   public DocumentDBException(String message) {
      super(message);
   }

   public DocumentDBException(String message, Throwable cause) {
      super(message, cause);
   }


   public DocumentDBException(Throwable cause) {
      super(cause);
   }

   public DocumentDBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }

   public static DocumentDBException tableAlreadyExists(String tableName) {
      return new DocumentDBException(String.format("A table with the name '%s' already exists", tableName));
   }

   public static DocumentDBException tableDoesNotExitException(String tableName) {
      return new DocumentDBException(String.format("Table does not exist: '%s'", tableName));
   }

   public static DocumentDBException tableNameMustNotBeBlank() {
      return new DocumentDBException("A table name must not be blank or null");
   }
}//END OF DocumentDBException
