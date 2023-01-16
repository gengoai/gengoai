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

package com.gengoai.hermes.format;

import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.stream.Stream;

public class TwinsightsFormat extends WholeFileTextFormat {
   private final DocFormatParameters parameters;

   public TwinsightsFormat(DocFormatParameters parameters) {
      this.parameters = parameters;
   }


   @Override
   public DocFormatParameters getParameters() {
      return parameters;
   }

   @Override
   public void write(DocumentCollection documentCollection, Resource outputResource) throws IOException {

   }

   @Override
   public void write(Document document, Resource outputResource) throws IOException {

   }

   @Override
   protected Stream<Document> readSingleFile(String content) {
      try {
         JsonEntry e = Json.parse(content);
         Document doc = getParameters().getDocumentFactory().create(e.getStringProperty("content"));
         doc.setId(e.getStringProperty("id"));
         doc.put(Types.AUTHOR, e.getStringProperty("user_id"));
         return Stream.of(doc);
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   @MetaInfServices
   public static class Provider implements DocFormatProvider {

      @Override
      public DocFormat create(DocFormatParameters parameters) {
         return new TwinsightsFormat(parameters);
      }

      @Override
      public String getName() {
         return "TWIN";
      }

      @Override
      public boolean isWriteable() {
         return false;
      }
   }
}
