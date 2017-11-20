commit 5e69391c85a263fc00edae7b1a3e221b609a1886
Author: Roberto Franchini <ro.franchini@gmail.com>
Date:   Wed Dec 28 11:41:28 2016 +0100

    refactors of ETL, first steps to improve logging and error handling
    - removes system out in favor of OLogManager
    - adds specialized logging config file to etl scripts (bat/sh)
    - renames classes adding OETL prefix to all
    - refactors tests to use one in memory db for each test method

    refs https://github.com/orientechnologies/orientdb/issues/6872