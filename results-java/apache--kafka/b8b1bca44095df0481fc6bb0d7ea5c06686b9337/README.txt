commit b8b1bca44095df0481fc6bb0d7ea5c06686b9337
Author: Geoff Anderson <geoff@confluent.io>
Date:   Tue Sep 8 17:59:49 2015 -0700

    KAFKA-2489: add benchmark for new consumer

    ewencp
    The changes here are smaller than they look - mostly refactoring/cleanup.

    - ConsumerPerformanceService: added new_consumer flag, and exposed more command-line settings
    - benchmark.py: refactored to use `parametrize` and `matrix` - this reduced some amount of repeated code
    - benchmark.py: added consumer performance tests with new consumer (using `parametrize`)
    - benchmark.py: added more detailed test descriptions
    - performance.py: broke into separate files

    Author: Geoff Anderson <geoff@confluent.io>

    Reviewers: Ewen Cheslack-Postava, Jason Gustafson, Gwen Shapira

    Closes #179 from granders/KAFKA-2489-benchmark-new-consumer