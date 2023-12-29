package com.gengoai.hermes.ml.model.huggingface.annotator;

import com.gengoai.Language;
import com.gengoai.cache.Cache;
import com.gengoai.config.Config;
import com.gengoai.hermes.*;
import com.gengoai.hermes.annotator.Annotator;
import com.gengoai.hermes.ml.model.huggingface.TokenClassification;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityAnnotator extends Annotator {
    private final Cache<Language, TokenClassification> tokenClassification = Cache.create(100);


    private TokenClassification getTagger(Language language) {
        String modelName = Config.get("huggingface.entity.model." + language.getCode()).asString(TokenClassification.BERT_BASE_NER);
        return tokenClassification.get(language, () -> new TokenClassification(modelName));
    }


    @Override
    protected void annotateImpl(Document d) {
        final TokenClassification tc = getTagger(d.getLanguage());
        List<String> sentences = d.sentences().stream().map(Annotation::toString).collect(Collectors.toList());
        List<List<TokenClassification.Output>> outputs = tc.predict(sentences);
        for (int i = 0; i < outputs.size(); i++) {
            List<TokenClassification.Output> output = outputs.get(i);
            Annotation sentence = d.sentences().get(i);
            for (TokenClassification.Output o : output) {
                HString eHStr = sentence.substring(o.getStart(), o.getEnd());
                d.createAnnotation(Types.ENTITY,
                                   eHStr.start(),
                                   eHStr.end(),
                                   Map.of(Types.ENTITY_TYPE, EntityType.valueOf(o.getLabel()),
                                          Types.CONFIDENCE, o.getConfidence()));
            }
        }
//        for (Annotation sentence : d.sentences()) {
//            List<TokenClassification.Output> outputs = tc.predict(sentence.toString());
//            for (TokenClassification.Output output : outputs) {
//                HString eHStr = sentence.substring(output.getStart(), output.getEnd());
//                d.createAnnotation(Types.ENTITY,
//                                   eHStr.start(),
//                                   eHStr.end(),
//                                   Map.of(Types.ENTITY_TYPE, EntityType.valueOf(output.getLabel()),
//                                          Types.CONFIDENCE, output.getConfidence()));
//            }
//        }
    }

    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.ML_ENTITY);
    }

}//END OF EntityAnnotator
