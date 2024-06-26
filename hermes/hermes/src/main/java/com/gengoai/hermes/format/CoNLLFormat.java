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

package com.gengoai.hermes.format;

import com.gengoai.Language;
import com.gengoai.ParameterDef;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.Types;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.reflection.TypeUtils.parameterizedType;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>Format Name: <b>conll</b></p>
 * <p>
 * The CoNLL format. The following additional parameters are available when reading/writing in CoNLL format:
 * </p>
 * <p><ul>
 * <li>docPerSentence=[true|false]: One document per sentence when true (default: true).
 * <li>fields=&lt;list of fields&gt;: list of string denoting the field names (default: ["WORD", "POS", "CHUNK")]).
 * <li>fs=&lt;String&gt;: Field separator (default: "\\s+")
 * <li>overrideSentences=[true|false]: Override the CoNLL sentence boundaries with Hermes boundaries when true
 * (default: false)
 * </ul></p>
 * <p>Currently, the following indexes are supported:</p>
 * <p><ul>
 * <li>INDEX - The index of the word in the sentence.
 * <li>WORD - The word.
 * <li>LEMMA - The lemmatized form of the word.
 * <li>UPOS - The universal part-of-speech tag of the word.
 * <li>POS - The part-of-speech tag of the word.
 * <li>CHUNK - IOB annotated Phrase Chunks.
 * <li>ENTITY - IOB annotated Named Entities.
 * <li>HEAD - The index of this word’s syntactic head in the sentence.
 * <li>DEP_REL - The dependency relation of this word to its head.
 * <li>IGNORE - Ignores the field.
 * </ul></p>
 */
public class CoNLLFormat extends WholeFileTextFormat implements OneDocPerFileFormat, Serializable {
    /**
     * True create a document per sentence, False multiple sentences per document
     */
    public static final ParameterDef<Boolean> DOC_PER_SENTENCE = ParameterDef.boolParam("docPerSentence");
    /**
     * Empty Field Content
     */
    public static final String EMPTY_FIELD = "_";
    /**
     * The name of the fields in the CoNLL File
     */
    public static final ParameterDef<List<String>> FIELDS = ParameterDef.param("fields", parameterizedType(List.class,
            String.class));
    /**
     * The String used to separate fields
     */
    public static final ParameterDef<String> FIELD_SEPARATOR = ParameterDef.strParam("fs");
    /**
     * True override sentence boundaries with Hermes boundaries
     */
    public static final ParameterDef<Boolean> OVERRIDE_SENTENCES = ParameterDef.boolParam("overrideSentences");
    private static final long serialVersionUID = 1L;
    private final CoNLLParameters parameters;

    CoNLLFormat(@NonNull CoNLLParameters parameters) {
        this.parameters = parameters;
    }

    private Document createDocument(String content, List<CoNLLRow> list, DocumentFactory documentFactory) {
        Document document = documentFactory.createRaw(content);
        int lastSentenceStart = -1;
        int sentenceIndex = 0;
        Map<Tuple2<Integer, Integer>, Long> sentenceIndexToIDMap = new HashMap<>();

        boolean keepSentences = !parameters.overrideSentences.value();
        for (ListIterator<CoNLLRow> iterator = list.listIterator(); iterator.hasNext(); ) {
            CoNLLRow token = iterator.next();
            if (lastSentenceStart == -1) {
                lastSentenceStart = token.getStart();
            }
            token.setAnnotationID(document.createAnnotation(Types.TOKEN, token.getStart(), token.getEnd(),
                    Collections.emptyMap()).getId());
            sentenceIndexToIDMap.put($(token.getSentence(), token.getIndex()), token.getAnnotationID());
            if (!iterator.hasNext() || token.getSentence() != list.get(iterator.nextIndex()).getSentence()) {
                if (keepSentences) {
                    document.createAnnotation(Types.SENTENCE,
                            lastSentenceStart,
                            token.getEnd(),
                            hashMapOf($(Types.INDEX, sentenceIndex))
                    );
                }
                sentenceIndex++;
                lastSentenceStart = -1;
            }
        }
        for (CoNLLColumnProcessor processor : CoNLLProcessors.get(parameters.fields.value())) {
            processor.processInput(document, list, sentenceIndexToIDMap);
        }
        if (keepSentences) {
            document.setCompleted(Types.SENTENCE, "PROVIDED");
        }
        document.setCompleted(Types.TOKEN, "PROVIDED");
        return document;
    }

