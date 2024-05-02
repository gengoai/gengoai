package com.gengoai.hermes.ml.model.huggingface;

import com.gengoai.ParamMap;
import com.gengoai.ParameterDef;

import java.io.Serial;
import java.io.Serializable;

public class ChatConfig extends ParamMap<ChatConfig> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1234567L;
    public static final ParameterDef<Double> TOP_P = ParameterDef.doubleParam("topP");
    public static final ParameterDef<Double> TEMPRATURE = ParameterDef.doubleParam("temperature");
    public static final ParameterDef<Integer> TOP_K = ParameterDef.intParam("topK");
    public static final ParameterDef<Boolean> DO_SAMPLE = ParameterDef.boolParam("doSample");
    public static final ParameterDef<Integer> MAX_TOKENS = ParameterDef.intParam("maxTokens");

    /**
     * The nucleus sampling parameter.
     */
    public final Parameter<Double> topP = parameter(TOP_P, 0.95);

    /**
     * The maximum number of tokens to generate.
     */
    public final Parameter<Integer> topK = parameter(TOP_K, 50);

    /**
     * Whether to sample from the model or not.
     */
    public final Parameter<Boolean> doSample = parameter(DO_SAMPLE, false);

    /**
     * The maximum number of tokens to generate.
     */
    public final Parameter<Integer> maxTokens = parameter(MAX_TOKENS, 512);

    /**
     * The temperature of the sampling. Higher values make the model more creative, lower values more conservative.
     */
    public final Parameter<Double> temperature = parameter(TEMPRATURE, 0.7);

}//END OF ChatConfig
