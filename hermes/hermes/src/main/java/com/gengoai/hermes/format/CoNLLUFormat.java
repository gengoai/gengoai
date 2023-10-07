package com.gengoai.hermes.format;

import com.gengoai.LogUtils;
import com.gengoai.collection.tree.SimpleSpan;
import com.gengoai.config.Preloader;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Relation;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.Data;
import lombok.extern.java.Log;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Log
public class CoNLLUFormat extends WholeFileTextFormat implements OneDocPerFileFormat, Serializable {
    private static final long serialVersionUID = 1L;
    private final DocFormatParameters docFormatParameters;

    public CoNLLUFormat(DocFormatParameters docFormatParameters) {
        this.docFormatParameters = docFormatParameters;
    }

    @Override
    public DocFormatParameters getParameters() {
        return docFormatParameters;
    }

    @Override
    public void write(Document document, Resource outputResource) throws IOException {
        throw new UnsupportedOperationException();
    }

    private Document createDocument(String content,
                                    String documentId,
                                    List<List<Row>> sentences) {
        Document document = Document.create(documentId, content);
        document.setLanguage(docFormatParameters.getDocumentFactory().getDefaultLanguage());
        int sentenceIndex = 0;
        for (List<Row> sentence : sentences) {
            int sentenceStart = sentence.get(0).start;
            int sentenceEnd = sentence.get(sentence.size() - 1).end;
            document.annotationBuilder(Types.SENTENCE)
                    .bounds(new SimpleSpan(sentenceStart, sentenceEnd))
                    .attribute(Types.INDEX, sentenceIndex)
                    .createAttached();
            Map<String, Long> index2AnnotationId = new HashMap<>();

            //Add tokens
            for (int i = 0; i < sentence.size(); i++) {
                Row row = sentence.get(i);
                Annotation token = document.annotationBuilder(Types.TOKEN)
                                           .bounds(new SimpleSpan(row.start, row.end))
                                           .attribute(Types.LEMMA, row.lemma)
                                           .createAttached();
                row.annotationId = token.getId();
                index2AnnotationId.put(row.index, token.getId());
                if (!row.feats.equals("_")) {
                    //IGNORE FOR NOW
//                    token.put(Types.MORPHOLOGICAL_FEATURES, UniversalFeatureSet.parse(row.feats));
                }
                if (row.getXpos().equals("GW")) {
                    Row next = null;
                    for (int j = i + 1; j < sentence.size(); j++) {
                        if (!sentence.get(j).getXpos().equals("GW")) {
                            next = sentence.get(j);
                            break;
                        }
                    }
                    token.put(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(POSCorrection.pos(next.getWord(), next.getXpos())));
                } else {
                    try {
                        token.put(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(POSCorrection.pos(row.getWord(), row.getXpos())));
                    } catch (IllegalArgumentException e) {
                        LogUtils.logWarning(log, "Invalid POS: {0} in {1}", row.getXpos(), document.getId());
                        token.put(Types.PART_OF_SPEECH, PartOfSpeech.ANY);
                    }
                }
            }

            //Add dependencies
            for (Row row : sentence) {
                if (!Strings.isNullOrBlank(row.head) && !row.head.equals("_") && !row.head.equals("0")) {
                    Annotation token = document.annotation(row.annotationId);
                    Annotation head = document.annotation(index2AnnotationId.get(row.head));
                    token.add(new Relation(Types.DEPENDENCY, row.deprel, head.getId()));
                }
            }

            sentenceIndex++;
        }
        return document;
    }

    @Override
    protected Stream<Document> readSingleFile(String content) {
        final List<Document> documents = new ArrayList<>();
        final StringBuilder stringBuilder = new StringBuilder();
        List<List<Row>> document = new ArrayList<>();
        List<Row> sentence = new ArrayList<>();
        int tokenIndex = 0;

        String documentId = null;
        for (String line : content.split("\\r?\\n")) {
            line = line.strip();
            if (Strings.isNullOrBlank(line) || line.startsWith("-X-")) {
                if (!sentence.isEmpty()) {
                    document.add(sentence);
                    sentence = new ArrayList<>();
                    stringBuilder.append("\n");
                }
            } else if (line.startsWith("# newdoc id")) {
                if (!sentence.isEmpty()) {
                    document.add(sentence);
                    tokenIndex = 0;
                }
                if (!document.isEmpty()) {
                    documents.add(createDocument(stringBuilder.toString(), documentId, document));
                    document = new ArrayList<>();
                    sentence = new ArrayList<>();
                    stringBuilder.setLength(0);
                }
                documentId = line.substring(line.indexOf("=") + 1).strip();
            } else if (!line.startsWith("#")) {
                String[] parts = line.split("\\s+");
                Row row = new Row();
                row.tokenIndex = tokenIndex;
                tokenIndex++;
                row.start = stringBuilder.length();
                row.end = row.start + parts[1].length();
                row.index = parts[0];
                row.word = parts[1];
                row.lemma = parts[2];
                row.upos = parts[3];
                row.xpos = parts[4];
                row.feats = parts[5];
                row.head = parts[6];
                row.deprel = parts[7];
                row.deps = parts[8];
                row.misc = parts[9];
                row.addSpace = !row.misc.contains("SpaceAfter=No");
                sentence.add(row);
                stringBuilder.append(row.word);
                if (row.addSpace) {
                    stringBuilder.append(" ");
                }
            }
        }

        if (!sentence.isEmpty()) {
            document.add(sentence);
        }
        if (!document.isEmpty()) {
            documents.add(createDocument(stringBuilder.toString(), documentId, document));
        }

        return documents.stream();
    }

    @Data
    private static class Row {
        long annotationId;
        int tokenIndex;
        String index;
        int start;
        int end;
        String word;
        String lemma;
        String upos;
        String xpos;
        String feats;
        String head;
        String deprel;
        String deps;
        String misc;
        boolean addSpace;
    }

    public static void main(String[] args) {
        Preloader.preload();
        CoNLLUFormat format = new CoNLLUFormat(new DocFormatParameters());
        List<Document> docs =
                format.read(Resources.from("https://raw.githubusercontent.com/UniversalDependencies/UD_English-GUM/master/en_gum-ud-train.conllu"))
                      .collect();
        for (Document doc : docs) {
            System.out.println(doc.getId());
            System.out.println(doc);
            for (Annotation sentence : doc.sentences()) {
                System.out.println(sentence.toPOSString());
            }
        }
    }

    @MetaInfServices
    public static class Provider implements DocFormatProvider {

        @Override
        public DocFormat create(DocFormatParameters parameters) {
            return new CoNLLUFormat(parameters);
        }

        @Override
        public String getName() {
            return "CONLLU";
        }

        @Override
        public boolean isWriteable() {
            return false;
        }
    }
}//END OF CoNLLUFormat
