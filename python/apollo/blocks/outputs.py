from typing import Iterable, List, Dict, Any, Union, Callable

import keras as K
import numpy as np

from apollo.blocks.base import OutputBlock, pad_list


class OutputBlockList:

    def __init__(self, blocks: Iterable[OutputBlock]):
        self.__blocks = blocks
        self.__output_layers = dict()
        self.loses = dict()
        self.metrics = dict()

    def output_layers(self) -> List[K.layers.Layer]:
        return list(self.__output_layers.values())

    def output_layer(self, name) -> K.layers.Layer:
        return self.__output_layers[name]

    def get_output_data(self, dataset: 'apollo.data.DataSet') -> Dict[str, Any]:
        outputs = dict()
        for block in self.__blocks:
            for name in block.observations():
                outputs[name] = dataset[name]
        return outputs

    def __call__(self, dataset: 'apollo.data.DataSet') -> 'OutputBlockList':
        for block in self.__blocks:
            block(dataset)
            self.__output_layers.update(block.output_layers())
            self.loses.update(block.losses())
            self.metrics.update(block.metrics())
        return self


class CRFSequenceOutput(OutputBlock):

    def __init__(self, observation: str, max_sequence_length=112):
        super(CRFSequenceOutput, self).__init__()
        self.__output = observation
        self.__label_size = 0
        self.__max_sequence_length = max_sequence_length

    def losses(self) -> Dict[str, Any]:
        from keras_contrib.losses import crf_loss
        return {self.__output: crf_loss}

    def metrics(self) -> Dict[str, Any]:
        from keras_contrib.metrics import crf_accuracy
        return {self.__output: crf_accuracy}

    def _create_output_layers(self) -> Dict[str, K.layers.Layer]:
        from keras_contrib.layers import CRF
        return {self.__output: CRF(self.__label_size, learn_mode="marginal", name=self.__output)}

    def observations(self) -> Iterable[str]:
        return [self.__output]

    def __call__(self, data: 'apollo.data.DataSet') -> None:
        name = self.__output
        self.__label_size = data.dimension(name)
        data[name] = np.array([K.utils.to_categorical(pad_list(x, self.__max_sequence_length, 0), self.__label_size)
                               for x in data[name]])


class CategoricalOutput(OutputBlock):

    def __init__(self,
                 observation: str):
        super(CategoricalOutput, self).__init__()
        self.__output = observation
        self.__label_size = 0

    def losses(self) -> Dict[str, Any]:
        if self.__label_size <= 2:
            return {self.__output: "binary_crossentropy"}
        return {self.__output: "categorical_crossentropy"}

    def metrics(self) -> Dict[str, Any]:
        return {self.__output: "accuracy"}

    def _create_output_layers(self) -> Dict[str, K.layers.Layer]:
        activation = "sigmoid" if self.__label_size <= 2 else "softmax"
        return {self.__output: K.layers.Dense(self.__label_size, activation=activation, name=self.__output)}

    def observations(self) -> Iterable[str]:
        return [self.__output]

    def __call__(self, data: 'apollo.data.DataSet') -> None:
        name = self.__output
        self.__label_size = data.dimension(name)
        data[name] = K.utils.to_categorical(np.array(data[name]), num_classes=self.__label_size)
