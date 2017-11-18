commit 492310987d9033cf8763435368872337b6032bab
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Mon Apr 14 16:30:38 2014 +0200

    improve error message for common elasticsearch discovery problems

    guess default HTTP endpoint for elasticsearch and try to read config

    * compare cluster.name
    * check compatible version
    * warn if no connection could be made

    fixes #506

    (cherry picked from commit 2db6a92a51b8efd620fbc26879f69dc2e6e40bce)

    Conflicts:
            graylog2-server/src/main/java/org/graylog2/bindings/providers/IndexerProvider.java
            graylog2-server/src/main/java/org/graylog2/indexer/Indexer.java
            graylog2-server/src/test/java/org/graylog2/indexer/IndexerTest.java