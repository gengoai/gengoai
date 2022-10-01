#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.


import keras as K
import numpy as np
import tensorflow as tf
from apollo.data import ApolloSQLDataSet, pad
from apollo.layers import sequence_input, char_sequence_input, GloveEmbedding, CharEmbedding

data = ApolloSQLDataSet("../data/snli.db")

max_sequence_length = 128
max_word_length = 10
word_dimensions = 300
shape_dimensions = 50
char_dimensions = 50
lstm_units = word_dimensions + shape_dimensions + char_dimensions

input_data = {
    "c1": pad(data["c1"], max_sequence_length, max_word_length),
    "c2": pad(data["c2"], max_sequence_length, max_word_length),
    "w1": pad(data["w1"], max_sequence_length),
    "w2": pad(data["w2"], max_sequence_length),
    "s1": pad(data["s1"], max_sequence_length),
    "s2": pad(data["s2"], max_sequence_length)
}

input_layers = {
    "w1": sequence_input("w1"),
    "w2": sequence_input("w2"),
    "c1": char_sequence_input(max_word_length, "c1"),
    "c2": char_sequence_input(max_word_length, "c2"),
    "s1": sequence_input("s1"),
    "s2": sequence_input("s2")
}

w_embedding = GloveEmbedding(dimension=word_dimensions,
                             glove_path="../embeddings/glove%s.npy",
                             mask_zero=True)
w_translate = K.layers.TimeDistributed(K.layers.Dense(word_dimensions, activation='relu'), name="word_embedding")

c_embedding = CharEmbedding(input_dim=data.dimension("c1"),
                            output_dim=char_dimensions,
                            input_length=max_word_length)

s_embedding = K.layers.Embedding(output_dim=shape_dimensions,
                                 input_dim=data.dimension("s1"),
                                 mask_zero=True,
                                 name="shape_embedding")

w1_embedding = w_translate(w_embedding(input_layers["w1"]))
s1_embedding = s_embedding(input_layers["s1"])
c1_embedding = c_embedding(input_layers["c1"])
sentence1 = K.layers.concatenate([w1_embedding, c1_embedding, s1_embedding])

w2_embedding = w_translate(w_embedding(input_layers["w2"]))
c2_embedding = c_embedding(input_layers["c2"])
s2_embedding = s_embedding(input_layers["s2"])
sentence2 = K.layers.concatenate([w2_embedding, c2_embedding, s2_embedding])

# Sentence Embeddings
sentence_lstm = K.layers.LSTM(units=lstm_units, recurrent_dropout=0.5, name="sentence_lstm")
sentence1_embedding = sentence_lstm(sentence1)
sentence1_embedding = K.layers.Dropout(0.5)(sentence1_embedding)
sentence2_embedding = sentence_lstm(sentence2)
sentence2_embedding = K.layers.Dropout(0.5)(sentence2_embedding)

# Fully Connected Layers
out = K.layers.concatenate([sentence1_embedding, sentence2_embedding])
out = K.layers.Dense(600, activation="relu")(out)

# Output Label
out = K.layers.Dense(3, activation="softmax")(out)

model = K.Model(inputs=list(input_layers.values()), outputs=[out])
model.compile(optimizer=K.optimizers.Adadelta(),
              loss=K.losses.sparse_categorical_crossentropy,
              metrics=[K.metrics.sparse_categorical_accuracy])
print(model.summary())

model.fit(x=input_data,
          y=[np.array(data["label"])],
          validation_split=0.2,
          batch_size=512,
          epochs=20,
          callbacks=[K.callbacks.EarlyStopping(monitor='val_loss',patience=3,restore_best_weights=True)])
# callbacks=[es])

# words = sequence_input("words")
# shape = sequence_input("shape")
# chars = char_sequence_input(10, "chars")
# x = K.layers.concatenate([model.get_layer("word_embedding")(model.get_layer("glove300_embeddings")(words)),
#                           model.get_layer("char_embedding")(chars),
#                           model.get_layer("shape_embedding")(shape)])
# x = model.get_layer("sentence_lstm")(x)
# model = K.Model(inputs=[words, chars, shape],
#                 outputs=[x])
with K.backend.get_session() as sess:
    tf.saved_model.simple_save(
        sess,
        "models/test_model",
        inputs={t.name: t for t in model.inputs},
        outputs={t.name: t for t in model.outputs}
    )
