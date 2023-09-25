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

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@Value
public class SearchResults implements Iterable<ScoredDocument> {
    public long totalHits;
    @Getter
    public Stream<ScoredDocument> stream;

    public SearchResults(long totalHits, @NonNull Stream<ScoredDocument> stream) {
        this.totalHits = totalHits;
        this.stream = stream;
    }

    @Override
    public Iterator<ScoredDocument> iterator() {
        return stream.iterator();
    }
}//END OF SearchResults
