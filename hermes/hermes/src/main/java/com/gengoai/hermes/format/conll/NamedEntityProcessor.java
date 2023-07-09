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

package com.gengoai.hermes.format.conll;

import com.gengoai.hermes.Types;
import com.gengoai.hermes.format.CoNLLColumnProcessor;
import org.kohsuke.MetaInfServices;

/**
 * Processes Named Entities in CoNLL Format. Setting the config property <code>conll.ner.normalize=true</code> will
 * cause the entity types to be collapsed into coarser grained categories.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(CoNLLColumnProcessor.class)
public class NamedEntityProcessor extends IOBFieldProcessor {

    /**
     * Instantiates a new Named entity processor.
     */
    public NamedEntityProcessor() {
        super(Types.ENTITY, Types.ENTITY_TYPE);
    }

    @Override
    public String getFieldName() {
        return "ENTITY";
    }

    @Override
    protected String normalizeTag(String tag) {
        tag = tag.toUpperCase();
        switch (tag) {
            case "NORP":
                return "PERSON_GROUP";
            case "FAC":
                return "FACILITY";
            case "ORG":
                return "ORGANIZATION";
            case "LOC":
                return "LOCATION";
            case "PER":
                return "PERSON";
        }
        return tag;
    }
}//END OF NamedEntityProcessor
