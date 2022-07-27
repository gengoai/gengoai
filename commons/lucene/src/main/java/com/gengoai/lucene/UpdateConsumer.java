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

package com.gengoai.lucene;

import com.gengoai.LogUtils;
import lombok.extern.java.Log;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.util.function.Consumer;

@Log
class UpdateConsumer implements Consumer<IndexDocument> {
   private final DocumentUpdater documentProcessor;
   private final IndexConfig config;
   private final IndexWriter writer;
   private final String idField;

   UpdateConsumer(DocumentUpdater documentProcessor,
                  IndexConfig config,
                  IndexWriter writer,
                  String idField) {
      this.documentProcessor = documentProcessor;
      this.writer = writer;
      this.config = config;
      this.idField = idField;
   }

   @Override
   public void accept(IndexDocument document) {
      try {
         if (documentProcessor.update(document)) {
            writer.updateDocument(new Term(idField, document.get(idField).asString()),
                                  document.asIndexableFields(config));
         }
      } catch (IOException e) {
         LogUtils.logSevere(log, e);
         throw new RuntimeException(e);
      }
   }

}//END OF UpdateConsumer
