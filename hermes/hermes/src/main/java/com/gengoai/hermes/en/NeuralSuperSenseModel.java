package com.gengoai.hermes.en;

import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.IOB;
import com.gengoai.hermes.ml.IOBValidator;
import com.gengoai.hermes.ml.model.TFSequenceLabeler;

import java.util.List;

import static com.gengoai.apollo.feature.Featurizer.valueFeaturizer;

public class NeuralSuperSenseModel extends TFSequenceLabeler {
    private static final long serialVersionUID = 1L;
    private static final String LABEL = "label";
    private static final String TOKENS = "words";
    private static final String CHARS = "chars";
    private static final String POS = "pos";
    private static final int MAX_WORD_LENGTH = 25;

    public NeuralSuperSenseModel() {
        super(
                List.of(TFInputVar.sequence(TOKENS, -1)),
                List.of(TFOutputVar.sequence(LABEL,
                                             "label/truediv",
                                             "O",
                                             IOBValidator.INSTANCE)),
                IOB.decoder(Types.SUPER_SENSE)
             );
    }

    @Override
    public HStringDataSetGenerator getDataGenerator() {
        return HStringDataSetGenerator.builder(Types.SENTENCE)
                                      .tokenSequence(TOKENS, valueFeaturizer(h -> h.toLowerCase() + "::" + h.upos().name()))
                                      .source(LABEL, IOB.encoder(Types.SUPER_SENSE))
                                      .build();
    }

    @Override
    public String getVersion() {
        return "2.2";
    }

}//END OF NeuralNERModel
