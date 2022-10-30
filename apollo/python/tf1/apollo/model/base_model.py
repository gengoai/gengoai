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

from apollo.layers import sequence_input, char_sequence_input, GloveEmbedding, CharEmbedding
from apollo.model_io import save_model


class ApolloModel:

    def __init__(self):
        self.model = None

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


class BaseBiLstm(ApolloModel):

    def __init__(self,
                 lstm_units: int = 100,
                 use_residual: bool = True,
                 word_embedding_dim: int = 100,
                 use_chars: bool = True,
                 char_input_dim: int = 0,
                 char_embedding_dim: int = 10,
                 max_word_length: int = 20,
                 use_shape: bool = True,
                 shape_input_dim: int = 0,
                 shape_embedding_dim: int = 10,
                 middle_layer_dropout: int = 0.2,
                 lstm_recurrent_dropout: int = 0.5):
        super(BaseBiLstm, self).__init__()
        self.input_layers = {
            "words": sequence_input("words")
        }
        self.word_embedding = GloveEmbedding(dimension=word_embedding_dim)(self.input_layers["words"])

        if use_chars:
            self.input_layers["chars"] = char_sequence_input(max_word_length, "chars")
            self.char_embedding = CharEmbedding(input_dim=char_input_dim,
                                                output_dim=char_embedding_dim,
                                                input_length=max_word_length)(self.input_layers["chars"])
        if use_shape:
            self.input_layers["shape"] = sequence_input("shape")
            self.shape_embedding = K.layers.Embedding(output_dim=shape_embedding_dim,
                                                      input_dim=shape_input_dim,
                                                      mask_zero=True,
                                                      name="shape_embedding")(self.input_layers["shape"])

        if use_shape and use_chars:
            self.middle_layer = K.layers.concatenate([self.word_embedding,
                                                      self.char_embedding,
                                                      self.shape_embedding])
        elif use_shape:
            self.middle_layer = K.layers.concatenate([self.word_embedding,
                                                      self.shape_embedding])
        elif use_chars:
            self.middle_layer = K.layers.concatenate([self.word_embedding,
                                                      self.char_embedding])
        else:
            self.middle_layer = self.word_embedding

        lstm = K.layers.LSTM(lstm_units, return_sequences=True, recurrent_dropout=lstm_recurrent_dropout)
        lstm = K.layers.Bidirectional(lstm)(self.middle_layer)

        if use_residual:
            self.middle_layer = K.layers.concatenate([self.middle_layer, lstm])
            self.middle_layer = K.layers.Dropout(middle_layer_dropout)(self.middle_layer)
        else:
            self.middle_layer = lstm

        self.inputs = [self.input_layers["words"]]
        if use_chars:
            self.inputs.append(self.input_layers["chars"])
        if use_shape:
            self.inputs.append(self.input_layers["shape"])
