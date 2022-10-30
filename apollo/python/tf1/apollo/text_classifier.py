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

from apollo.blocks import WordEmbedding, LSTM, BiLSTMCharEmbedding, Sequence, Residual, ConvNet
from apollo.data import ApolloSQLDataSet
from apollo.model import SequenceClassifier, InputBlockList

input_blocks = InputBlockList([
    WordEmbedding("words", sequence=True, mask_zero=False),
    BiLSTMCharEmbedding("chars", sequence=True, mask_zero=False, max_word_length=10, dimension=20),
    WordEmbedding("shape", sequence=True, mask_zero=False, glove=False, dimension=50)
])

arch = Sequence([
    Residual(LSTM(200, bidirectional=True, recurrent_dropout=0.5)),
    ConvNet(200, dropout=0.2)
])

model = SequenceClassifier(inputs=input_blocks, label="LABEL", architecture=arch)
dataset = ApolloSQLDataSet('data/sentiment.db')
model.fit(dataset, epochs=10, validation_split=0.2, batch_size=100)
