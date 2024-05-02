package com.gengoai.hermes.ml.model.huggingface;

import com.gengoai.Copyable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class ChatTemplate extends ArrayList<Map<String, String>> implements Copyable<ChatTemplate> {


    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ChatTemplate copy() {
        return Copyable.deepCopy(this);
    }

    public static class Builder implements Copyable<Builder>, Serializable {
        private final ChatTemplate chatTemplate = new ChatTemplate();


        public Builder system(String content) {
            chatTemplate.add(Map.of("role", "system", "content", content));
            return this;
        }

        public Builder user(String content) {
            chatTemplate.add(Map.of("role", "user", "content", content));
            return this;
        }

        public Builder assistant(String content) {
            chatTemplate.add(Map.of("role", "assistant", "content", content));
            return this;
        }


        public Builder message(String role, String content) {
            chatTemplate.add(Map.of("role", role, "content", content));
            return this;
        }

        public ChatTemplate build() {
            return chatTemplate;
        }

        @Override
        public Builder copy() {
            return Copyable.deepCopy(this);
        }
    }//END OF Builder


}//END OF ChatTemplate
