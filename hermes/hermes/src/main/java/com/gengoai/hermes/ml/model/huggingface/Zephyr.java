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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Zephyr {
    private final String uniqueFuncName;

    public Zephyr(boolean doSample) {
        this.uniqueFuncName = PythonInterpreter.getInstance().generateUniqueFuncName();
        PythonInterpreter.getInstance()
                         .exec("from transformers import pipeline\n" +
                                       "import torch\n" +
                                       uniqueFuncName + "_nlp = pipeline('text-generation',model=\"HuggingFaceH4/zephyr-7b-alpha\",\n" +
                                       "    torch_dtype=torch.bfloat16,\n" +
                                       "    device_map=\"auto\")\n" +
                                       "def " + uniqueFuncName + "(context):\n" +
                                       "   prompt = " + uniqueFuncName + "_nlp.tokenizer.apply_chat_template(\n" +
                                       "        context, tokenize=False, add_generation_prompt=True\n" +
                                       "    )\n" +
                                       "   decoded = " + uniqueFuncName + "_nlp(prompt,max_new_tokens=4000,do_sample=" + (doSample ? "True" : "False") + ",temperature=0.7,top_k=50,top_p=0.95)[0][\"generated_text\"]\n" +
                                       "   decoded = decoded[ decoded.rfind(\"<|assistant|>\") + len(\"<|assistant|>\"): ].strip()\n" +
                                       "   return decoded\n");
    }

    public String predict(List<Map<String, String>> context) {
        return PythonInterpreter.getInstance().invoke(uniqueFuncName, context).toString();
    }

    public static void main(String[] args) throws Exception {
        Config.initialize("Zephyr", args);
        Zephyr tg = new Zephyr(false);
        List<Map<String, String>> m = List.of(
                Map.of("role", "system", "content", "You're a helpful taxonomer. Fill in the blank with the concept that best fits the instance. Only answer with what goes in the blank."),
                Map.of("role", "user", "content", "Afghanistan is a type of _."),
                Map.of("role", "assistant", "content", "Country"),
                Map.of("role", "user", "content", "Toyota is a type of _."),
                Map.of("role", "assistant", "content", "Organization"),
                Map.of("role", "user", "content", "Jane Doe is a type of _."),
                Map.of("role", "assistant", "content", "Person"),
                Map.of("role", "user", "content", "Christmas is a type of _."),
                Map.of("role", "assistant", "content", "Holiday"),
                Map.of("role", "user", "content", "Olympics is a type of _."),
                Map.of("role", "assistant", "content", "Sporting Event")
                                             );

        Corpus corpus = Corpus.open("/home/ik/news.corpus");
        long counter = 0;
        try (CSVWriter writer = CSV.csv().writer(Resources.from("/home/ik/news.csv"))) {
            for (Document document : corpus) {
                for (Annotation chunk : document.chunks()) {
                    if (chunk.pos().isNoun()) {
                        HString np = chunk.trim(StopWords.isStopWord());
                        if (!np.isEmpty()) {
                            var m2 = new ArrayList<>(m);
                            m2.add(Map.of("role", "user", "content", np + " is a type of _."));
                            writer.write(Arrays.asList(
                                    np.toString(),
                                    tg.predict(m2)
                                                      ));
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