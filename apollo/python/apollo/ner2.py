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
from apollo.data import ApolloSQLDataSet, pad
from apollo.layers import sequence_input, char_sequence_input, GloveEmbedding, CharEmbedding
from keras_contrib.layers import CRF
from keras_contrib.losses import crf_loss
from keras_contrib.metrics import crf_marginal_accuracy
import tensorflow as tf


data = ApolloSQLDataSet('data/entity2.db')

max_sequence_length = 512
max_word_length = 10
word_dimensions = 50
shape_dimensions = 50
char_dimensions = 50
lstm_units = word_dimensions + shape_dimensions + char_dimensions

input_data = {
    "chars": pad(data["chars"], max_sequence_length, max_word_length),
    "words": pad(data["words"], max_sequence_length),
    "shape": pad(data["shape"], max_sequence_length)
}
y = K.utils.to_categorical(pad(data['label'], max_sequence_length), data.dimension('label'))

input_layers = {
    "words": sequence_input("words"),
    "chars": char_sequence_input(max_word_length, "chars"),
    "shape": sequence_input("shape")
}

word_embedding = GloveEmbedding(dimension=word_dimensions)(input_layers["words"])
char_embedding = CharEmbedding(input_dim=data.dimension("chars"),
                               output_dim=char_dimensions,
                               input_length=max_word_length)(input_layers["chars"])
shape_embedding = K.layers.Embedding(output_dim=shape_dimensions,
                                     input_dim=data.dimension("shape"),
                                     mask_zero=True,
                                     name="shape_embedding")(input_layers["shape"])

concat = K.layers.concatenate([word_embedding, char_embedding, shape_embedding])
x = concat
for i in range(2):
    lstm = K.layers.Bidirectional(K.layers.LSTM(lstm_units, return_sequences=True, recurrent_dropout=0.5))(x)
    x = K.layers.concatenate([concat, lstm])

x = CRF(data.dimension('label'), learn_mode="marginal", name='label')(x)

model = K.Model(inputs=[input_layers["words"], input_layers["chars"], input_layers["shape"]],
                outputs=[x])

model.compile(optimizer=K.optimizers.Adam(),
              loss=crf_loss,
              metrics=[crf_marginal_accuracy])
print(model.summary())

model.fit(x=input_data,
          y=[y],
          validation_split=0,
          shuffle=True,
          batch_size=100,
          epochs=20,
          callbacks=[K.callbacks.EarlyStopping(monitor="loss", patience=4)])
with K.backend.get_session() as sess:
    tf.saved_model.simple_save(
        sess,
        "models/ner-tmp",
        inputs={t.name: t for t in model.inputs},
        outputs={t.name: t for t in model.outputs}
    )
