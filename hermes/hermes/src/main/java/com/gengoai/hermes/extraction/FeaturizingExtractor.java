/*
 * (c) 2005 David B. Bracewell
 *
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
 *
 */

package com.gengoai.hermes.extraction;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.apollo.feature.Featurizer;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.extraction.lyre.LyreExpression;

/**
 * Combines an {@link Extractor} with an Apollo <code>Featurizer</code> allowing for the output of the extractor to be
 * directly used as features for machine learning.
 *
 * @author David B. Bracewell
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = LyreExpression.class)
public abstract class FeaturizingExtractor extends Featurizer<HString> implements Extractor {
   private static final long serialVersionUID = 1L;

}//END OF Extractor
