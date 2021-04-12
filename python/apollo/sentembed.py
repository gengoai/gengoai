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
from apollo.data import ApolloSQLDataSet
from apollo.layers import GloveEmbedding

data = ApolloSQLDataSet("/Users/ik/snli.db")

# data["label"] = np.array(data["label"])
# dfdsf
# data["w1"] = K.preprocessing.sequence.pad_sequences(data["w1"],
#                                                     maxlen=512,
#                                                     padding="post",
#                                                     truncating="post")
# data["w2"] = K.preprocessing.sequence.pad_sequences(data["w2"],
#                                                     maxlen=512,
#                                                     padding="post",
#                                                     truncating="post")
# data["s1"] = K.preprocessing.sequence.pad_sequences(data["s1"],
#                                                     maxlen=512,
#                                                     padding="post",
#                                                     truncating="post")
# data["s2"] = K.preprocessing.sequence.pad_sequences(data["s2"],
#                                                     maxlen=512,
#                                                     padding="post",
#                                                     truncating="post")
#
# c1 = []
# for ex in data["c1"]:
#     ex = K.preprocessing.sequence.pad_sequences(ex,
#                                                 maxlen=10,
#                                                 padding="post",
#                                                 truncating="post")
#     ex = K.preprocessing.sequence.pad_sequences(ex.T,
#                                                 maxlen=512,
#                                                 padding="post",
#                                                 truncating="post").T
#
#     c1.append(ex)
# c1 = np.array(c1)
# c1 = np.array(c1).reshape([len(c1), 512, 10])
#
# c2 = []
# for ex in data["c2"]:
#     ex = K.preprocessing.sequence.pad_sequences(ex,
#                                                 maxlen=10,
#                                                 padding="post",
#                                                 truncating="post")
#     ex = K.preprocessing.sequence.pad_sequences(ex.T,
#                                                 maxlen=512,
#                                                 padding="post",
#                                                 truncating="post").T
#
#     c2.append(ex)
# c2 = np.array(c2)
# c2 = np.array(c2).reshape([len(c2), 512, 10])

w1 = K.layers.Input(shape=(None,), name="w1")
ci1 = K.layers.Input(shape=(None, 10), name="c1")
shape1 = K.layers.Input(shape=(None,), name="s1")

w2 = K.layers.Input(shape=(None,), name="w2")
ci2 = K.layers.Input(shape=(None, 10), name="c2")
shape2 = K.layers.Input(shape=(None,), name="s2")

char_vocab_size = data.dimension("c1")

glove = GloveEmbedding(dimension=100, glove_path="embeddings/glove%s.npy", mask_zero=True)
w1_embedding = glove(w1)
w2_embedding = glove(w2)

char_embedding = K.layers.Embedding(input_dim=char_vocab_size,
                                    output_dim=20,
                                    input_length=10,
                                    trainable=True,
                                    mask_zero=True,
                                    name="chars_embedding")
char_embedding = K.layers.TimeDistributed(char_embedding, name="chars_embedding_over_time")

lstm = K.layers.Bidirectional(K.layers.LSTM(units=10,
                                            return_sequences=False,
                                            recurrent_dropout=0.5,
                                            name="chars_bilstm"))
lstm = K.layers.TimeDistributed(lstm, name="chars_bilstm_over_time")

c1_embedding = lstm(char_embedding(ci1))
c2_embedding = lstm(char_embedding(ci2))

emb = K.layers.Embedding(output_dim=50,
                         input_dim=data.dimension("s1"),
                         mask_zero=True,
                         name="shape_embedding")
shape1_embedding = emb(shape1)
shape2_embedding = emb(shape2)

sentence_embedding = K.layers.Bidirectional(K.layers.LSTM(units=200,
                                                          return_sequences=True,
                                                          recurrent_dropout=0.5),
                                            name="sentence_embedding")

s1 = K.layers.Concatenate()([w1_embedding, c1_embedding, shape1_embedding])
s1 = sentence_embedding(s1)
s1 = K.layers.GlobalAvgPool1D()(s1)

s2 = K.layers.Concatenate()([w2_embedding, c2_embedding, shape2_embedding])
s2 = sentence_embedding(s2)
s2 = K.layers.GlobalAvgPool1D()(s2)

dot = K.layers.Dot(axes=1, normalize=True)([s1, s2])
dot = K.layers.Dense(1, activation=K.activations.sigmoid)(dot)
model = K.Model(inputs=[w1, ci1, shape1,
                        w2, ci2, shape2], outputs=dot)

model.compile(optimizer="adam", loss=K.losses.cosine_similarity, metrics=K.metrics.CosineSimilarity())
print(model.summary())


model.fit(x={
    "w1": data["w1"],
    "w2": data["w2"],
    "s1": data["s1"],
    "s2": data["s2"],
    "c1": data["c1"],
    "c2": data["c2"],
}, y=[data["label"]], epochs=1)