    @Override
    public DocFormatParameters getParameters() {
        return parameters;
    }

    @Override
    protected Stream<Document> readSingleFile(String file) {
        List<Document> documents = new LinkedList<>();
        List<CoNLLRow> list = new ArrayList<>();
        int sentenceIndex = 0;
        StringBuilder content = new StringBuilder();
        int lastSize = 0;

        final List<CoNLLColumnProcessor> processors = CoNLLProcessors.get(parameters.fields.value());
        final String FS = parameters.fieldSeparator.value();
        final boolean oneDocumentPerSentence = parameters.docPerSentence.value();
        final DocumentFactory documentFactory = parameters.getDocumentFactory();

        final String resource = file.strip();
        for (String line : resource.split("\\r?\\n")) {
            line = line.strip();
            if (Strings.isNullOrBlank(line) || line.trim().startsWith("-X-") || line.startsWith("# newdoc id")) {
                if (list.size() > lastSize) {
                    sentenceIndex++;
                    if (oneDocumentPerSentence) {
                        documents.add(createDocument(content.toString(), list, documentFactory));
                        sentenceIndex = 0;
                        list.clear();
                        content.setLength(0);
                        lastSize = 0;
                    }
                }
            } else if (line.strip().startsWith("#")) {
                continue;
            } else {
                List<String> parts = Arrays.asList(line.split(FS));
                if (parts.size() < processors.size()) {
                    continue;
                }
                CoNLLRow row = new CoNLLRow();
                row.setSentence(sentenceIndex);
                for (int i = 0; i < processors.size(); i++) {
                    if (Strings.isNullOrBlank(parts.get(i))) {
                        continue;
                    }
                    processors.get(i).updateRow(row, parts.get(i));
                }
                row.setStart(content.length());
                content.append(row.getWord());
                if (parameters.defaultLanguage.value().usesWhitespace()) {
                    content.append(" ");
                    row.setEnd(content.length() - 1);
                } else {
                    row.setEnd(content.length());
                }

                list.add(row);
            }
        }
        if (list.size() > 0) {
            documents.add(createDocument(content.toString(), list, documentFactory));
        }
        return documents.stream();
    }

    @Override
    public void write(Document document, Resource outputResource) throws IOException {
        final List<CoNLLColumnProcessor> processors = CoNLLProcessors.get(parameters.fields.value());
        final String FS = "\t";
        int index = 0;
        try (BufferedWriter writer = new BufferedWriter(outputResource.writer())) {
            for (Annotation sentence : document.sentences()) {
                for (Annotation token : sentence.tokens()) {
                    for (int i = 0; i < processors.size(); i++) {
                        if (i > 0) {
                            writer.write(FS);
                        }
                        writer.write(processors.get(i).processOutput(document, token, index));
                    }
                    writer.newLine();
                    index++;
                }
                writer.newLine();
            }
        }
    }

    /**
     * The type Provider.
     */
    @MetaInfServices
    public static class Provider implements DocFormatProvider {

        @Override
        public DocFormat create(DocFormatParameters parameters) {
            if (parameters instanceof CoNLLParameters) {
                return new CoNLLFormat(Cast.as(parameters));
            }
            throw new IllegalArgumentException("Invalid parameter class, expecting: " +
                    CoNLLParameters.class.getName() +
                    ", but received: " +
                    parameters.getClass().getName());
        }

        @Override
        public DocFormatParameters getDefaultFormatParameters() {
            return new CoNLLParameters();
        }

        @Override
        public String getName() {
            return "CONLL";
        }

        @Override
        public boolean isWriteable() {
            return true;
        }
    }

    /**
     * The type CoNLL parameters.
     */
    public static class CoNLLParameters extends DocFormatParameters {
        /**
         * True create a document per sentence, False multiple sentences per document
         */
        Parameter<Boolean> docPerSentence = parameter(DOC_PER_SENTENCE, false);
        /**
         * The String used to separate fields
         */
        Parameter<String> fieldSeparator = parameter(FIELD_SEPARATOR, "\\s+");
        /**
         * The name of the fields in the CoNLL File
         */
        Parameter<List<String>> fields = parameter(FIELDS, Arrays.asList("WORD", "POS", "CHUNK"));
        /**
         * True override sentence boundaries with Hermes boundaries
         */
        Parameter<Boolean> overrideSentences = parameter(OVERRIDE_SENTENCES, false);
    }
}//END OF CoNLLFormat
