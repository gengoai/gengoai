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

import com.gengoai.function.Unchecked;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.io.MultiFileWriter;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import com.gengoai.stream.Streams;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;

class OPLFormat implements DocFormat, Serializable {
   private static final long serialVersionUID = 1L;
   private final DocFormat subFormat;

   OPLFormat(DocFormat subFormat) {
      this.subFormat = subFormat;
   }

   @Override
   public DocFormatParameters getParameters() {
      return subFormat.getParameters();
   }

   @Override
   public MStream<Document> read(Resource inputResource) {
      MStream<Document> stream = StreamingContext.get(getParameters().distributed.value())
                                                 .textFile(inputResource)
                                                 .flatMap(line -> Streams.asStream(subFormat.read(new StringResource(
                                                       line))));
      if(getParameters().distributed.value()) {
         stream = stream.cache();
      }
      return stream;
   }

   @Override
   public void write(DocumentCollection corpus, Resource outputResource) throws IOException {
      if(outputResource.isDirectory()) {
         outputResource.mkdirs();
         int nFiles = (int) corpus.size() / 1000;
         try(MultiFileWriter writer = new MultiFileWriter(outputResource, "part-", nFiles)) {
            corpus.parallelStream().forEach(Unchecked.consumer(document -> {
               Resource strResource = new StringResource();
               subFormat.write(document, strResource);
               writer.write(strResource.readToString() + "\n");
               writer.flush();
            }));
         } catch(IOException e) {
            throw e;
         }
      } else {
         try(BufferedWriter writer = new BufferedWriter(outputResource.writer())) {
            for(Document document : corpus) {
               Resource strResource = new StringResource();
               subFormat.write(document, strResource);
               writer.write(strResource.readToString());
               writer.newLine();
            }
         }
      }
   }

   @Override
   public void write(Document document, Resource outputResource) throws IOException {
      Resource strResource = new StringResource();
      subFormat.write(document, strResource);
      outputResource.write(strResource.readToString().replaceAll("\n", "\\\\n"));
   }

}//END OF OPLFormat
