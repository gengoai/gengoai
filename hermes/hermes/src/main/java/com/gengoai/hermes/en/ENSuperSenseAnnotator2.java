package com.gengoai.hermes.en;

import com.gengoai.Language;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.ResourceType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.Annotator;
import com.gengoai.hermes.ml.HStringMLModel;

import java.util.Set;

public class ENSuperSenseAnnotator2 extends Annotator {
    private static final long serialVersionUID = 1L;
    private final HStringMLModel tagger;

    public ENSuperSenseAnnotator2() {
        this.tagger = ResourceType.MODEL.load("super_sense", Language.ENGLISH);
    }

    @Override
    protected void annotateImpl(Document document) {
        tagger.apply(document);
    }

    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.SUPER_SENSE);
    }

    @Override
    public Set<AnnotatableType> requires() {
        return Set.of(Types.PART_OF_SPEECH, Types.SENTENCE, Types.TOKEN);
    }

    @Override
    public String getProvider(Language language) {
        return tagger.getClass().getSimpleName() + " v" + tagger.getVersion();
    }

}//END OF ENSuperSenseAnnotator
