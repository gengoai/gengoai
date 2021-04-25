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

import com.gengoai.Language;
import com.gengoai.Validation;
import com.gengoai.conversion.Converter;
import com.gengoai.lucene.AnalyzerUtils;
import lombok.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.util.Locale;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class TextFieldProcessor extends BaseStoreOptionalFieldProcessor {
   private static final long serialVersionUID = 1L;
   private final Language language;
   @Getter
   @Setter
   private boolean removeStopWords = true;

   public TextFieldProcessor(String fieldName,
                             boolean isStored,
                             @NonNull Language language,
                             boolean removeStopWords) {
      super(Validation.notNullOrBlank(fieldName), isStored);
      this.language = language;
      this.removeStopWords = removeStopWords;
   }

   @Override
   public Analyzer getAnalyzer() {
      return AnalyzerUtils.analyzerForLanguage(language, removeStopWords);
   }

   @Override
   @SneakyThrows
   public void singleValue(@NonNull Document document, @NonNull Object value) {
      String str;
      if (value instanceof CharSequence) {
         str = value.toString();
      } else {
         str = Converter.convert(value, String.class);
      }
      document.add(new TextField(getFieldName(), str, isStored() ? Field.Store.YES : Field.Store.NO));
   }

}//END OF StringFieldProcessor
