commit 109b0807fce6453b7543c68069f30b7cece0f20e
Author: Lee moon soo <moon@apache.org>
Date:   Thu Jun 4 17:00:07 2015 +0900

    Move Spark specific things from pom.xml to spark/pom.xml

    Move Spark specific dependencyManagement and properties from pom.xml to spark/pom.xml.
    Which interfere other interpreter's dependency version.

    Author: Lee moon soo <moon@apache.org>

    Closes #88 from Leemoonsoo/pom_refactor and squashes the following commits:

    9916875 [Lee moon soo] automated ci test not only spark-1.3 but also spark-1.2, spark-1.1
    aa6d1fd [Lee moon soo] Test pyspark with spark cluster
    be0b7c4 [Lee moon soo] Remove unnecessary #
    40698f3 [Lee moon soo] Make default version 1.3.1
    18cb474 [Lee moon soo] Parse version correctly
    b5f7343 [Lee moon soo] Make hadoop version configurable in test
    bb47e81 [Lee moon soo] Add license header
    8b6d3f5 [Lee moon soo] Gracefully shutdown ZeppelinServer in test
    80698e9 [Lee moon soo] Add test against spark cluster
    654d761 [Lee moon soo] Move spark specific dependencyManagement and properties block from pom.xml to spark/pom.xml