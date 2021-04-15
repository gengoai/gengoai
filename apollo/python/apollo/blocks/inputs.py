from typing import Iterable, List, Dict, Any

import keras as K
import numpy as np

from apollo.blocks.base import InputBlock, pad_list
from apollo.layers import ElmoEmbeddingLayer


class InputBlockList:

    def __init__(self, blocks: Iterable[InputBlock]):
        self.__blocks = blocks
        self.__input_layers = dict()
        self.__model_layers = dict()

    def input_layers(self) -> List[K.layers.Layer]:
        return list(self.__input_layers.values())

    def input_layer(self, name) -> K.layers.Layer:
        return self.__input_layers[name]

    def model_layers(self) -> K.layers.Layer:
        return list(self.__model_layers.values())

    def model_layer(self, name) -> List[K.layers.Layer]:
        return self.__model_layers[name]

    def concatenated_model_layer(self) -> K.layers.Layer:
        el = self.model_layers()
        if len(el) == 1:
            return el[0]
        return K.layers.concatenate(el)

    def get_input_data(self, dataset: 'apollo.data.DataSet') -> Dict[str, Any]:
        inputs = dict()
        for block in self.__blocks:
            for name in block.observations():
                inputs[name] = dataset[name]
        return inputs

    def __call__(self, dataset: 'apollo.data.DataSet') -> 'InputBlockList':
        for block in self.__blocks:
            block(dataset)
            self.__input_layers.update(block.input_layers())
            self.__model_layers.update(block.model_layers())
        return self


class FeatureVector(InputBlock):

    def __init__(self,
                 input: str,
                 sequence: bool = False,
                 max_sequence_length: int = 112):
        super().__init__()
        self.__input = input
        self.__is_sequence = sequence
        self.__max_sequence_length = max_sequence_length
        self.__dimension = None

    def __call__(self, dataset: 'apollo.data.DataSet') -> None:
        self.reset()
        self.__dimension = dataset.dimension(self.__input)
        if self.__is_sequence:
            dataset[self.__input] = K.preprocessing.sequence.pad_sequences(dataset[self.__input],
                                                                           maxlen=self.__max_sequence_length,
                                                                           padding="post",
                                                                           truncating="post")
        else:
            dataset[self.__input] = np.array(dataset[self.__input])

    def _create_input_layers(self) -> Dict[str, K.layers.Layer]:
        if self.__is_sequence:
            return {self.__input: K.layers.Input(shape=(None, self.__dimension), name=self.__input)}
        else:
            return {self.__input: K.layers.Input(shape=(self.__dimension,), name=self.__input)}

    def _create_model_layers(self) -> Dict[str, K.layers.Layer]:
        return self.input_layers()

    def observations(self) -> Iterable[str]:
        return [self.__input]


class WordEmbedding(InputBlock):

    def __init__(self,
                 input: str,
                 sequence: bool = False,
                 mask_zero: bool = True,
                 max_sequence_length: int = 112,
                 dimension: int = 100,
                 glove: bool = True,
                 glove_path: str = "embeddings/glove%s.npy"):
        super(WordEmbedding, self).__init__()
        self.__input = input
        self.__is_sequence = sequence
        self.__max_sequence_length = max_sequence_length
        self.__dimension = dimension
        self.__use_glove = glove
        self.__vocab_size = None
        self.__mask_zero = mask_zero
        self.__glove_path = glove_path

    def _create_input_layers(self) -> Dict[str, K.layers.Layer]:
        if self.__is_sequence:
            return {self.__input: K.layers.Input(shape=(None,), name=self.__input)}
        else:
            return {self.__input: K.layers.Input(shape=(self.__dimension,), name=self.__input)}

    def _create_model_layers(self) -> Dict[str, K.layers.Layer]:
        il = self.input_layers()[self.__input]
        if self.__use_glove:
            from apollo.layers import GloveEmbedding
            return {self.__input: GloveEmbedding(dimension=self.__dimension, glove_path=self.__glove_path,
                                                 mask_zero=self.__mask_zero)(il)}
        else:
            return {self.__input: K.layers.Embedding(input_dim=self.__vocab_size,
                                                     output_dim=self.__dimension,
                                                     trainable=True,
                                                     mask_zero=self.__mask_zero,
                                                     name=self.__input + "_embedding")(il)}

    def __call__(self, data: 'apollo.data.DataSet') -> None:
        self.reset()
        self.__vocab_size = data.dimension(self.__input)
        if self.__is_sequence:
            data[self.__input] = K.preprocessing.sequence.pad_sequences(data[self.__input],
                                                                        maxlen=self.__max_sequence_length,
                                                                        padding="post",
                                                                        truncating="post")
        else:
            data[self.__input] = np.array(data[self.__input])

    def observations(self) -> Iterable[str]:
        return [self.__input]


