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
 *
 */

package com.gengoai.specification;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * @author David B. Bracewell
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class TestBean implements Specifiable {
   @Protocol
   private String protocol;
   @SubProtocol(0)
   private String sp1;
   @SubProtocol
   private List<String> sp;
   @Path
   private String path;
   @QueryParameter("q")
   private int qp1;
   @QueryParameter("f")
   private float qp2;
   @QueryParameter("b")
   private List<Boolean> qp3;


   @Override
   public String getSchema() {
      return "testBean";
   }

   @Override
   public String toString(){
      return toSpecification();
   }

}//END OF TestBean
