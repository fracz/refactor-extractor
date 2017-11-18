commit 0814d46758eddabe8b0cfe382f325f8fd2023110
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Mon Jan 6 16:30:36 2014 +0100

    elasticsearch config file for graylog2 server node takes precedence over default values.

    previously the values in graylog2.conf or the default values in the code would override whatever was in the external config file for the graylog2 server node
    now the elasticsearch yml file overrides the other values.

    added tests

    slight refactoring of the Indexer startup procedure: to allow separate configuration reading the class now has a separate start method that needs to be invoked

    fixes issue #351