class BiLSTMCharEmbedding(InputBlock):

    def __init__(self,
                 input: str,
                 max_word_length=5,
                 mask_zero: bool = True,
                 sequence: bool = False,
                 max_sequence_length: int = 112,
                 dimension: int = 10,
                 lstm_units: int = 10,
                 recurrent_dropout: float = 0.5):
        super(BiLSTMCharEmbedding, self).__init__()
        self.__input = input
        self.__is_sequence = sequence
        self.__max_sequence_length = max_sequence_length
        self.__dimension = dimension
        self.__vocab_size = None
        self.__mask_zero = mask_zero
        self.__max_word_length = max_word_length
        self.__lstm_units = lstm_units
        self.__recurrent_dropout = recurrent_dropout

    def _create_input_layers(self) -> Dict[str, K.layers.Layer]:
        shape = (None, self.__max_word_length) if self.__is_sequence else (self.__max_word_length,)
        return {self.__input: K.layers.Input(shape=shape, name=self.__input)}

    def _create_model_layers(self) -> Dict[str, K.layers.Layer]:
        il = self.input_layers()[self.__input]
        embedding_layer = K.layers.Embedding(input_dim=self.__vocab_size,
                                             output_dim=self.__dimension,
                                             input_length=self.__max_word_length,
                                             trainable=True,
                                             mask_zero=self.__mask_zero,
                                             name=self.__input + "_embedding")
        lstm = K.layers.Bidirectional(K.layers.LSTM(units=self.__lstm_units,
                                                    return_sequences=False,
                                                    recurrent_dropout=self.__recurrent_dropout,
                                                    name=self.__input + "_bilstm"))

        if self.__is_sequence:
            embedding_layer = K.layers.TimeDistributed(embedding_layer, name=self.__input + "_embedding_over_time")(il)
            lstm = K.layers.TimeDistributed(lstm, name=self.__input + "_bilstm_over_time")(embedding_layer)
        else:
            embedding_layer = embedding_layer(il)
            lstm = lstm(embedding_layer)

        return {self.__input: lstm}

    def __call__(self, data: 'apollo.data.DataSet') -> None:
        self.reset()
        self.__vocab_size = data.dimension(self.__input)
        x = []
        for ex in data[self.__input]:
            if len(ex.shape) == 1:
                ex = ex.reshape(ex.shape[0], 1)
            ex = K.preprocessing.sequence.pad_sequences(ex,
                                                        maxlen=self.__max_word_length,
                                                        padding="post",
                                                        truncating="post")
            if self.__is_sequence:
                ex = K.preprocessing.sequence.pad_sequences(ex.T,
                                                            maxlen=self.__max_sequence_length,
                                                            padding="post",
                                                            truncating="post").T
            x.append(ex)

        x = np.array(x)
        if self.__is_sequence:
            x = np.array(x).reshape([len(x), self.__max_sequence_length, self.__max_word_length])

        data[self.__input] = x

    def observations(self) -> Iterable[str]:
        return [self.__input]


class ElmoTokenEmbedding(InputBlock):

    def __init__(self,
                 tokens_observation: str = "tokens",
                 seq_len_observation: str = "seq_len",
                 max_sequence_length: int = 112,
                 trainable: bool = False):
        super(ElmoTokenEmbedding, self).__init__()
        self.__max_sequence_length = max_sequence_length
        self.__tokens = tokens_observation
        self.__seq_len = seq_len_observation
        self.__trainable = trainable

    def __call__(self, data: 'apollo.data.DataSet') -> None:
        self.reset()
        data[self.__tokens] = np.array(
            [pad_list(x, self.__max_sequence_length, "--PAD--") for x in data[self.__tokens]])
        data[self.__seq_len] = np.minimum(np.array(data[self.__seq_len]), self.__max_sequence_length)

    def observations(self) -> Iterable[str]:
        return [self.__tokens, self.__seq_len]

    def _create_input_layers(self) -> Dict[str, K.layers.Layer]:
        return {self.__tokens: K.layers.Input(shape=(None,), dtype="string", name=self.__tokens),
                self.__seq_len: K.layers.Input(shape=(1,), dtype="int32", name=self.__seq_len)}

    def _create_model_layers(self) -> Dict[str, K.layers.Layer]:
        inputs = self.input_layers()
        return {self.__tokens: ElmoEmbeddingLayer(trainable=self.__trainable)(
            [inputs[self.__tokens], inputs[self.__seq_len]])}
