package com.gengoai.hermes.ml.model.huggingface;

import com.gengoai.config.Config;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.morphology.StopWords;
import com.gengoai.io.CSV;
import com.gengoai.io.CSVWriter;
import com.gengoai.io.Resources;
import com.gengoai.python.PythonInterpreter;
import lombok.NonNull;
import org.apache.commons.text.StringSubstitutor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

public class Zephyr {
    private final String uniqueFuncName;

    public Zephyr() {
        this(config -> {
        });
    }

    public Zephyr(@NonNull Consumer<ChatConfig> consumer) {
        ChatConfig config = new ChatConfig();
        consumer.accept(config);
        this.uniqueFuncName = PythonInterpreter.getInstance().generateUniqueFuncName();
        PythonInterpreter.getInstance()
                         .exec(new StringSubstitutor(Map.of("uniqueFuncName", uniqueFuncName,
                                                            "maxTokens", Integer.toString(config.maxTokens.value()),
                                                            "doSample", config.doSample.value() ? "True" : "False",
                                                            "temperature", Double.toString(config.temperature.value()),
                                                            "topK", Integer.toString(config.topK.value()),
                                                            "topP", Double.toString(config.topP.value())
                                                           ))
                                       .replace("""
                                                        from transformers import pipeline
                                                        import torch
                                                        ${uniqueFuncName}_nlp = pipeline('text-generation',
                                                                                         model="HuggingFaceH4/zephyr-7b-beta",
                                                                                         torch_dtype=torch.bfloat16,
                                                                                         device_map="auto")
                                                         
                                                        def ${uniqueFuncName}(context):
                                                            prompt = ${uniqueFuncName}_nlp.tokenizer.apply_chat_template(
                                                                context, tokenize=False, add_generation_prompt=True
                                                            )
                                                            decoded = ${uniqueFuncName}_nlp(prompt,
                                                                                          max_new_tokens=${maxTokens},
                                                                                          do_sample=${doSample},
                                                                                          temperature=${temperature},
                                                                                          top_k=${topK},
                                                                                          top_p=${topP})[0]["generated_text"]
                                                            decoded = decoded[decoded.rfind("<|assistant|>") + len("<|assistant|>"):].strip()
                                                            return decoded
                                                         """));
    }

    public String predict(ChatTemplate context) {
        return PythonInterpreter.getInstance().invoke(uniqueFuncName, context).toString();
    }

    public static void main(String[] args) throws Exception {
        Config.initialize("Zephyr", args);
        Zephyr tg = new Zephyr(p -> p.doSample.set(false));
        ChatTemplate.Builder builder = ChatTemplate.builder()
                                                   .system("You're a helpful taxonomer. Fill in the blank with the concept that best fits the instance. Only answer with what goes in the blank.")
                                                   .user("Afghanistan is a type of _.")
                                                   .assistant("Country")
                                                   .user("Toyota is a type of _.")
                                                   .assistant("Organization")
                                                   .user("Jane Doe is a type of _.")
                                                   .assistant("Person")
                                                   .user("Christmas is a type of _.")
                                                   .assistant("Holiday")
                                                   .user("Olympics is a type of _.")
                                                   .assistant("Sporting Event");

        Corpus corpus = Corpus.open("/home/ik/news.corpus");
        long counter = 0;
        try (CSVWriter writer = CSV.csv().writer(Resources.from("/home/ik/news.csv"))) {
            for (Document document : corpus) {
                for (Annotation chunk : document.chunks()) {
                    if (chunk.pos().isNoun()) {
                        HString np = chunk.trim(StopWords.isStopWord());
                        if (!np.isEmpty()) {
                            var m2 = builder.copy();
                            m2.user(np + " is a type of _.");
                            String prediction = tg.predict(m2.build());
                            writer.write(Arrays.asList(np.toString(), prediction));
                            writer.flush();
                            System.out.println(np + " is a type of " + prediction);
                        }
                    }
                }
                counter++;
                if (counter > 1000) {
                    break;
                }
            }
        }


    }

}//END OF Zephyr