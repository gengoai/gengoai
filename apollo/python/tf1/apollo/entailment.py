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
from keras.layers.normalization import BatchNormalization
from keras.regularizers import l2

from apollo.data import ApolloSQLDataSet, pad
from apollo.layers import sequence_input, GloveEmbedding

# data = ApolloSQLDataSet("../data/snli_data.db")
#
max_sequence_length = 50
word_dimensions = 300
sentence_hidden_size = 300
# DP = 0.2
# L2 = 4e-6
#
# input_data = {
#     "w1": pad(data["w1"], max_sequence_length),
#     "w2": pad(data["w2"], max_sequence_length),
# }
#
# input_layers = {
#     "w1": sequence_input("w1"),
#     "w2": sequence_input("w2"),
# }
#
# embed = GloveEmbedding(dimension=word_dimensions,
#                        glove_path="../embeddings/glove%s.npy",
#                        mask_zero=True)
# translate = K.layers.TimeDistributed(
#     K.layers.Dense(word_dimensions, activation='relu'), name="word_embedding")
#
# premise = translate(embed(input_layers["w1"]))
# hypothesis = translate(embed(input_layers["w2"]))
#
# # Sentence Embeddings
# sentence_embed = K.layers.Lambda(lambda x: K.backend.sum(x, axis=1),
#                                  output_shape=(sentence_hidden_size,))
# premise = sentence_embed(premise)
# hypothesis = sentence_embed(hypothesis)
#
# premise = BatchNormalization()(premise)
# hypothesis = BatchNormalization()(hypothesis)
#
# # Output Label
# out = K.layers.concatenate([premise, hypothesis])
# out = K.layers.Dropout(DP)(out)
#
# for i in range(3):
#     out = K.layers.Dense(2 * sentence_hidden_size, activation="relu",
#                          W_regularizer=l2(4e-6))(out)
#     out = K.layers.Dropout(DP)(out)
#     out = BatchNormalization()(out)
#
# out = K.layers.Dense(3, activation="softmax")(out)
#
# model = K.Model(inputs=list(input_layers.values()), outputs=[out])
# model.compile(optimizer="rmsprop",
#               loss=K.losses.sparse_categorical_crossentropy,
#               metrics=[K.metrics.sparse_categorical_accuracy])
# print(model.summary())
#
# model.fit(x=input_data,
#           y=[np.array(data["label"])],
#           validation_split=0,
#           batch_size=512,
#           epochs=22,
#           callbacks=[K.callbacks.EarlyStopping(monitor='loss', patience=3,
#                                                restore_best_weights=True)])
model = K.models.load_model("entailment.h5")
data = ApolloSQLDataSet("../data/snli_test.db")
input_data = {
    "w1": pad(data["w1"], max_sequence_length),
    "w2": pad(data["w2"], max_sequence_length),
}
print(model.evaluate(x=input_data,
                     y=[np.array(data["label"])]))
model.save("entailment.h5", overwrite=True)
# with K.backend.get_session() as sess:
#     tf.saved_model.simple_save(
#         sess,
#         "models/test_model",
#         inputs={t.name: t for t in model.inputs},
#         outputs={t.name: t for t in model.outputs}
#     )
