import tensorflow as tf
from tensorflow.contrib import rnn
import numpy as np
import csv
import math
from random import shuffle

INPUTS_PATH = '../input/ast/'
input_file = 'deleted.txt'

# czytamy pliki wejsciowe zapamietujac czy kod jest dobry. tworza sie listy:
# [ [[TOKENY],[DOBRY_KOD]], [[TOKENY],[DOBRY_KOD]], [[TOKENY],[ZLY_KOD]] ]
with open(INPUTS_PATH + 'changed-before.txt', 'r') as f:
    reader = csv.reader(f)
    input_changed_before = map(lambda row: [row, [0, 1]], list(reader)) # [0, 1] zly kod
with open(INPUTS_PATH + 'changed-after.txt', 'r') as f:
    reader = csv.reader(f)
    input_changed_after = map(lambda row: [row, [1, 0]], list(reader)) # [1, 0] dobry kod

input_tuples = list(input_changed_before) + list(input_changed_after) # laczymy liste dobrych i zlych kodow
shuffle(input_tuples) # mieszamy

input_rows = list(map(lambda row: list(map(lambda token: int(token), row[0])), input_tuples)) # wycigamy tylko TOKENY
expected_result = list(map(lambda row: row[1], input_tuples)) # wycigamy tylko informacje DOBRY/ZLY_KOD

input_rows_lengths = map(lambda row: len(row), input_rows) # uzyskujemy liczbe TOKENOW w kazdej probce

num_input = len(input_rows) # liczba probek uczacych
print(num_input)
longest_row_length = max(input_rows_lengths) # najdluzsza probka tokenow
vocabulary_size = 128 # liczba tokenow w kodzie
embedding_size = 100 # rozmiar wektora wejsciowego (arbitralny chyba?)

# https://www.tensorflow.org/tutorials/word2vec#building_the_graph
embeddings = tf.Variable(tf.random_uniform([vocabulary_size, embedding_size], -1.0, 1.0))
nce_weights = tf.Variable(tf.truncated_normal([vocabulary_size, embedding_size], stddev=1.0 / math.sqrt(embedding_size)))
nce_biases = tf.Variable(tf.zeros([vocabulary_size]))

# https://github.com/aymericdamien/TensorFlow-Examples/blob/master/examples/3_NeuralNetworks/bidirectional_rnn.py
INPUT = tf.placeholder("float", [None, longest_row_length, num_input])
OUTPUT = tf.placeholder("float", [None, 2])

embed = tf.nn.embedding_lookup(embeddings, input_rows)
