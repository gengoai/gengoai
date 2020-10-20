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

package com.gengoai.hermes.format.conll;

import com.gengoai.hermes.*;
import com.gengoai.hermes.format.CoNLLColumnProcessor;
import com.gengoai.hermes.format.CoNLLRow;
import com.gengoai.json.Json;
import com.gengoai.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
@MetaInfServices
public class CategoryProcessor implements CoNLLColumnProcessor {
   @Override
   public String getFieldName() {
      return "CATEGORY";
   }

   @Override
   public void processInput(Document document, List<CoNLLRow> documentRows, Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {
      documentRows
            .stream()
            .filter(row -> row.hasOther("CATEGORY"))
            .forEach(row -> document.annotation(row.getAnnotationID())
                                    .ifNotEmpty(a -> {
                                       Set<BasicCategories> cats = Types.CATEGORY.decode(row.getOther("CATEGORY"));
                                       a.put(Types.CATEGORY, cats);
                                    }));
   }

   @Override
   public String processOutput(HString document, Annotation token, int index) {
      return Json.dumps(token.categories());
   }
}//END OF CategoryProcessor
