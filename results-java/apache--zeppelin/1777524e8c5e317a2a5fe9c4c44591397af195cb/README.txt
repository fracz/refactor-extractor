commit 1777524e8c5e317a2a5fe9c4c44591397af195cb
Author: maocorte <mauro.cortellazzi@radicalbit.io>
Date:   Mon Feb 29 08:49:36 2016 +0100

    [ZEPPELIN-701] - Upgrade Tachyon Interpreter to Alluxio Interpreter

    ### What is this PR for?

    Upgrading existing Tachyon interpreter to Alluxio interpreter.

    ### What type of PR is it?

    improvement

    ### Is there a relevant Jira issue?

    https://issues.apache.org/jira/browse/ZEPPELIN-701

    ### How should this be tested?

    * [Install and configure Alluxio](http://www.alluxio.org/downloads/) on a local machine.
    * run Alluxio in local mode ```$ ./bin/alluxio-start.sh local```
    * check that interpreter params are setted to default values (hostname: localhost, port: 19998)
    * use the [Alluxio CLI commands](http://www.alluxio.org/documentation/v1.0.0/en/Command-Line-Interface.html) to interact with your Alluxio file system

    ### Questions:

    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: maocorte <mauro.cortellazzi@radicalbit.io>

    Closes #750 from maocorte/701-alluxio-interpreter and squashes the following commits:

    3b9278e [maocorte] removed unused variable
    3871541 [maocorte] refactor on tests
    42b1884 [maocorte] update tachyon interpreter to alluxio interpreter