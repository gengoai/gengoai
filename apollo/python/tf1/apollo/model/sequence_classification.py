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
from typing import List

import keras as K

from apollo.layers import sequence_input, instance_input, \
    ElmoEmbeddingLayer
from apollo.model.base_model import ApolloModel, BaseBiLstm


class ElmoMLP(ApolloModel):

    def __init__(self,
                 number_of_labels: int,
                 mlp_layers: List[int] = None,
                 elmo_is_trainable: bool = False,
                 optimizer: K.optimizers.Optimizer = K.optimizers.Adam()):
        super(ElmoMLP, self).__init__()

        self.input_layers = {
            "tokens": sequence_input("tokens"),
            "seq_len": instance_input(1, name="seq_len")
        }

        self.elmo = ElmoEmbeddingLayer(trainable=elmo_is_trainable)
        self.middle_layer = self.elmo

        if not mlp_layers:
            mlp_layers = [self.elmo.dimensions]
        for units in mlp_layers:
            self.middle_layer = K.layers.Dense(units, activation="tanh")(self.middle_layer)

        if number_of_labels <= 2:
            self.output_layer = K.layers.Dense(number_of_labels, activation="sigmoid")(self.middle_layer)
            loss_function = K.losses.binary_crossentropy
        else:
            self.output_layer = K.layers.Dense(number_of_labels, activation="softmax")(self.middle_layer)
            loss_function = K.losses.categorical_crossentropy

        self.model = K.Model(inputs=[self.input_layers["tokens"], self.input_layers["seq_len"]],
                             outputs=[self.output_layer])
        self.model.compile(optimizer=optimizer,
                           loss=loss_function,
                           metrics=[K.metrics.categorical_accuracy])


class BiLstmMLP(BaseBiLstm):

    def __init__(self,
                 number_of_labels: int,
                 lstm_units: int = 100,
                 mlp_layers: List[int] = None,
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
                 lstm_recurrent_dropout: int = 0.5,
                 optimizer: K.optimizers.Optimizer = K.optimizers.Adam()):
        super(BiLstmMLP, self).__init__(lstm_units, use_residual, word_embedding_dim, use_chars,
                                        char_input_dim, char_embedding_dim, max_word_length, use_shape, shape_input_dim,
                                        shape_embedding_dim, middle_layer_dropout, lstm_recurrent_dropout)

        if not mlp_layers:
            mlp_layers = [lstm_units]

        for units in mlp_layers:
            self.middle_layer = K.layers.Dense(units, activation="tanh")(self.middle_layer)

        if number_of_labels <= 2:
            self.output_layer = K.layers.Dense(number_of_labels, activation="sigmoid")(self.middle_layer)
            loss_function = K.losses.binary_crossentropy
        else:
            self.output_layer = K.layers.Dense(number_of_labels, activation="softmax")(self.middle_layer)
            loss_function = K.losses.categorical_crossentropy

        self.model = K.Model(inputs=self.inputs, outputs=[self.output_layer])
        self.model.compile(optimizer=optimizer,
                           loss=loss_function,
                           metrics=[K.metrics.categorical_accuracy])
