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

import com.gengoai.hermes.RelationType;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.tuple.Tuple2;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

@Value
@Builder
class RelationProvider implements Serializable {
   static long serialVersionUID = 1L;
   boolean bidirectional;
   @NonNull
   String name;
   @Singular("requires")
   Set<String> required;
   @NonNull
   Tuple2<String, LyreExpression> source;
   @NonNull
   Tuple2<String, LyreExpression> target;
   LyreExpression sourceFilter;
   LyreExpression targetFilter;
   @NonNull
   RelationType type;
   @NonNull
   String value;

}//END OF RelationProvider
