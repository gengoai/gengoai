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
import com.gengoai.io.resource.Resource;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * <p>Format Name: <b>hjson</b></p>
 * <p>Reads and writes documents in Hermes Json format created when using {@link Document#toJson()}</p>
 */
public class HermesJsonFormat extends WholeFileTextFormat implements OneDocPerFileFormat, Serializable {
   private static final long serialVersionUID = 1L;
   private final DocFormatParameters parameters;

   HermesJsonFormat(DocFormatParameters parameters) {
      this.parameters = parameters;
   }

   @Override
   public DocFormatParameters getParameters() {
      return parameters;
   }

   @Override
   protected Stream<Document> readSingleFile(String content) {
      return Stream.of(Document.fromJson(content));
   }

   @Override
   public void write(Document document, Resource outputResource) throws IOException {
      outputResource.write(document.toJson());
   }

   /**
    * The type Provider.
    */
   @MetaInfServices
   public static class Provider implements DocFormatProvider {

      @Override
      public DocFormat create(DocFormatParameters parameters) {
         return new HermesJsonFormat(parameters);
      }

      @Override
      public String getName() {
         return "HJSON";
      }

      @Override
      public boolean isWriteable() {
         return true;
      }
   }

}//END OF HermesJsonFormat
