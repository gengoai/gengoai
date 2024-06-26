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

package com.gengoai.hermes.workflow.actions;

import com.gengoai.LogUtils;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.io.resource.Resource;
import lombok.Data;
import lombok.extern.java.Log;
import org.kohsuke.MetaInfServices;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Log
@Data
@MetaInfServices
public class Import implements Action {
    private static final long serialVersionUID = 1L;
    private String id;
    private boolean dropExisting = false;

    @Override
    public String getName() {
        return "IMPORT";
    }

    @Override
    public String getDescription() {
        return "Import documents into a Corpus.";
    }

    @Override
    public DocumentCollection process(DocumentCollection corpus, Context context) throws Exception {
        final Resource workflowFolder = context.getWorkflowFolder();
        final Resource location = workflowFolder.getChild("corpus");

        if (dropExisting && location.exists()) {
            LogUtils.logConfig(log, "Dropping existing corpus at ''{0}''.", location);
            location.delete(true);
        }

        LogUtils.logConfig(log, "Saving document collection to ''{0}''.", location);
        Corpus toCorpus = Corpus.open(location);
        String importDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        toCorpus.addAll(corpus.stream().map(d -> {
            d.put(Types.IMPORT_DATE, importDate);
            return d;
        }));
        return toCorpus;
    }


}//END OF ImportDocuments
