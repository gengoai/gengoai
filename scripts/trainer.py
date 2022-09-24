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

LANG = "en"
HERMES_RESOURCE_DIR = "/Volumes/ikdata/hermes/"
CORPUS_DIR = "/Volumes/ikdata/corpora/hermes_data"

models = dict()
models["POS"] = f"--model={HERMES_RESOURCE_DIR}/{LANG}/models/pos/"
models["PHRASE_CHUNK"] = f"--model={HERMES_RESOURCE_DIR}/{LANG}/models/phrase_chunk"
models["ENTITY"] = f"--model={HERMES_RESOURCE_DIR}/{LANG}/models/ner-crf"
models["TF_ENTITY"] = f"--model={HERMES_RESOURCE_DIR}/{LANG}/models/ner-reslstm"

tagger = dict()
tagger["POS"]="--tagger=EN_POS"
tagger["PHRASE_CHUNK"]="--tagger=PHRASE_CHUNK"
tagger["ENTITY"]="--tagger=ENTITY"
tagger["TF_ENTITY"]="--tagger=TF_ENTITY"

train = dict()
train["POS"]=f"--df=pos_opl::/{CORPUS_DIR}/{LANG}/part_of_speech/penn_treebank_train.txt"
train["PHRASE_CHUNK"]=f"--df=conll::{CORPUS_DIR}/{LANG}/phrase_chunk/train.txt "
train["ENTITY"]=f"--df=hjson_opl::{CORPUS_DIR}/{LANG}/named_entity/train.json_opl"
train["TF_ENTITY"]=f"--df=corpus::/data/corpora/hermes_data/ontonotes_ner --query=\$SPLIT='TRAIN'"