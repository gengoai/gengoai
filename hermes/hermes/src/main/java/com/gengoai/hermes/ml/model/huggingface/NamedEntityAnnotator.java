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

package com.gengoai.hermes.ml.model.huggingface;

import com.gengoai.collection.Iterables;
import com.gengoai.config.Config;
import com.gengoai.config.Preloader;
import com.gengoai.hermes.*;
import com.gengoai.hermes.corpus.DocumentCollection;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NamedEntityAnnotator {

    private final TokenClassification tc;

    public NamedEntityAnnotator(String modelPath) {
        tc = new TokenClassification(modelPath);
    }

    public void annotate(@NonNull Document document) {
        document.annotate(Types.SENTENCE);
        List<String> sentences = document.sentences().stream().map(Annotation::toString).collect(Collectors.toList());
        List<List<TokenClassification.Output>> outputs = tc.predict(sentences);
        for (Map.Entry<List<TokenClassification.Output>, Annotation> listAnnotationEntry : Iterables.zip(outputs, document.sentences())) {
            List<TokenClassification.Output> sentenceOutput = listAnnotationEntry.getKey();
            Annotation sentence = listAnnotationEntry.getValue();
            for (TokenClassification.Output output : sentenceOutput) {
                HString eHStr = sentence.substring(output.getStart(), output.getEnd());
                document.createAnnotation(Types.ML_ENTITY,
                                          eHStr.start(),
                                          eHStr.end(),
                                          Map.of(Types.ENTITY_TYPE, EntityType.valueOf(output.getLabel()),
                                                 Types.CONFIDENCE, output.getConfidence()));
            }
        }
    }

    public static void main(String[] args) {
        Config.initialize("NamedEntityAnnotator", args);
        Preloader.preload();
        NamedEntityAnnotator annotator = new NamedEntityAnnotator("/work/prj/huggingface/token-classification/bert-finetuned-ner");
        DocumentCollection documents = DocumentCollection.create(
                Document.create("This offseason, the Tampa Bay Buccaneers brought in two new quarterbacks to replace the old guard of Tom Brady and backup Blaine Gabbert. Those two QBs were Baker Mayfield and John Wolford. For Baker, it was clear that he would enter the offseason in Tampa Bay in a heated quarterback battle with third-year former University of Florida QB Kyle Trask - the former went on to win the starting job."),
                Document.create("The Tampa police arrested Kenny Loggings, 76, for bicycling naked down main street."),
                Document.create("Tampa and Orlando are seeing a surge in COVID-19 cases.")
                                                                );
        for (Document document : documents) {
            annotator.annotate(document);
            System.out.println(document);
            document.entities().forEach(e -> System.out.println(e + " " + e.getTag()));
            System.out.println();
        }
    }

}//END OF NamedEntityAnnotator
