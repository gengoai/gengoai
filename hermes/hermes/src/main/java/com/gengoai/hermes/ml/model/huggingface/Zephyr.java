package com.gengoai.hermes.ml.model.huggingface;

import com.gengoai.python.PythonInterpreter;

import java.util.ArrayList;
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

    public static void main(String[] args) {
        Zephyr tg = new Zephyr(false);
        List<Map<String, String>> m = List.of(
                Map.of("role", "system", "content", "You're a helpful taxonomer. Fill in the blank with the concept that best fits the instance. Only answer with what goes in the blank. Afghanistan is a type of _."),
                Map.of("role", "assistant", "content", "Country"));

        var m2 = new ArrayList<>(m);
        m2.add(Map.of("role", "user", "content", "Covid-19 is a type of _."));
        System.out.println(tg.predict(m2));


        m2 = new ArrayList<>(m);
        m2.add(Map.of("role", "user", "content", "M1 Abrams is a type of _."));
        System.out.println(tg.predict(m2));
        m2 = new ArrayList<>(m);
        m2.add(Map.of("role", "user", "content", "F14 Tomcat is a type of _."));
        System.out.println(tg.predict(m2));
        m2 = new ArrayList<>(m);
        m2.add(Map.of("role", "user", "content", "B1 Bomber is a type of _."));
        System.out.println(tg.predict(m2));
        m2 = new ArrayList<>(m);
        m2.add(Map.of("role", "user", "content", "M1 Abrams is a type of _."));
        System.out.println(tg.predict(m2));

    }

}//END OF Zephyr