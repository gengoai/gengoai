package com.gengoai.hermes.zh;

import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.Annotator;

import java.util.Set;

public class ZHTokenAnnotator extends Annotator {
    private static final long serialVersionUID = 1L;
    private final ZHSegmentationModel model;

    public ZHTokenAnnotator()  {
        try {
            model = new ZHSegmentationModel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void annotateImpl(Document document) {
        model.segment(document);
    }

    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.TOKEN, Types.PART_OF_SPEECH, Types.LEMMA);
    }
}//END OF ZHTokenAnnotator
