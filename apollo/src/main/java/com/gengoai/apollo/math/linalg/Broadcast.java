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

/**
 * <p>Describes the type of broadcasting required to complete a mapping operation</p>
 */
public enum Broadcast {
   /**
    * Empty LHS and Empty RHS
    */
   EMPTY,
   /**
    * Scalar RHS
    */
   SCALAR,
   /**
    * Vector LHS and Vector RHS with same total length.
    */
   VECTOR,
   /**
    * Matrix LHS and Matrix RHS with same shape.
    */
   MATRIX,
   /**
    * Tensor LHS and Tensor RHS with same shape.
    */
   TENSOR,
   /**
    * Matrix LHS and Vector RHS with same number of columns
    */
   MATRIX_ROW,
   /**
    * Matrix LHS and Vector RHS with same number of rows
    */
   MATRIX_COLUMN,
   /**
    * Tensor LHS and Vector RHS with same number of columns
    */
   TENSOR_ROW,
   /**
    * Tensor LHS and Vector RHS with same number of rows
    */
   TENSOR_COLUMN,
   /**
    * Tensor LHS and Tensor RHS with same number of kernels but differing number of channels
    */
   TENSOR_CHANNEL,
   /**
    * Tensor LHS and Tensor RHS with same number of channels but differing number of kernels
    */
   TENSOR_KERNEL,
   /**
    * Cannot Broadcast
    */
   ERROR;
}//END OF Broadcast
