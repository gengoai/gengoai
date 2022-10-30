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

package com.gengoai.hermes.tools;

import com.gengoai.hermes.workflow.ActionDescription;
import com.gengoai.string.Strings;

import java.util.ServiceLoader;

/**
 * @author David B. Bracewell
 */
public class ListWorkflowActions extends HermesCLI {
   public static void main(String[] args) {
      new ListWorkflowActions().run(args);
   }

   @Override
   protected void programLogic() throws Exception {
      final String EQUALS_SEP = Strings.repeat('=', 80);
      final String DASH_SEP = Strings.repeat('-', 80);
      for (ActionDescription description : ServiceLoader.load(ActionDescription.class)) {
         System.out.println(EQUALS_SEP);
         System.out.println(description.name());
         System.out.println(EQUALS_SEP);
         System.out.println(Strings.lineBreak(description.description(), 80).strip());
         System.out.println(DASH_SEP);
         System.out.println();
      }
   }
}//END OF ListWorkflowActions
