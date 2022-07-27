#!/usr/bin/env bash

if [[ ! -d "glove.6B" ]]; then
  if [[ ! -f "glove.6B.zip" ]]; then
    wget http://nlp.stanford.edu/data/glove.6B.zip
  fi
  unzip glove.6B.zip -d glove.6B/
fi

if [[ ! -d "embeddings" ]]; then
  mkdir "embeddings" || exit
fi

python3 utils/glove.py embeddings/ glove.6B
rm -rf glove.6B
