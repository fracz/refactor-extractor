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

input_rows_lengths = list(map(lambda row: len(row), input_rows)) # uzyskujemy liczbe TOKENOW w kazdej probce

num_input = len(input_rows) # liczba probek uczacych
longest_row_length = max(input_rows_lengths) # najdluzsza probka tokenow

print("Loaded samples: " + str(num_input) + ", longest sample: " + str(longest_row_length))

vocabulary_size = 128 # liczba tokenow w kodzie
embedding_size = 100 # rozmiar wektora wejsciowego (arbitralny chyba?)
num_hidden = 128
num_classes = 2

learning_rate = 0.001
training_steps = 10000
batch_size = 10 #128
display_step = 1#200

# https://www.tensorflow.org/tutorials/word2vec#building_the_graph
embeddings = tf.Variable(tf.random_uniform([vocabulary_size, embedding_size], -1.0, 1.0))
#nce_weights = tf.Variable(tf.truncated_normal([vocabulary_size, embedding_size], stddev=1.0 / math.sqrt(embedding_size)))
#nce_biases = tf.Variable(tf.zeros([vocabulary_size]))

train_inputs = tf.placeholder(tf.int32, shape=[longest_row_length])

# https://github.com/aymericdamien/TensorFlow-Examples/blob/master/examples/3_NeuralNetworks/bidirectional_rnn.py
X = tf.placeholder(tf.float32, [None, longest_row_length, num_input])
Y = tf.placeholder(tf.float32, [None, num_classes])
# https://github.com/aymericdamien/TensorFlow-Examples/blob/master/examples/3_NeuralNetworks/dynamic_rnn.py
seqlen = tf.placeholder(tf.int32, [None])

embed = tf.nn.embedding_lookup(embeddings, train_inputs)


weights = {
    # Hidden layer weights => 2*n_hidden because of forward + backward cells
    'out': tf.Variable(tf.random_normal([2*num_hidden, num_classes]))
}
biases = {
    'out': tf.Variable(tf.random_normal([num_classes]))
}

def BiRNN(x, seqlen, weights, biases):
    exit()
    inputs = tf.unstack(x, longest_row_length, 1)
    lstm_fw_cell = rnn.BasicLSTMCell(num_hidden, forget_bias=1.0)
    lstm_bw_cell = rnn.BasicLSTMCell(num_hidden, forget_bias=1.0)
    outputs, _, _ = rnn.static_bidirectional_rnn(lstm_fw_cell, lstm_bw_cell, x, dtype=tf.float32, sequence_length=seqlen)
    return tf.matmul(outputs[-1], weights['out']) + biases['out']

logits = BiRNN(X, seqlen, weights, biases)
prediction = tf.nn.softmax(logits)

loss_op = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=logits, labels=INPUT_EXPECTED))
optimizer = tf.train.GradientDescentOptimizer(learning_rate=learning_rate)
train_op = optimizer.minimize(loss_op)

correct_pred = tf.equal(tf.argmax(prediction, 1), tf.argmax(Y, 1))
accuracy = tf.reduce_mean(tf.cast(correct_pred, tf.float32))
init = tf.global_variables_initializer()

with tf.Session() as sess:
    sess.run(init)

    for step in range(1, training_steps+1):
        startIndex = step * batch_size % num_input
        if startIndex + batch_size >= num_input:
            startIndex = 0
        batch_x = input_rows[startIndex:startIndex+batch_size]
        batch_y = expected_result[startIndex:startIndex+batch_size]
        batch_seqlen = input_rows_lengths[startIndex:startIndex+batch_size]
        # Reshape data to get 28 seq of 28 elements
        batch_INPUT = batch_INPUT.reshape((batch_size, longest_row_length, num_input))
        # Run optimization op (backprop)
        sess.run(train_op, feed_dict={X: batch_x, Y: batch_y, seqlen: batch_seqlen})
        if step % display_step == 0 or step == 1:
            # Calculate batch loss and accuracy
            loss, acc = sess.run([loss_op, accuracy], feed_dict={X: batch_INPUT,
                                                                 Y: batch_EXPECTED})
            print("Step " + str(step) + ", Minibatch Loss= " + \
                  "{:.4f}".format(loss) + ", Training Accuracy= " + \
                  "{:.3f}".format(acc))

    print("Optimization Finished!")

    # Calculate accuracy for 128 mnist test images
    test_len = 128
    test_data = mnist.test.images[:test_len].reshape((-1, timesteps, num_input))
    test_label = mnist.test.labels[:test_len]
    print("Testing Accuracy:", \
        sess.run(accuracy, feed_dict={X: test_data, Y: test_label}))
