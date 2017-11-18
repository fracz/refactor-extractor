commit 070acfad57169d1bfbb8f87076f63f7586a24400
Author: Pawel Koperek <pkoperek@gmail.com>
Date:   Tue Jan 10 13:32:12 2017 +0000

    Keras backend - support for Theano sequential models (#2562)

    * gitignore
    stub code for server
    pom - basic config

    * add keras to parent pom

    * slf logging

    * copying data from float[] to NDArray according to specified shape

    * reading data from h5 files

    * ndj4 api deps

    * fix the issue with copying only the first array element

    * select correct batch intervals for labels dataset

    * remove obsolete log

    * trace batch processing progress

    * removed ugly hack for tensorflow

    * additional logging and import cleanup

    * pom: remove compiler plugin, add dependency to single backend (??? not sure which backend should be shipped with server)

    * better package name

    * fixes from the comments

    * changes from comments + little cleanup

    * pojo for fit entry point

    * fluent assertions + re-indent

    * use the pojo as parameter

    * missing test scope

    * failing test

    * not using assertj finally

    * resources for Keras theano model for MNIST example

    * working test

    * logging in tests

    * use lombok in Server

    * changing poms according to comments

    * javadoc

    * javadoc

    * try-with and other comments

    * refactor a bit - extract fitInBatches

    * javadoc

    * enum for model types

    * refactoring: using the enum

    * reorganizing pom (adding backend for tests)

    * refactoring into smaller pieces

    * basic unit tests for neural network batch learner

    * using nd4j-native-platform instead of nd4j-native

    * @author

    * use nd4j-native again

    * StringEndsWith predicate

    * switch from reading a whole file with all data to multiple files with batches

    * use databuffer instead of just raw float array

    * javadoc

    * javadoc and logging

    * javadoc

    * not used anymore

    * turning off async support - causes unpredictable segfaults in javacpp when freeing memory after hdf5

    * additional comment

    * additional javadoc

    * pre-process data set

    * unsupportedexception in getLabels

    * don't use recursivecopier

    * remove unused code


    Former-commit-id: dead64e695a15e9a3abff1600197e7cfe22dcdf0