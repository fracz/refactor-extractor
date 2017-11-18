commit 0d2e91ed502c31f954a99d93d602e7af4abb5f72
Author: Dongkyu Hwangbo <hwangbodk@gmail.com>
Date:   Wed Apr 5 06:07:43 2017 +0900

    Adding Kafka-emitter (#3860)

    * Initial commit

    * Apply another config: clustername

    * Rename variable

    * Fix bug

    * Add retry logic

    * Edit retry logic

    * Upgrade kafka-clients version to the most recent release

    * Make callback single object

    * Write documentation

    * Rewrite error message and emit logic

    * Handling AlertEvent

    * Override toString()

    * make clusterName more optional

    * bump up druid version

    * add producer.config option which make user can apply another optional config value of kafka producer

    * remove potential blocking in emit()

    * using MemoryBoundLinkedBlockingQueue

    * Fixing coding convention

    * Remove logging every exception and just increment counting

    * refactoring

    * trivial modification

    * logging when callback has exception

    * Replace kafka-clients 0.10.1.1 with 0.10.2.0

    * Resolve the problem related of classloader

    * adopt try statement

    * code reformatting

    * make variables final

    * rewrite toString