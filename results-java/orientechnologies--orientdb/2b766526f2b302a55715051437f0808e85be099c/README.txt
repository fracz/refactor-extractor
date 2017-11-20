commit 2b766526f2b302a55715051437f0808e85be099c
Author: Luigi Dell'Aquila <luigi.dellaquila@gmail.com>
Date:   Wed Jan 13 16:12:05 2016 +0100

    Fix transaction operation ordering after refactoring

    operations in transactions did not reach the server in the same order
    as they were executed in the client