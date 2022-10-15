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

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.SQLiteDataSet;
import com.gengoai.application.CommandLineApplication;
import com.gengoai.application.Option;
import com.gengoai.io.resource.Resource;

public class ExplainDataSet extends CommandLineApplication {

   @Option(description = "The dataset")
   Resource dataSet;

   @Override
   protected void programLogic() throws Exception {
      DataSet data = new SQLiteDataSet(dataSet);

      System.out.println("File: '" + dataSet + "'");
      System.out.println("Size: " + data.size());

      System.out.println("Sources");
      System.out.println("=================================");
      for (String source : data.getMetadata().keySet()) {
         System.out.println(source + " : " + data.getMetadata().get(source));
      }

   }


   public static void main(String[] args) {
      new ExplainDataSet().run(args);
   }

}
