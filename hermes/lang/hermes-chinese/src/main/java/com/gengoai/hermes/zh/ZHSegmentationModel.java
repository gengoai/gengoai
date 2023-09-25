package com.gengoai.hermes.zh;

import com.gengoai.Language;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.model.Model;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.ResourceType;

import java.io.Serializable;
import java.util.Map;

public class ZHSegmentationModel implements Serializable {
    private static final long serialVersionUID = 1234567L;
    private final Model model;
    private final ZHSegmentationExtractor segmentationExtractor;

    public ZHSegmentationModel() throws Exception {
        model = ResourceType.MODEL.load("segmentation", Language.CHINESE);
        segmentationExtractor = new ZHSegmentationExtractor(ResourceType.WORD_LIST.load("dictionary", Language.CHINESE), 3);
    }

    public void segment(Document doc) {
        CharacterBILU.decode(doc, model.transform(new Datum(Map.of(Datum.DEFAULT_INPUT,
                                                                   segmentationExtractor.extractObservation(doc))))
                                       .getDefaultOutput()
                                       .asVariableSequence());
    }


}//END OF ZHSegmentationModel
