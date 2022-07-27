from typing import List, Union

import keras as K
from tensorflow.python.framework.ops import Tensor

from apollo.blocks.base import ArchitectureBlock


class Concatenate(ArchitectureBlock):

    def __init__(self, blocks: List[Union[ArchitectureBlock, K.layers.Layer]]):
        self.__blocks = blocks

    def __call__(self, input_layer: Tensor):
        x = input_layer
        layers = []
        for block in self.__blocks:
            layers.append(block(x))
        return K.layers.concatenate(layers)

    def returns_sequences(self) -> bool:
        for block in self.__blocks:
            if not block.returns_sequences():
                return False
        return True


class Sequence(ArchitectureBlock):

    def __init__(self,
                 blocks: List[Union[ArchitectureBlock, K.layers.Layer]],
                 returns_sequences=None):
        self.__blocks = blocks
        self.__returns_sequences = returns_sequences

    def __call__(self, input_layer: Tensor):
        x = input_layer
        for block in self.__blocks:
            x = block(x)
        return x

    def returns_sequences(self) -> bool:
        if self.__returns_sequences is not None:
            return self.__returns_sequences
        i = -1
        while i >= 0:
            if isinstance(self.__blocks[i], ArchitectureBlock):
                return self.__blocks[-i].returns_sequences()
            i -= 1
        return True


class ConvNet(ArchitectureBlock):

    def __init__(self,
                 filters: int,
                 kernel_size: int = 5,
                 activation: str = "relu",
                 dilation_rate: int = 1,
                 dropout: float = 0.0):
        self.__units = filters
        self.__kernels = kernel_size
        self.__activation = activation
        self.__dilation_rate = dilation_rate
        self.__dropout = dropout

    def __call__(self, input_layer: Tensor):
        conv = K.layers.Convolution1D(self.__units,
                                      self.__kernels,
                                      activation=self.__activation,
                                      padding="same",
                                      dilation_rate=self.__dilation_rate)(input_layer)
        if self.__dropout > 0:
            return K.layers.Dropout(self.__dropout)(conv)
        return conv

    def returns_sequences(self) -> bool:
        return True


class MLP(ArchitectureBlock):

    def __init__(self,
                 hidden_layers: List[int],
                 activation: str = "relu"):
        self.__layers = [K.layers.Dense(n, activation=activation) for n in hidden_layers]

    def __call__(self, input_layer: Tensor):
        x = input_layer
        for layer in self.__layers:
            x = layer(x)
        return x


class LSTM(ArchitectureBlock):

    def __init__(self,
                 units: int,
                 return_sequences: bool = True,
                 bidirectional: bool = False,
                 recurrent_dropout: float = 0.2,
                 dropout: float = 0.2):
        self.__lstm = K.layers.LSTM(units=units,
                                    return_sequences=return_sequences,
                                    recurrent_dropout=recurrent_dropout,
                                    dropout=dropout)
        if bidirectional:
            self.__lstm = K.layers.Bidirectional(self.__lstm)

    def __call__(self, input_layer: Tensor):
        return self.__lstm(input_layer)

    def returns_sequences(self) -> bool:
        return self.__lstm.return_sequences


class Residual(ArchitectureBlock):

    def __init__(self, block: Union[ArchitectureBlock, K.layers.Layer]):
        self.__block = block

    def __call__(self, input_layer: Tensor):
        out = self.__block(input_layer)
        return K.layers.concatenate([input_layer, out])


class StackedResidualLSTM(ArchitectureBlock):

    def __init__(self,
                 units: int,
                 bidirectional: bool = False,
                 recurrent_dropout: float = 0.2,
                 dropout: float = 0.2):
        self.__lstm_1 = LSTM(units, True, bidirectional, recurrent_dropout, dropout)
        self.__lstm_2 = LSTM(units, True, bidirectional, recurrent_dropout, dropout)

    def __call__(self, input_layer: Tensor):
        lstm1 = self.__lstm_1(input_layer)
        x = K.layers.concatenate([input_layer, lstm1])
        y = self.__lstm_2(x)
        return K.layers.concatenate([input_layer, y])

    def returns_sequences(self) -> bool:
        return True
