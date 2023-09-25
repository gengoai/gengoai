package com.gengoai.hermes.annotator;

import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.SuperSense;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.en.ENWordSenseAnnotator;
import com.gengoai.hermes.wordnet.Sense;

import java.util.List;
import java.util.Set;

public class DefaultSuperSenseAnnotator extends SentenceLevelAnnotator {
    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.SUPER_SENSE);
    }

    @Override
    protected Set<AnnotatableType> furtherRequires() {
        return Set.of(Types.WORD_SENSE);
    }

    @Override
    protected void annotate(Annotation sentence) {
        for (Annotation wordSense : sentence.annotations(Types.WORD_SENSE)) {
            List<Sense> senses = wordSense.attribute(ENWordSenseAnnotator.SENSE);
            Sense sense = senses.get(0);
            if (sense.getPOS().isVerb()) { //|| sense.getPOS().isNoun()) {
                sentence.document()
                        .annotationBuilder(Types.SUPER_SENSE)
                        .bounds(wordSense)
                        .attribute(Types.SENSE_CLASS, SuperSense.valueOf(sense.getSynset().getLexicographerFile().name()))
                        .createAttached();
            }
        }
    }
}
