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

package com.gengoai.hermes.extraction.caduceus;

import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.AttributeMap;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

@Value
@Builder
class AnnotationProvider implements Serializable {
   AttributeMap attributeMap;
   @NonNull
   String capture;
   @Singular("requires")
   @NonNull
   Set<String> required;
   @NonNull
   AnnotationType type;
   LyreExpression expand;
}//END OF AnnotationProvider
