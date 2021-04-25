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

package com.gengoai.lucene.processor;

import com.gengoai.Validation;
import com.gengoai.conversion.Converter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.StoredField;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class DoubleFieldProcessor extends BaseStoreOptionalFieldProcessor {
   private static final long serialVersionUID = 1L;

   public DoubleFieldProcessor(String fieldName, boolean stored) {
      super(Validation.notNullOrBlank(fieldName), stored);
   }

   @Override
   public Analyzer getAnalyzer() {
      return new KeywordAnalyzer();
   }

   @Override
   @SneakyThrows
   public void singleValue(@NonNull Document document, @NonNull Object value) {
      Double number = Converter.convert(value, Double.class);
      document.add(new DoublePoint(getFieldName(), number));
      if (isStored()) {
         document.add(new StoredField(getFieldName() + "_stored", number));
      }
   }

}//END OF DoubleFieldProcessor
