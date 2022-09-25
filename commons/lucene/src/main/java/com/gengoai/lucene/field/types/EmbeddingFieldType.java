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

package com.gengoai.lucene.field.types;

import com.gengoai.collection.counter.Counter;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.lucene.field.EmbeddingField;
import com.gengoai.lucene.field.Fields;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.kohsuke.MetaInfServices;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Log
@MetaInfServices
public class EmbeddingFieldType extends FieldType {

   public EmbeddingFieldType() {
      super(float[].class);
   }

   @Override
   public Analyzer getAnalyzer() {
      return new LuceneEmbeddingAnalyzer();
   }

   @Override
   protected Collection<IndexableField> processImpl(Object value, String outField, boolean store) {
      return Collections.singletonList(new EmbeddingField(outField, value.toString()));
   }

}
