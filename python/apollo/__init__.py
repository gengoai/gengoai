import os
import tensorflow as tf
from apollo.model import ApolloModel

os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'
tf.get_logger().setLevel('ERROR')
