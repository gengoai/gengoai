from typing import List, Any

import keras as K
import tensorflow as tf
from tensorflow.python.framework.ops import Tensor

from apollo.blocks import CRFSequenceOutput, OutputBlockList, InputBlockList, CategoricalOutput
from apollo.data import DataSet


class ApolloModel:

    def __init__(self,
                 input_blocks: InputBlockList,
                 output_blocks: OutputBlockList):
        self.__keras_model = None
        self.inputs = input_blocks
        self.outputs = output_blocks

    def _build(self) -> List[Tensor]:
        raise NotImplementedError

    def fit(self,
            data: DataSet,
            epochs: int = 1,
            batch_size: int = 1,
            print_summary: bool = True,
            optimizer: Any = "adam",
            **kwargs):
        self.inputs(data)
        self.outputs(data)
        self.__keras_model = K.Model(inputs=self.inputs.input_layers(), outputs=self._build())
        self.__keras_model.compile(optimizer=optimizer, loss=self.outputs.loses, metrics=self.outputs.metrics)
        if print_summary:
            print(self.__keras_model.summary())
        self.__keras_model.fit(x=self.inputs.get_input_data(data),
                               y=self.outputs.get_output_data(data),
                               epochs=epochs,
                               batch_size=batch_size,
                               shuffle=True,
                               **kwargs)

    def save(self, location: str, overwrite: bool = False):
        if overwrite:
            import os
            import shutil
            if os.path.exists(location):
                shutil.rmtree(location, ignore_errors=True)
        with K.backend.get_session() as sess:
            tf.saved_model.simple_save(
                sess,
                location,
                inputs={t.name: t for t in self.__keras_model.inputs},
                outputs={t.name: t for t in self.__keras_model.outputs}
            )


class SequenceClassifier(ApolloModel):

    def __init__(self,
                 inputs: InputBlockList,
                 label: str,
                 architecture: 'apollo.blocks.ArchitectureBlock'):
        super(SequenceClassifier, self).__init__(inputs,
                                                 OutputBlockList([CategoricalOutput(label)]))
        self._arch = architecture
        self._label_observation = label

    def _build(self) -> List[Tensor]:
        from apollo.layers import MeanPool
        if self._arch.returns_sequences():
            out = self._arch(self.inputs.concatenated_model_layer())
            out = MeanPool()(out)
        else:
            out = MeanPool()(self.inputs.concatenated_model_layer())
            out = self._arch(out)

        out = self.outputs.output_layer(self._label_observation)(out)
        return [out]


class SequenceLabeler(ApolloModel):

    def __init__(self,
                 inputs: InputBlockList,
                 label: str,
                 architecture: 'apollo.blocks.ArchitectureBlock'):
        super(SequenceLabeler, self).__init__(inputs,
                                              OutputBlockList([CRFSequenceOutput(label)]))
        self._arch = architecture
        assert self._arch.returns_sequences(), "Sequence Labeler requires an architecture that returns sequences"

    def _build(self) -> List[Tensor]:
        out = self._arch(self.inputs.concatenated_model_layer())
        out = self.outputs.output_layer(label)(out)
        return [out]
