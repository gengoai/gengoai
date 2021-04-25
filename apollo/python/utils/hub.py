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
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
# from tensorflow.saved_model import simple_save

import tensorflow.compat.v1 as tf

tf.disable_v2_behavior()
import tensorflow_hub as hub


def save_module(url, save_path, input_type):
    with tf.Graph().as_default():
        module = hub.Module(url)
        model_input = tf.placeholder(input_type, name="input")
        model_output = tf.identity(module(model_input), name="output")
        with tf.Session() as session:
            session.run(tf.global_variables_initializer())
            tf.saved_model.simple_save(
                session,
                save_path,
                inputs={'input': model_input},
                outputs={'output': model_output},
                legacy_init_op=tf.initializers.tables_initializer(name='init_all_tables'))


def create_elmo_token_embedding(save_path):
    with tf.Graph().as_default():
        elmo = hub.Module("https://tfhub.dev/google/elmo/3")
        model_input = {"tokens": tf.compat.v1.placeholder(tf.string, name="tokens"),
                       "sequence_len": tf.compat.v1.placeholder(tf.int32, name="sequence_len")}
        model_output = tf.identity(elmo(inputs=model_input, signature="tokens", as_dict=True)["elmo"], name="output")
        print(model_output)

        with tf.Session() as session:
            session.run(tf.global_variables_initializer())
            tf.saved_model.simple_save(
                session,
                save_path,
                inputs=model_input,
                outputs={'output': model_output},
                legacy_init_op=tf.initializers.tables_initializer(name='init_all_tables'))


save_module("https://tfhub.dev/google/universal-sentence-encoder-large/3", "/Users/ik/use", tf.string)
# create_elmo_token_embedding("/Users/ik/elmo")
# save_module("https://tfhub.dev/google/elmo/3", "elmo", tf.string)
