from apollo.blocks import WordEmbedding, LSTM, BiLSTMCharEmbedding, Sequence, Residual
from apollo.data import ApolloSQLDataSet
from apollo.model import SequenceLabeler, InputBlockList

input_blocks = InputBlockList([
    WordEmbedding("words", sequence=True, mask_zero=True),
    BiLSTMCharEmbedding("chars", sequence=True, mask_zero=True, max_word_length=10, dimension=20),
    WordEmbedding("shape", sequence=True, mask_zero=True, glove=False, dimension=50)
])

arch = Sequence([
    Residual(LSTM(200, bidirectional=True, recurrent_dropout=0.5)),
    Residual(LSTM(200, bidirectional=True, recurrent_dropout=0.5))
])

model = SequenceLabeler(inputs=input_blocks, label="label", architecture=arch)
dataset = ApolloSQLDataSet('data/entity2.db')
model.fit(dataset, epochs=10,  validation_split=0, batch_size=100)
model.save('models/ner', overwrite=True)


