from typing import Any, Union, List, Dict, Iterable

import numpy as np


# ---------------------------------------------------------------------------------------------------
# Base Block Types
# ---------------------------------------------------------------------------------------------------


class Block:
    """
    A block can describe a single layer or multiple layers.
    """

    def reset(self) -> None:
        """
        Resets any data-specific parameters to the Block
        :return: None
        """
        raise NotImplementedError()

    def __call__(self, *args, **kwargs):
        raise NotImplementedError()


class ArchitectureBlock(Block):
    """
    An Architectural Block typically defines a combination of Keras Layers defining the main architecture of a neural
    model (i.e. all the stuff between the input and output). Architectural blocks act similar to Keras layers in
    functional mode, e.g. arch(Tensor). Thus, implementations should implement the **__call__** method taking a
    tensorflow Tensor as input and returning a Tensor as output. Optionally, the implementation can override the
    **returns_sequences** to indicate that the architecture generates sequences as output.

    """

    def reset(self) -> None:
        pass

    def __call__(self,
                 input_layer: 'tensorflow.python.framework.ops.Tensor') -> 'tensorflow.python.framework.ops.Tensor':
        raise NotImplementedError()

    def returns_sequences(self) -> bool:
        """
        Indicates whether the Architectural block returns a sequence as output or not.
        :return: bool - True if returns sequence, False otherwise
        """
        return False


class InputBlock(Block):
    """
    An InputBlock defines input(s) to a model. The input block will define an Input layer and Model layer where the
    input layer is a **keras.layer.Input** and a model layer can either be the same input or a keras layer(s) feed via
    the input (e.g. embedding).

    === Input and Model Layers ===

    The base input block class takes care of remembering the input and model layers so that are only created once.
    Implementing classes will implement the **_create_input_layers** and **_create_model_layers** methods to create the
    actual layers (these methods should call **input_layers** or **model_layers()** when needing to reference the input
    or model layers.

    ===  Building the Block ===

    In addition to defining the layers, input blocks should reshape the data as needed. This is done using by treating
    the InputBlock as a callable on an Dataset, e.g. input_block(dataset). The input block may also use this call to
    record information about the data (e.g. dimension information).

    """

    def __init__(self):
        super().__init__()
        self.__input_layers = None
        self.__model_layers = None

    def reset(self) -> None:
        self.__input_layers = None
        self.__model_layers = None

    def input_layers(self) -> Dict[str, 'keras.layers.Layer']:
        """
        Returns the input layers as a dict where keys are the name of the observation and values are the associated
        Keras Input. Child classes should not override this method, but instead override the ** _create_input_layers **.
        :return: dict of observation names to Keras Input
        """
        if self.__input_layers is None:
            self.__input_layers = self._create_input_layers()
        return self.__input_layers

    def _create_input_layers(self) -> Dict[str, 'keras.layers.Layer']:
        """
        Returns the input layers as a dict where keys are the name of the observation and values are the associated
        Keras Input.
        :return: dict of observation names to Keras Input
        """
        raise NotImplementedError()

    def model_layers(self) -> Dict[str, 'keras.layers.Layer']:
        """
         Returns the model layers as a dict where keys are the name of the observation and values are the associated
         Keras Layer. Child classes should not override this method, but instead override the ** _create_model_layers **
         :return: dict of observation names to Keras Layer
         """
        if self.__model_layers is None:
            self.__model_layers = self._create_model_layers()
        return self.__model_layers

    def _create_model_layers(self) -> Dict[str, 'keras.layers.Layer']:
        """
         Returns the model layers as a dict where keys are the name of the observation and values are the associated
         Keras Layer.
         :return: dict of observation names to Keras Layer
         """
        raise NotImplementedError()

    def observations(self) -> Iterable[str]:
        """
        Returns the input observation names.
        :return: the input observation names.
        """
        raise NotImplementedError()

    def __call__(self, data: 'apollo.DataSet'):
        """
        Process the dataset to modify the input observation data and record any data-specific information needed
        to create the input and model layers.
        :param data: the Apollo DataSet to process
        :return: None
        """
        raise NotImplementedError()


class OutputBlock(Block):
    """
    An OutputBlock defines outputs(s) of a model. The output block will define one or more output layers that will be
    feed by a Keras Layer (typically generated from ArchitectureBlock)

    === Output Layers ===

    The base output block class takes care of remembering the output layers so that are only created once.
    Implementing classes will implement the **_create_output_layers**  to create the actual layers.

    ===  Building the Block ===

    In addition to defining the layers, output blocks should reshape the data as needed. This is done using by treating
    the OutputBlock as a callable on an Dataset, e.g. output_block(dataset). The output block may also use this call to
    record information about the data (e.g. dimension information).
    """

    def __init__(self):
        super().__init__()
        self.__output_layers = None

    def reset(self) -> None:
        self.__output_layers = None

    def output_layers(self) -> Dict[str, 'keras.layers.Layer']:
        """
        Returns the output layers as a dict where keys are the name of the observation and values are the associated
        Keras Layer. Child classes should not override this method, but instead override the ** _create_output_layers **.
        :return: dict of observation names to Keras Layer
        """
        if self.__output_layers is None:
            self.__output_layers = self._create_output_layers()
        return self.__output_layers

    def losses(self) -> Dict[str, Any]:
        """
        Generates a dictionary mapping output observation names to their associated loss functions used for training.
        :return: a dictionary mapping output observation names to their associated loss functions used for training.
        """
        raise NotImplementedError()

    def metrics(self) -> Dict[str, Any]:
        """
        Generates a dictionary mapping output observation names to their associated metrics used for training.
        :return: a dictionary mapping output observation names to their associated metrics used for training.
        """
        raise NotImplementedError()

    def _create_output_layers(self) -> Dict[str, 'keras.layers.Layer']:
        """
         Returns the output layers as a dict where keys are the name of the observation and values are the associated
         Keras Layer.
         :return: dict of observation names to Keras Layer
         """
        raise NotImplementedError()

    def observations(self) -> Iterable[str]:
        """
        Returns the output observation names.
        :return: the output observation names.
        """
        raise NotImplementedError()

    def __call__(self, data: 'apollo.DataSet'):
        """
        Process the dataset to modify the output observation data and record any data-specific information needed
        to create the output layers.
        :param data: the Apollo DataSet to process
        :return: None
        """
        raise NotImplementedError()


# ---------------------------------------------------------------------------------------------------
# Helper Functions
# ---------------------------------------------------------------------------------------------------

def pad_list(l: Union[List[Any], np.ndarray],
             max_length: int,
             pad_value: Any) -> Union[List[Any], np.ndarray]:
    """
    Pads lists or numpy arrays to a given maximum length with a given value (padding is done at the end of the list /
    array).
    :param l: the list of numpy array to pad
    :param max_length: the maximum sequence length
    :param pad_value: the value to use for padding
    :return: the padded list or array
    """
    if isinstance(l, np.ndarray):
        return _pad_ndarray(l, max_length, pad_value)

    return _pad_list(l, max_length, pad_value)


def _pad_list(l: List[Any], max_length: int, pad_value: List[Any]) -> List[Any]:
    l = l[:max_length]
    while len(l) < max_length:
        l.append(pad_value)
    return l


def _pad_ndarray(n: np.ndarray, max_length: int, pad_value: np.ndarray) -> np.ndarray:
    if n.shape[0] == max_length:
        return n
    elif n.shape[0] > max_length:
        return n[:max_length]
    return np.pad(n, (pad_value, max_length - n.shape[0]), 'constant')
