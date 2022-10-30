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
from keras_contrib.layers import CRF
from keras_contrib.losses import crf_loss
from keras_contrib.metrics import crf_marginal_accuracy

from apollo.layers import sequence_input, char_sequence_input, GloveEmbedding, CharEmbedding
from apollo.model_io import save_model


class BiLstmCRF:

    def __init__(self,
                 lstm_units,
                 number_of_labels,
                 use_residual=True,
                 word_embedding_dim=100,
                 use_chars=True,
                 char_input_dim=0,
                 char_embedding_dim=10,
                 max_word_length=20,
                 use_shape=True,
                 shape_input_dim=0,
                 shape_embedding_dim=10,
                 embedding_layer_dropout=0.2,
                 middle_layer_dropout=0.2,
                 lstm_recurrent_dropout=0.5
                 ):
        self.input_layers = {
            "words": sequence_input("words")
        }
        self.inputs = [self.input_layers["words"]]
        self.embedding_layer = [GloveEmbedding(dimension=word_embedding_dim)(self.input_layers["words"])]

        if use_chars:
            self.input_layers["chars"] = char_sequence_input(max_word_length, "chars")
            self.inputs.append(self.input_layers["chars"])
            self.embedding_layer.append(CharEmbedding(input_dim=char_input_dim,
                                                      output_dim=char_embedding_dim,
                                                      input_length=max_word_length)(self.input_layers["chars"]))
        if use_shape:
            self.input_layers["shape"] = sequence_input("shape")
            self.inputs.append(self.input_layers["shape"])
            self.embedding_layer.append(K.layers.Embedding(output_dim=shape_embedding_dim,
                                                           input_dim=shape_input_dim,
                                                           mask_zero=True,
                                                           name="shape_embedding")(self.input_layers["shape"]))

        if len(self.embedding_layer) > 1:
            self.middle_layer = K.layers.concatenate(self.embedding_layer, name="embedding_layer")
        else:
            self.middle_layer = self.embedding_layer[0]

        if embedding_layer_dropout > 0:
            self.middle_layer = K.layers.Dropout(embedding_layer_dropout, name="embedding_dropout")(self.middle_layer)

        lstm = K.layers.LSTM(lstm_units, return_sequences=True, recurrent_dropout=lstm_recurrent_dropout)
        lstm = K.layers.Bidirectional(lstm, name="bilstm")(self.middle_layer)

        if use_residual:
            self.middle_layer = K.layers.concatenate([self.middle_layer, lstm], name="residual")
        else:
            self.middle_layer = lstm

        if middle_layer_dropout > 0:
            self.middle_layer = K.layers.Dropout(middle_layer_dropout, name="bilstm_dropout")(self.middle_layer)

        self.output_layer = CRF(number_of_labels, learn_mode="marginal", name='label')(self.middle_layer)

        self.model = K.Model(inputs=self.inputs, outputs=[self.output_layer])
        self.model.compile(optimizer=K.optimizers.Adam(),
                           loss=crf_loss,
                           metrics=[crf_marginal_accuracy])

    def fit(self, x, y, epochs=1, batch_size=32, validation_split=0, shuffle=True):
        self.model.fit(x, y,
                       epochs=epochs,
                       batch_size=batch_size,
                       validation_split=validation_split,
                       shuffle=shuffle,
                       callbacks=[K.callbacks.EarlyStopping(monitor="loss", patience=4)])

    def summary(self):
        return self.model.summary()

    def save(self, location):
        save_model(self.model, location)
